package com.eaglebank.web.dto;

import com.eaglebank.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to open a new bank account")
public record CreateBankAccountRequest(
        @Schema(example = "Personal Bank Account")
        @NotBlank(message = "name is required")
        String name,
        @Schema(example = "personal")
        @NotNull(message = "accountType is required")
        AccountType accountType
) {
}
