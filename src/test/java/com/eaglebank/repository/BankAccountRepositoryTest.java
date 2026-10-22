package com.eaglebank.repository;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.Money;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Test suite of the class: BankAccountRepository")
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should increase version when saving bank account multiple times")
    void givenBankAccount_whenSavingMultipleTimes_thenShouldIncreaseVersion() {
        //given
        Instant t1 = Instant.parse("2026-10-21T09:00:00Z");
        Instant t2 = Instant.parse("2026-10-21T09:05:00Z");
        BankAccount account = new BankAccount(
                "01200001", "10-10-10", "usr-h2200", "First Account",
                AccountType.PERSONAL, new BigDecimal("100.00"), Currency.GBP, t1, t1
        );
        repository.saveAndFlush(account);
        Long initialVersion = account.getVersion();
        entityManager.clear();

        //when
        BankAccount managed = repository.findById("01200001").orElseThrow();
        managed.updateBalance(Money.gbp(new BigDecimal("175.50")), t2);
        BankAccount secondSaving = repository.saveAndFlush(managed);

        //then
        assertNotNull(initialVersion);
        assertTrue(initialVersion < secondSaving.getVersion());
        assertEquals(new BigDecimal("175.50"), secondSaving.getBalance());
    }

    @Test
    @DisplayName("Should list accounts by userId ordered by createdTimestamp ascending and check existence")
    void givenMultipleAccounts_whenFindAllByUserId_thenShouldReturnOrderedList() {
        //given
        Instant t1 = Instant.parse("2026-10-21T09:00:00Z");
        Instant t2 = Instant.parse("2026-10-21T09:05:00Z");
        BankAccount first = new BankAccount(
                "01200001", "10-10-10", "usr-h2200", "First Account",
                AccountType.PERSONAL, new BigDecimal("100.00"), Currency.GBP, t1, t1
        );
        BankAccount second = new BankAccount(
                "01200002", "10-10-10", "usr-h2200", "Second Account",
                AccountType.PERSONAL, new BigDecimal("250.00"), Currency.GBP, t2, t2
        );
        repository.saveAndFlush(first);
        repository.saveAndFlush(second);
        entityManager.clear();

        //when
        List<BankAccount> accounts = repository.findAllByUserIdOrderByCreatedTimestampAsc("usr-h2200");

        //then
        assertEquals(2, accounts.size());
        assertEquals("01200001", accounts.get(0).getAccountNumber());
        assertEquals("01200002", accounts.get(1).getAccountNumber());
    }
}
