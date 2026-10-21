package com.eaglebank.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money zeroGbp() {
        return new Money(MIN_AMOUNT, Currency.GBP);
    }

    public static Money gbp(BigDecimal value) {
        return new Money(value, Currency.GBP);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean exceedsMaxLimit() {
        return this.amount.compareTo(MAX_AMOUNT) > 0;
    }
}
