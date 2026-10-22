package com.eaglebank.web.dto;

import com.eaglebank.domain.BankTransaction;
import com.eaglebank.domain.Currency;
import com.eaglebank.domain.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Immutable ledger transaction record")
public record TransactionResponse(
        @Schema(example = "tan-123abc45")
        String id,
        @Schema(example = "100.50")
        BigDecimal amount,
        @Schema(example = "GBP")
        Currency currency,
        @Schema(example = "deposit")
        TransactionType type,
        @Schema(example = "Monthly salary deposit")
        String reference,
        @Schema(example = "usr-abc12345")
        String userId,
        Instant createdTimestamp
) {
    public static TransactionResponse fromDomain(BankTransaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getType(),
                tx.getReference(),
                tx.getUserId(),
                tx.getCreatedTimestamp()
        );
    }

    public record ListTransactionsResponse(List<TransactionResponse> transactions) {
    }
}
