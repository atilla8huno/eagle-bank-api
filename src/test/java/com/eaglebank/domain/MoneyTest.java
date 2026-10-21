package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Test suite of the class: Money")
class MoneyTest {

    @Test
    @DisplayName("Should enforce scale 2 and HALF_EVEN rouding on Money")
    void givenBigDecimalValues_whenNewMoney_thenShouldRoundAndCalculateAccurately() {
        // given
        Money zero = Money.zeroGbp();
        Money roundedEvenDown = Money.gbp(new BigDecimal("10.125"));
        Money roundedEvenUp = Money.gbp(new BigDecimal("10.135"));

        // when
        Money sum = roundedEvenDown.add(Money.gbp(new BigDecimal("5.00")));
        Money diff = sum.subtract(Money.gbp(new BigDecimal("5.12")));

        // then
        assertEquals(new BigDecimal("0.00"), zero.amount());
        assertEquals(Currency.GBP, zero.currency());
        assertEquals(new BigDecimal("10.12"), roundedEvenDown.amount());
        assertEquals(new BigDecimal("10.14"), roundedEvenUp.amount());
        assertEquals(new BigDecimal("15.12"), sum.amount());
        assertEquals(new BigDecimal("10.00"), diff.amount());
        assertTrue(diff.isLessThan(sum));
        assertFalse(sum.isLessThan(diff));
        assertFalse(Money.gbp(new BigDecimal("10000.00")).exceedsMaxLimit());
        assertTrue(Money.gbp(new BigDecimal("10000.01")).exceedsMaxLimit());
    }

    @ParameterizedTest
    @MethodSource("invalidParametersToCreateMoney")
    @DisplayName("Should NOT accept null parameters when creating Money")
    void givenNullParameters_whenNewMoney_thenShouldThrowException(BigDecimal amount, Currency currency) {
        // given parameters
        // when
        Executable newMoneyInstance = () -> new Money(amount, currency);

        // then
        assertThrows(NullPointerException.class, newMoneyInstance);
    }

    private static Stream<Arguments> invalidParametersToCreateMoney() {
        return Stream.of(
                arguments(null, Currency.GBP),
                arguments(ONE, null)
        );
    }
}
