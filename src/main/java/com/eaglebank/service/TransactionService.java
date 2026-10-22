package com.eaglebank.service;

import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.BankTransaction;
import com.eaglebank.domain.IdGenerator;
import com.eaglebank.domain.IdempotencyRecord;
import com.eaglebank.domain.Money;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.BankTransactionRepository;
import com.eaglebank.repository.IdempotencyRepository;
import com.eaglebank.service.specification.AccountSpecifications;
import com.eaglebank.service.strategy.TransactionOperation;
import com.eaglebank.web.dto.CreateTransactionRequest;
import com.eaglebank.web.dto.TransactionResponse;
import com.eaglebank.web.dto.TransactionResponse.ListTransactionsResponse;
import com.eaglebank.web.exception.DomainExceptions;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TransactionService {

    private static final int MAX_OPTIMISTIC_RETRIES = 15;

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public TransactionService(
            BankAccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            IdempotencyRepository idempotencyRepository,
            PlatformTransactionManager transactionManager,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    public TransactionResponse createTransaction(
            String accountNumber,
            String authenticatedUserId,
            CreateTransactionRequest request,
            String idempotencyKey
    ) {
        int attempt = 0;
        while (true) {
            try {
                attempt++;
                return transactionTemplate.execute(status ->
                        executeAttempt(accountNumber, authenticatedUserId, request, idempotencyKey)
                );
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException | CannotAcquireLockException ex) {
                if (attempt >= MAX_OPTIMISTIC_RETRIES) {
                    throw ex;
                }
                backoffWithJitter(attempt);
            }
        }
    }

    @Transactional(readOnly = true)
    public ListTransactionsResponse listAccountTransactions(String accountNumber, String authenticatedUserId) {
        BankAccount account = findAccount(accountNumber);

        AccountSpecifications.assertOwnedBy(
                account,
                authenticatedUserId,
                "The user is not allowed to access the transactions"
        );

        List<TransactionResponse> transactions = transactionRepository
                .findAllByAccountNumberOrderByCreatedTimestampDesc(accountNumber)
                .stream()
                .map(TransactionResponse::fromDomain)
                .toList();

        return new ListTransactionsResponse(transactions);
    }

    @Transactional(readOnly = true)
    public TransactionResponse fetchAccountTransactionById(
            String accountNumber,
            String transactionId,
            String authenticatedUserId
    ) {
        BankAccount account = findAccount(accountNumber);

        AccountSpecifications.assertOwnedBy(
                account,
                authenticatedUserId,
                "The user is not allowed to access the transaction"
        );

        BankTransaction transaction = findTransaction(accountNumber, transactionId);

        return TransactionResponse.fromDomain(transaction);
    }

    private TransactionResponse executeAttempt(
            String accountNumber,
            String authenticatedUserId,
            CreateTransactionRequest request,
            String idempotencyKey
    ) {
        BankAccount account = findAccount(accountNumber);

        AccountSpecifications.assertOwnedBy(
                account,
                authenticatedUserId,
                "The user is not allowed to access the transactions"
        );

        if (hasIdempotencyKey(idempotencyKey)) {
            Optional<IdempotencyRecord> existingKey = idempotencyRepository
                    .findByAccountNumberAndIdempotencyKey(accountNumber, idempotencyKey);
            if (existingKey.isPresent()) {
                BankTransaction priorTx = transactionRepository
                        .findById(existingKey.get().getTransactionId())
                        .orElseThrow(() -> new DomainExceptions.ResourceNotFoundException("Transaction was not found"));
                return TransactionResponse.fromDomain(priorTx);
            }
        }

        Money currentBalance = account.getMoneyBalance();
        Money transactionAmount = new Money(request.amount(), request.currency());
        TransactionOperation operation = TransactionOperation.forType(request.type());
        Money updatedBalance = operation.apply(currentBalance, transactionAmount);

        Instant now = Instant.now(clock);
        account.updateBalance(updatedBalance, now);
        accountRepository.saveAndFlush(account);

        BankTransaction transaction = new BankTransaction(
                IdGenerator.nextTransactionId(),
                accountNumber,
                authenticatedUserId,
                transactionAmount.amount(),
                transactionAmount.currency(),
                request.type(),
                request.reference(),
                now
        );
        transactionRepository.save(transaction);

        if (hasIdempotencyKey(idempotencyKey)) {
            idempotencyRepository.save(new IdempotencyRecord(accountNumber, idempotencyKey, transaction.getId(), now));
        }

        return TransactionResponse.fromDomain(transaction);
    }

    private BankAccount findAccount(String accountNumber) {
        return accountRepository
                .findById(accountNumber)
                .orElseThrow(() -> new DomainExceptions.ResourceNotFoundException("Bank account was not found"));
    }

    private BankTransaction findTransaction(String accountNumber, String transactionId) {
        return transactionRepository
                .findByIdAndAccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new DomainExceptions.ResourceNotFoundException("Transaction was not found"));
    }

    private boolean hasIdempotencyKey(String idempotencyKey) {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }

    private void backoffWithJitter(int attempt) {
        try {
            long sleepMillis = Math.min(50L, (1L << Math.min(attempt, 4)) + ThreadLocalRandom.current().nextLong(1, 8));
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying optimistic lock", e);
        }
    }
}
