package com.eaglebank.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Currency {
    GBP("GBP");

    private final String code;

    Currency(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static Currency fromValue(String value) {
        for (Currency currency : values()) {
            if (currency.code.equalsIgnoreCase(value)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unsupported currency: " + value);
    }
}
