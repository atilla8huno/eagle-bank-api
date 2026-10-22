package com.eaglebank.web.dto;

import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload to deposit or withdraw money")
public record CreateTransactionRequest(
        @Schema(example = "100.50", minimum = "0.01", maximum = "10000.00")
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        @DecimalMax(value = "10000.00", message = "amount must not exceed 10000.00")
        @Digits(integer = 5, fraction = 2, message = "amount must have at most 2 decimal places")
        BigDecimal amount,
        @Schema(example = "GBP")
        @NotNull(message = "currency is required")
        Currency currency,
        @Schema(example = "deposit")
        @NotNull(message = "type is required")
        TransactionType type,
        @Schema(example = "Monthly salary deposit")
        String reference
) {
}
