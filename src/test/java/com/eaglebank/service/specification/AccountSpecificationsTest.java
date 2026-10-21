package com.eaglebank.service.specification;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.Money;
import com.eaglebank.web.exception.DomainExceptions;
import com.eaglebank.web.exception.DomainExceptions.InsufficientFundsException;
import com.eaglebank.web.exception.DomainExceptions.InvalidTransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test suite of the class: AccountSpecifications")
class AccountSpecificationsTest {

    @Test
    @DisplayName("Should evaluate specification combinators (and, or, not, nor)")
    void givenSpecifications_whenCombiningWithAndOrNot_thenShouldEvaluateAccurately() {
        //given
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isEven = n -> n % 2 == 0;
        Specification<Integer> isNegative = n -> n < 0;

        //when
        Specification<Integer> positiveAndEven = isPositive.and(isEven);
        Specification<Integer> positiveOrEven = isPositive.or(isEven);
        Specification<Integer> notPositive = isPositive.not();
        Specification<Integer> neitherPositiveNorNegative = isPositive.nor(isNegative);

        //then
        assertTrue(positiveAndEven.isSatisfiedBy(4));
        assertFalse(positiveAndEven.isSatisfiedBy(3));
        assertFalse(positiveAndEven.isSatisfiedBy(-2));

        assertTrue(positiveOrEven.isSatisfiedBy(3));
        assertTrue(positiveOrEven.isSatisfiedBy(-4));
        assertFalse(positiveOrEven.isSatisfiedBy(-3));

        assertTrue(notPositive.isSatisfiedBy(-1));
        assertFalse(notPositive.isSatisfiedBy(1));

        assertTrue(neitherPositiveNorNegative.isSatisfiedBy(0));
        assertFalse(neitherPositiveNorNegative.isSatisfiedBy(1));
        assertFalse(neitherPositiveNorNegative.isSatisfiedBy(-1));
    }

    @Test
    @DisplayName("Should enforce ownership, sufficient funds and max balance specifications")
    void givenAccountAndBalances_whenAssertingSpecifications_thenShouldEnforceRules() {
        //given
        Instant now = Instant.now();
        BankAccount account = new BankAccount(
                "01111111", "10-10-10", "usr-owner1", "Account",
                AccountType.PERSONAL, new BigDecimal("100.00"), Currency.GBP, now, now
        );
        Money balance = Money.gbp(new BigDecimal("100.00"));

        //when
        Executable forbiddenCheck =
                () -> AccountSpecifications.assertOwnedBy(account, "usr-intruder", "Forbidden");
        Executable insufficientFundsCheck =
                () -> AccountSpecifications.assertSufficientFunds(balance, Money.gbp(new BigDecimal("100.01")));
        Executable maxLimitCheck =
                () -> AccountSpecifications.assertWithinMaxLimit(Money.gbp(new BigDecimal("10000.01")));

        //then
        assertTrue(AccountSpecifications.isOwnedBy("usr-owner1").isSatisfiedBy(account));
        assertFalse(AccountSpecifications.isOwnedBy("usr-intruder").isSatisfiedBy(account));
        assertFalse(AccountSpecifications.isOwnedBy("usr-owner1").isSatisfiedBy(null));

        assertDoesNotThrow(() -> AccountSpecifications.assertOwnedBy(account, "usr-owner1", "Forbidden"));
        assertThrows(DomainExceptions.ForbiddenOperationException.class, forbiddenCheck);

        assertDoesNotThrow(() -> AccountSpecifications.assertSufficientFunds(balance, Money.gbp(new BigDecimal("100.00"))));
        assertThrows(InsufficientFundsException.class, insufficientFundsCheck);

        assertDoesNotThrow(() -> AccountSpecifications.assertWithinMaxLimit(Money.gbp(new BigDecimal("10000.00"))));
        assertThrows(InvalidTransactionException.class, maxLimitCheck);
    }
}
