package com.eaglebank.web.exception;

import com.eaglebank.web.dto.ErrorDtos.BadRequestErrorResponse;
import com.eaglebank.web.dto.ErrorDtos.ErrorResponse;
import com.eaglebank.web.dto.ErrorDtos.ValidationErrorDetail;
import com.eaglebank.web.exception.DomainExceptions.AuthenticationFailedException;
import com.eaglebank.web.exception.DomainExceptions.ConflictException;
import com.eaglebank.web.exception.DomainExceptions.ForbiddenOperationException;
import com.eaglebank.web.exception.DomainExceptions.InsufficientFundsException;
import com.eaglebank.web.exception.DomainExceptions.InvalidTransactionException;
import com.eaglebank.web.exception.DomainExceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorDetail(
                        error.getField(),
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        error.getCode() != null ? error.getCode() : "ValidationError"
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("Invalid details supplied", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BadRequestErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationErrorDetail> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationErrorDetail(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("The request didn't supply all the necessary data", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BadRequestErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        List<ValidationErrorDetail> details = List.of(
                new ValidationErrorDetail("body", "Malformed JSON or invalid enum value", "JsonParseError")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("Invalid details supplied", details));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({InsufficientFundsException.class, InvalidTransactionException.class})
    public ResponseEntity<ErrorResponse> handleUnprocessableEntity(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}
