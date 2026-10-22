package com.eaglebank.repository;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.BankTransaction;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Test suite of the class: BankTransactionRepository")
class BankTransactionRepositoryTest {

    @Autowired
    private BankTransactionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should list transactions by accountNumber ordered by createdTimestamp descending")
    void givenTransactions_whenFindAllByAccountNumber_thenShouldReturnDescendingOrder() {
        //given
        Instant t1 = Instant.parse("2026-10-21T10:00:00Z");
        Instant t2 = Instant.parse("2026-10-21T10:15:00Z");
        User owner = userRepository.saveAndFlush(dummyUser("usr-h2300", t1));
        BankTransaction tx1 = new BankTransaction(
                "tan-h2001", "01300001", owner,
                new BigDecimal("100.00"), Currency.GBP, TransactionType.DEPOSIT, "First deposit", t1
        );
        BankTransaction tx2 = new BankTransaction(
                "tan-h2002", "01300001", owner,
                new BigDecimal("40.00"), Currency.GBP, TransactionType.WITHDRAWAL, "ATM withdrawal", t2
        );
        repository.saveAndFlush(tx1);
        repository.saveAndFlush(tx2);
        entityManager.clear();

        //when
        List<BankTransaction> ledger = repository.findAllByAccountNumberOrderByCreatedTimestampDesc("01300001");

        //then
        assertEquals(2, ledger.size());
        assertEquals("tan-h2002", ledger.get(0).getId());
        assertEquals("tan-h2001", ledger.get(1).getId());
    }

    @Test
    @DisplayName("Should find transaction by id and accountNumber only when both match")
    void givenTransaction_whenFindByIdAndAccountNumber_thenShouldMatchAccount() {
        //given
        Instant t1 = Instant.parse("2026-10-21T10:00:00Z");
        User owner = userRepository.saveAndFlush(dummyUser("usr-h2300", t1));
        BankTransaction tx = new BankTransaction(
                "tan-h2001", "01300001", owner,
                new BigDecimal("100.00"), Currency.GBP, TransactionType.DEPOSIT, "First deposit", t1
        );
        repository.saveAndFlush(tx);
        entityManager.clear();

        //when & then
        assertTrue(repository.findByIdAndAccountNumber("tan-h2001", "01300001").isPresent());
        assertFalse(repository.findByIdAndAccountNumber("tan-h2001", "01999999").isPresent());
    }

    private User dummyUser(String userId, Instant now) {
        return new User(
                userId,
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                userId + "@eaglebank.com",
                "hash",
                now,
                now
        );
    }
}
