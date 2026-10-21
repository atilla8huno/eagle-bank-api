package com.eaglebank.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload to register a new Eagle Bank user")
public record CreateUserRequest(
        @Schema(example = "Test User")
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "address is required")
        @Valid
        AddressDto address,
        @Schema(example = "+447700900000", pattern = "^\\+[1-9]\\d{1,14}$")
        @NotBlank(message = "phoneNumber is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phoneNumber must be in E.164 format (e.g. +447700900000)")
        String phoneNumber,
        @Schema(example = "test.user@eaglebank.com")
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,
        @Schema(description = "Optional user password for OAuth2 authentication (defaults to default secret if omitted for OpenAPI backwards-compatibility)", example = "Barclays2026!")
        String password
) {
}
