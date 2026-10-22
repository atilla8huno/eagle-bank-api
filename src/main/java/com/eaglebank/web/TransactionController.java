package com.eaglebank.web;

import com.eaglebank.service.TransactionService;
import com.eaglebank.web.dto.CreateTransactionRequest;
import com.eaglebank.web.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(
        value = "/v1/accounts/{accountNumber}/transactions",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "transaction", description = "Manage transactions on a bank account")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable("accountNumber")
            @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match pattern ^01\\d{6}$")
            String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request,
            @Parameter(description = "Optional Idempotency-Key header")
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TransactionResponse response = transactionService.createTransaction(
                accountNumber,
                jwt.getSubject(),
                request,
                resolveIdempotencyKey(idempotencyKey)
        );
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List transactions")
    public ResponseEntity<TransactionResponse.ListTransactionsResponse> listAccountTransactions(
            @PathVariable("accountNumber")
            @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match pattern ^01\\d{6}$")
            String accountNumber,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(transactionService.listAccountTransactions(accountNumber, jwt.getSubject()));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Fetch transaction by ID")
    public ResponseEntity<TransactionResponse> fetchAccountTransactionById(
            @PathVariable("accountNumber")
            @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match pattern ^01\\d{6}$")
            String accountNumber,
            @PathVariable("transactionId")
            @Pattern(regexp = "^tan-[A-Za-z0-9]+$", message = "transactionId must match pattern ^tan-[A-Za-z0-9]+$")
            String transactionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                transactionService.fetchAccountTransactionById(accountNumber, transactionId, jwt.getSubject())
        );
    }

    private String resolveIdempotencyKey(String idempotencyKey) {
        return (idempotencyKey == null || idempotencyKey.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKey.trim();
    }
}
