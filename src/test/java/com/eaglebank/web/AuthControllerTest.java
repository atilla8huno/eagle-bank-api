package com.eaglebank.web;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.BankTransactionRepository;
import com.eaglebank.repository.IdempotencyRepository;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Test suite of the class: AuthController")
class AuthControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    @Autowired
    private MockMvc client;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        idempotencyRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should issue JWT Bearer token on POST /v1/auth/token when userId or email credentials are valid")
    void givenSeededUser_whenIssueToken_thenShouldReturnSignedJwtToken() throws Exception {
        //given
        userRepository.saveAndFlush(new User(
                "usr-alice1",
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                "alice@eaglebank.com",
                passwordEncoder.encode("Barclays2026!"),
                FIXED_NOW,
                FIXED_NOW
        ));

        String byUserIdBody = """
                {
                  "username": "usr-alice1",
                  "password": "Barclays2026!"
                }
                """;
        String byEmailBody = """
                {
                  "username": "alice@eaglebank.com",
                  "password": "Barclays2026!"
                }
                """;

        //when & then
        client.perform(post("/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content(byUserIdBody)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        client.perform(post("/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content(byEmailBody)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized on POST /v1/auth/token when credentials are invalid")
    void givenInvalidCredentials_whenIssueToken_thenShouldReturnUnauthorized() throws Exception {
        //given
        userRepository.saveAndFlush(new User(
                "usr-alice1",
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                "alice@eaglebank.com",
                passwordEncoder.encode("Barclays2026!"),
                FIXED_NOW,
                FIXED_NOW
        ));

        //when & then
        client.perform(post("/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usr-alice1",
                                  "password": "wrong-password"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid user credentials supplied"));

        client.perform(post("/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usr-doesnotexist",
                                  "password": "wrong"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid user credentials supplied"));
    }

    @Test
    @DisplayName("Should expose OpenAPI specification at /v3/api-docs")
    void givenOpenApiEndpoint_whenGetApiDocs_thenShouldReturnSpecification() throws Exception {
        //given & when
        client.perform(get("/v3/api-docs").accept(APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/v1/accounts/{accountNumber}/transactions']").exists());
    }
}
