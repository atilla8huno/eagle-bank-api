package com.eaglebank.web;

import com.eaglebank.service.AccountService;
import com.eaglebank.web.dto.BankAccountResponse;
import com.eaglebank.web.dto.CreateBankAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(
        value = "/v1/accounts",
        produces = APPLICATION_JSON_VALUE
)
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "account", description = "Manage a bank account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody CreateBankAccountRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(CREATED).body(accountService.createAccount(jwt.getSubject(), request));
    }

    @GetMapping
    @Operation(summary = "List accounts")
    public ResponseEntity<BankAccountResponse.ListBankAccountsResponse> listAccounts(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountService.listAccounts(jwt.getSubject()));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Fetch account by account number")
    public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(
            @PathVariable("accountNumber")
            @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match pattern ^01\\d{6}$")
            String accountNumber,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(accountService.fetchAccountByAccountNumber(accountNumber, jwt.getSubject()));
    }
}
