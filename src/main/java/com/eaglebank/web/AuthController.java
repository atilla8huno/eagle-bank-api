package com.eaglebank.web;

import com.eaglebank.service.AuthService;
import com.eaglebank.web.dto.AuthDtos.AuthTokenRequest;
import com.eaglebank.web.dto.AuthDtos.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(
        value = "/v1/auth",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
)
@Tag(name = "auth", description = "OAuth2 JWT authentication endpoint")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    @Operation(summary = "Authenticate user and issue Bearer JWT token")
    public ResponseEntity<AuthTokenResponse> issueToken(@Valid @RequestBody AuthTokenRequest request) {
        return ResponseEntity.ok(authService.issueToken(request));
    }
}
