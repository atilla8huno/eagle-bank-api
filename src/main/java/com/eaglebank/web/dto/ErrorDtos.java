package com.eaglebank.web.dto;

import java.util.List;

public final class ErrorDtos {

    private ErrorDtos() {
    }

    public record ErrorResponse(
            String message
    ) {
    }

    public record BadRequestErrorResponse(
            String message,
            List<ValidationErrorDetail> details
    ) {
    }

    public record ValidationErrorDetail(
            String field,
            String message,
            String type
    ) {
    }
}
