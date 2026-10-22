package com.eaglebank;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.service.AccountService;
import com.eaglebank.service.TransactionService;
import com.eaglebank.service.UserService;
import com.eaglebank.web.dto.AddressDto;
import com.eaglebank.web.dto.BankAccountResponse;
import com.eaglebank.web.dto.CreateBankAccountRequest;
import com.eaglebank.web.dto.CreateTransactionRequest;
import com.eaglebank.web.dto.CreateUserRequest;
import com.eaglebank.web.dto.TransactionResponse;
import com.eaglebank.web.dto.UserResponse;
import com.eaglebank.web.exception.DomainExceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Test suite of concurrent transactions")
public class ConcurrencyTransactionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Test
    @DisplayName("Should process 50 withdrawals and reject 50 when 100 threads withdraw 10.00 from 500.00")
    void givenFundedAccount_when100ParallelWithdrawals_thenShouldAllow50AndReject50() throws Exception {
        //given
        UserResponse user = createDummyUser("concurrency@eaglebank.com");
        BankAccountResponse account = accountService.createAccount(
                user.id(),
                new CreateBankAccountRequest("Personal Account", AccountType.PERSONAL)
        );
        transactionService.createTransaction(
                account.accountNumber(),
                user.id(),
                new CreateTransactionRequest(new BigDecimal("500.00"), Currency.GBP, TransactionType.DEPOSIT, "Initial funding"),
                "init-deposit-key"
        );

        int totalThreads = 100;
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger succeededCount = new AtomicInteger(0);
        AtomicInteger insufficientFundsCount = new AtomicInteger(0);

        //when
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Void>> tasks = new ArrayList<>(totalThreads);
            for (int i = 0; i < totalThreads; i++) {
                final int attemptNumber = i;
                tasks.add(() -> {
                    startGate.await();
                    try {
                        createTransaction(account, user, attemptNumber);
                        succeededCount.incrementAndGet();
                    } catch (DomainExceptions.InsufficientFundsException ex) {
                        insufficientFundsCount.incrementAndGet();
                    }
                    return null;
                });
            }

            List<Future<Void>> futures = tasks.stream().map(executor::submit).toList();
            startGate.countDown();
            for (Future<Void> future : futures) {
                future.get();
            }
        }

        //then
        BankAccountResponse finalAccount = accountService.fetchAccountByAccountNumber(account.accountNumber(), user.id());
        TransactionResponse.ListTransactionsResponse ledger = transactionService.listAccountTransactions(account.accountNumber(), user.id());

        assertEquals(50, succeededCount.get());
        assertEquals(50, insufficientFundsCount.get());
        assertEquals(new BigDecimal("0.00"), finalAccount.balance());
        assertEquals(51, ledger.transactions().size());
    }

    @Test
    @DisplayName("Should deduplicate retried deposit requests with same Idempotency-Key")
    void givenSameIdempotencyKey_whenRetryingDeposit_thenShouldCreditBalanceOnce() {
        //given
        UserResponse user = createDummyUser("idempotency@eaglebank.com");
        BankAccountResponse account = accountService.createAccount(
                user.id(),
                new CreateBankAccountRequest("Savings Account", AccountType.PERSONAL)
        );
        CreateTransactionRequest depositRequest = new CreateTransactionRequest(
                new BigDecimal("250.00"),
                Currency.GBP,
                TransactionType.DEPOSIT,
                "Salary payment"
        );

        //when
        TransactionResponse firstAttempt = transactionService.createTransaction(
                account.accountNumber(), user.id(), depositRequest, "idem-key-999"
        );
        TransactionResponse secondAttempt = transactionService.createTransaction(
                account.accountNumber(), user.id(), depositRequest, "idem-key-999"
        );
        TransactionResponse thirdAttempt = transactionService.createTransaction(
                account.accountNumber(), user.id(), depositRequest, "idem-key-999"
        );

        //then
        BankAccountResponse updatedAccount = accountService.fetchAccountByAccountNumber(account.accountNumber(), user.id());

        assertEquals(firstAttempt.id(), secondAttempt.id());
        assertEquals(firstAttempt.id(), thirdAttempt.id());
        assertEquals(new BigDecimal("250.00"), updatedAccount.balance());
    }

    private void createTransaction(BankAccountResponse account, UserResponse user, int attemptNumber) {
        transactionService.createTransaction(
                account.accountNumber(),
                user.id(),
                new CreateTransactionRequest(
                        new BigDecimal("10.00"),
                        Currency.GBP,
                        TransactionType.WITHDRAWAL,
                        "Withdrawal #" + attemptNumber
                ),
                null
        );
    }

    private UserResponse createDummyUser(String email) {
        return userService.createUser(new CreateUserRequest(
                "Atilla Barros",
                new AddressDto("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900111",
                email,
                "Barclays2026!"
        ));
    }
}
