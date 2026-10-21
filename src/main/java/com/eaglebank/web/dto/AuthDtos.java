package com.eaglebank.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    @Schema(description = "OAuth2 Token request credentials (supports userId or email)")
    public record AuthTokenRequest(
            @Schema(description = "User ID (usr-...) or registered email address", example = "test.user@eaglebank.com")
            @NotBlank(message = "username (userId or email) is required")
            String username,
            @Schema(description = "User password (optional if user was created with default OpenAPI payload)", example = "Barclays2026!")
            String password
    ) {
    }

    @Schema(description = "OAuth2 Bearer Access Token response")
    public record AuthTokenResponse(
            @Schema(description = "Signed JWT Bearer token")
            String accessToken,
            @Schema(example = "Bearer")
            String tokenType,
            @Schema(example = "3600")
            long expiresIn
    ) {
    }
}
