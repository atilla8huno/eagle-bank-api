package com.eaglebank.service;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.Address;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.BankTransaction;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.IdempotencyRecord;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.domain.User;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.BankTransactionRepository;
import com.eaglebank.repository.IdempotencyRepository;
import com.eaglebank.web.dto.CreateTransactionRequest;
import com.eaglebank.web.dto.TransactionResponse;
import com.eaglebank.web.dto.TransactionResponse.ListTransactionsResponse;
import com.eaglebank.web.exception.DomainExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
@DisplayName("Test suite of the class: TransactionService")
class TransactionServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-22T12:00:00Z");

    private TransactionService service;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setup() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new TransactionService(
                accountRepository,
                transactionRepository,
                idempotencyRepository,
                transactionManager,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("Should retry on optimistic lock collision and succeed")
    void givenOptimisticLockCollision_whenCreateTransaction_thenShouldRetryAndSucceed() {
        //given
        AtomicInteger callCount = new AtomicInteger(0);

        when(accountRepository.findById(eq("01123456"))).thenAnswer(inv -> Optional.of(dummyAccount("100.00")));
        when(accountRepository.saveAndFlush(any(BankAccount.class))).thenAnswer(inv -> {
            if (callCount.incrementAndGet() < 3) {
                throw new ObjectOptimisticLockingFailureException(BankAccount.class, "01123456");
            }
            return inv.getArgument(0);
        });

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("25.00"), Currency.GBP, TransactionType.DEPOSIT, "Retry test"
        );

        //when
        TransactionResponse response = service.createTransaction("01123456", "usr-owner", request, "idem-retry");

        //then
        assertNotNull(response);
        assertEquals(new BigDecimal("25.00"), response.amount());
        assertEquals(3, callCount.get());
        verify(transactionRepository, times(1)).save(any(BankTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when optimistic lock retries are exhausted")
    void givenPersistentOptimisticLockCollision_whenCreateTransaction_thenShouldThrowException() {
        //given
        when(accountRepository.findById(eq("01123456"))).thenAnswer(inv -> Optional.of(dummyAccount("100.00")));
        when(accountRepository.saveAndFlush(any(BankAccount.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(BankAccount.class, "01123456"));

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("25.00"), Currency.GBP, TransactionType.DEPOSIT, "Exhausted retry"
        );

        //when
        Executable createTx = () -> service.createTransaction("01123456", "usr-owner", request, "idem-exhausted");

        //then
        assertThrows(ObjectOptimisticLockingFailureException.class, createTx);

        verify(transactionRepository, times(0)).save(any(BankTransaction.class));
        verify(idempotencyRepository, times(0)).save(any(IdempotencyRecord.class));
    }

    @Test
    @DisplayName("Should return existing transaction when Idempotency-Key already exists")
    void givenExistingIdempotencyKey_whenCreateTransaction_thenShouldReturnPriorTransaction() {
        //given
        BankAccount account = dummyAccount("100.00");
        when(accountRepository.findById(eq("01123456"))).thenReturn(Optional.of(account));

        BankTransaction existingTx = new BankTransaction(
                "tan-existing1", "01123456", account.getUser(),
                new BigDecimal("50.00"), Currency.GBP, TransactionType.DEPOSIT, "Prior", FIXED_NOW
        );
        IdempotencyRecord existingRecord = new IdempotencyRecord("01123456", "idem-1", existingTx, FIXED_NOW);
        when(idempotencyRepository.findByAccountNumberAndIdempotencyKey(eq("01123456"), eq("idem-1")))
                .thenReturn(Optional.of(existingRecord));

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("50.00"), Currency.GBP, TransactionType.DEPOSIT, "Prior"
        );

        //when
        TransactionResponse response = service.createTransaction("01123456", "usr-owner", request, "idem-1");

        //then
        assertNotNull(response);
        assertEquals("tan-existing1", response.id());
    }

    @Test
    @DisplayName("Should list and fetch transactions and enforce 404, 403 and 422 rules")
    void givenAccountAndTransactions_whenListingOrFetching_thenShouldEnforceRules() {
        //given
        BankAccount account = dummyAccount("100.00");
        when(accountRepository.findById(eq("01123456"))).thenReturn(Optional.of(account));
        when(accountRepository.findById(eq("01000000"))).thenReturn(Optional.empty());

        BankTransaction tx = new BankTransaction(
                "tan-111", "01123456", account.getUser(),
                new BigDecimal("20.00"), Currency.GBP, TransactionType.WITHDRAWAL, "Lunch", FIXED_NOW
        );
        when(transactionRepository.findAllByAccountNumberOrderByCreatedTimestampDesc(eq("01123456")))
                .thenReturn(List.of(tx));
        when(transactionRepository.findByIdAndAccountNumber(eq("tan-111"), eq("01123456")))
                .thenReturn(Optional.of(tx));
        when(transactionRepository.findByIdAndAccountNumber(eq("tan-missing"), eq("01123456")))
                .thenReturn(Optional.empty());

        CreateTransactionRequest overdraft = new CreateTransactionRequest(
                new BigDecimal("500.00"), Currency.GBP, TransactionType.WITHDRAWAL, "Overdraft"
        );

        //when
        ListTransactionsResponse list = service.listAccountTransactions("01123456", "usr-owner");
        TransactionResponse fetched = service.fetchAccountTransactionById("01123456", "tan-111", "usr-owner");

        Executable listMissing = () -> service.listAccountTransactions("01000000", "usr-owner");
        Executable listForbidden = () -> service.listAccountTransactions("01123456", "usr-intruder");
        Executable fetchAccountMissing = () -> service.fetchAccountTransactionById("01000000", "tan-111", "usr-owner");
        Executable fetchForbidden = () -> service.fetchAccountTransactionById("01123456", "tan-111", "usr-intruder");
        Executable fetchTxMissing = () -> service.fetchAccountTransactionById("01123456", "tan-missing", "usr-owner");
        Executable withdrawInsufficient = () -> service.createTransaction("01123456", "usr-owner", overdraft, "idem-overdraft");

        //then
        assertEquals(1, list.transactions().size());
        assertEquals("tan-111", fetched.id());
        assertThrows(DomainExceptions.ResourceNotFoundException.class, listMissing);
        assertThrows(DomainExceptions.ForbiddenOperationException.class, listForbidden);
        assertThrows(DomainExceptions.ResourceNotFoundException.class, fetchAccountMissing);
        assertThrows(DomainExceptions.ForbiddenOperationException.class, fetchForbidden);
        assertThrows(DomainExceptions.ResourceNotFoundException.class, fetchTxMissing);
        assertThrows(DomainExceptions.InsufficientFundsException.class, withdrawInsufficient);
    }

    private BankAccount dummyAccount(String balance) {
        User owner = new User(
                "usr-owner",
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                "owner@eaglebank.com",
                "hash",
                FIXED_NOW,
                FIXED_NOW
        );
        return new BankAccount(
                "01123456",
                "10-10-10",
                owner,
                "Personal Account",
                AccountType.PERSONAL,
                new BigDecimal(balance),
                Currency.GBP,
                FIXED_NOW,
                FIXED_NOW
        );
    }
}
