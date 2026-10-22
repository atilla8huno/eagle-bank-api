package com.eaglebank.web;


import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.BankTransaction;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.BankTransactionRepository;
import com.eaglebank.repository.IdempotencyRepository;
import com.eaglebank.security.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Test suite of the class: TransactionController")
class TransactionControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-22T12:00:00Z");

    @Autowired
    private MockMvc client;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @BeforeEach
    void setup() {
        idempotencyRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create deposit transaction on POST /v1/accounts/{accountNumber}/transactions and commit balance, ledger and idempotency record")
    void givenSeededAccount_whenCreateDepositTransaction_thenShouldCommitBalanceLedgerAndIdempotency() throws Exception {
        //given
        String userId = "usr-alice1";
        String accountNumber = "01234567";
        BankAccount initial = accountRepository.saveAndFlush(dummyAccount(accountNumber, userId, new BigDecimal("100.00")));
        Long initialVersion = initial.getVersion();

        String body = """
                {
                  "amount": 200.50,
                  "currency": "GBP",
                  "type": "deposit",
                  "reference": "Initial Deposit"
                }
                """;

        //when
        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .header("Idempotency-Key", "idem-1")
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", matchesPattern("^tan-[A-Za-z0-9]+$")))
                .andExpect(jsonPath("$.amount").value(200.50))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.userId").value(userId));

        BankAccount updatedAccount = accountRepository.findById(accountNumber).orElseThrow();
        assertEquals(new BigDecimal("300.50"), updatedAccount.getBalance());
        assertTrue(updatedAccount.getVersion() > initialVersion);
        assertEquals(1, transactionRepository.count());
        assertTrue(idempotencyRepository.findByAccountNumberAndIdempotencyKey(accountNumber, "idem-1").isPresent());
    }

    @Test
    @DisplayName("Should create withdrawal transaction on POST /v1/accounts/{accountNumber}/transactions and debit balance")
    void givenFundedAccount_whenCreateWithdrawalTransaction_thenShouldDebitBalanceAndRecordTransaction() throws Exception {
        //given
        String userId = "usr-alice1";
        String accountNumber = "01234567";
        accountRepository.saveAndFlush(dummyAccount(accountNumber, userId, new BigDecimal("250.00")));

        String body = """
                {
                  "amount": 75.25,
                  "currency": "GBP",
                  "type": "withdrawal",
                  "reference": "ATM Withdrawal"
                }
                """;

        //when
        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(75.25))
                .andExpect(jsonPath("$.type").value("withdrawal"));

        BankAccount updatedAccount = accountRepository.findById(accountNumber).orElseThrow();
        assertEquals(new BigDecimal("174.75"), updatedAccount.getBalance());
        assertEquals(1, transactionRepository.count());
    }

    @Test
    @DisplayName("Should rollback transaction and leave balance and ledger untouched on 422 insufficient funds or max limit exceeded")
    void givenInsufficientFundsOrMaxLimitExceeded_whenCreateTransaction_thenShouldReturn422AndRollbackDatabase() throws Exception {
        //given
        String userId = "usr-alice1";
        String accountNumber = "01234567";
        BankAccount initial = accountRepository.saveAndFlush(dummyAccount(accountNumber, userId, new BigDecimal("100.00")));
        Long initialVersion = initial.getVersion();

        //when & then
        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .header("Idempotency-Key", "idem-overdraft")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 500.00,
                                  "currency": "GBP",
                                  "type": "withdrawal",
                                  "reference": "Overdraft attempt"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient funds to process transaction"));

        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .header("Idempotency-Key", "idem-maxlimit")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 9950.00,
                                  "currency": "GBP",
                                  "type": "deposit",
                                  "reference": "Over 10k limit"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Account balance cannot exceed maximum limit of 10000.00 GBP"));

        BankAccount unchangedAccount = accountRepository.findById(accountNumber).orElseThrow();
        assertEquals(new BigDecimal("100.00"), unchangedAccount.getBalance());
        assertEquals(initialVersion, unchangedAccount.getVersion());
        assertEquals(0, transactionRepository.count());
        assertEquals(0, idempotencyRepository.count());
    }

    @Test
    @DisplayName("Should NOT create transaction when payload is invalid (400), non-owner (403) or account is missing (404)")
    void givenInvalidBodyNonOwnerOrMissingAccount_whenCreateTransaction_thenShouldRejectAndNotWriteToDatabase() throws Exception {
        //given
        String userId = "usr-alice1";
        String accountNumber = "01234567";
        accountRepository.saveAndFlush(dummyAccount(accountNumber, userId, new BigDecimal("100.00")));

        //when & then
        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "currency": "GBP"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());

        client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt("usr-bob2"))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00,
                                  "currency": "GBP",
                                  "type": "withdrawal"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the transactions"));

        client.perform(post("/v1/accounts/{accountNumber}/transactions", "01888888")
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00,
                                  "currency": "GBP",
                                  "type": "deposit"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account was not found"));

        assertEquals(new BigDecimal("100.00"), accountRepository.findById(accountNumber).orElseThrow().getBalance());
        assertEquals(0, transactionRepository.count());
    }

    @Test
    @DisplayName("Should list transactions on GET /v1/accounts/{accountNumber}/transactions and reject non-owner")
    void givenSeededTransactions_whenListAccountTransactions_thenShouldReturnListForOwnerAndRejectNonOwner() throws Exception {
        //given
        String userId = "usr-alice1";
        String accountNumber = "01234567";
        accountRepository.saveAndFlush(dummyAccount(accountNumber, userId, new BigDecimal("200.50")));
        transactionRepository.saveAndFlush(new BankTransaction(
                "tan-abc12345", accountNumber, userId,
                new BigDecimal("200.50"), Currency.GBP, TransactionType.DEPOSIT, "Initial Deposit", FIXED_NOW
        ));

        //when & then
        client.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].id").value("tan-abc12345"));

        client.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .with(customerJwt("usr-bob2"))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the transactions"));
    }

    @Test
    @DisplayName("Should fetch transaction by ID on GET /v1/accounts/{accountNumber}/transactions/{transactionId} and handle 403/404")
    void givenSeededTransaction_whenFetchAccountTransactionById_thenShouldReturnTransactionOrError() throws Exception {
        //given
        String userId = "usr-alice1";
        String account1 = "01234567";
        String account2 = "01765432";
        accountRepository.saveAndFlush(dummyAccount(account1, userId, new BigDecimal("200.50")));
        accountRepository.saveAndFlush(dummyAccount(account2, userId, new BigDecimal("0.00")));
        transactionRepository.saveAndFlush(new BankTransaction(
                "tan-abc12345", account1, userId,
                new BigDecimal("200.50"), Currency.GBP, TransactionType.DEPOSIT, "Initial Deposit", FIXED_NOW
        ));

        //when & then
        client.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", account1, "tan-abc12345")
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tan-abc12345"))
                .andExpect(jsonPath("$.amount").value(200.50));

        client.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", account2, "tan-abc12345")
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction was not found"));

        client.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", account1, "tan-abc12345")
                        .with(customerJwt("usr-bob2"))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the transaction"));
    }

    private RequestPostProcessor customerJwt(String userId) {
        return jwt()
                .jwt(builder -> builder.subject(userId).claim("scope", Permission.Role.CUSTOMER.toScopeClaim()))
                .authorities(Permission.Role.CUSTOMER.getAuthorities());
    }

    private BankAccount dummyAccount(String accountNumber, String userId, BigDecimal balance) {
        return new BankAccount(
                accountNumber,
                "10-10-10",
                userId,
                "Everyday Account",
                AccountType.PERSONAL,
                balance,
                Currency.GBP,
                FIXED_NOW,
                FIXED_NOW
        );
    }
}
