package com.eaglebank.web.dto;


import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Bank account details")
public record BankAccountResponse(
        @Schema(example = "01234567")
        String accountNumber,
        @Schema(example = "10-10-10")
        String sortCode,
        @Schema(example = "Personal Bank Account")
        String name,
        @Schema(example = "personal")
        AccountType accountType,
        @Schema(example = "1000.00")
        BigDecimal balance,
        @Schema(example = "GBP")
        Currency currency,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
    public static BankAccountResponse fromDomain(BankAccount account) {
        return new BankAccountResponse(
                account.getAccountNumber(),
                account.getSortCode(),
                account.getName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedTimestamp(),
                account.getUpdatedTimestamp()
        );
    }

    public record ListBankAccountsResponse(List<BankAccountResponse> accounts) {
    }
}
