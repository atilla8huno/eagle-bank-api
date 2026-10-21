package com.eaglebank.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public final class ErrorDtos {

    private ErrorDtos() {
    }

    @Schema(name = "ErrorResponse", description = "Standard error response payload")
    public record ErrorResponse(
            @Schema(example = "Resource was not found")
            String message
    ) {
    }

    @Schema(name = "BadRequestErrorResponse", description = "Validation error response with field-level details")
    public record BadRequestErrorResponse(
            @Schema(example = "Invalid details supplied")
            String message,
            List<ValidationErrorDetail> details
    ) {
    }

    @Schema(description = "Field-level validation error detail")
    public record ValidationErrorDetail(
            @Schema(example = "amount")
            String field,
            @Schema(example = "amount must be at least 0.01")
            String message,
            @Schema(example = "DecimalMin")
            String type
    ) {
    }
}
