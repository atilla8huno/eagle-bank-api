package com.eaglebank.web;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.security.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Test suite of the class: AccountController")
public class AccountControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    @Autowired
    private MockMvc client;

    @Autowired
    private BankAccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a bank account on POST /v1/accounts and persist in database")
    void givenValidRequest_whenCreateAccount_thenShouldPersistAndReturnCreatedAccount() throws Exception {
        //given
        String userId = "usr-alice1";
        String body = """
                {
                  "name": "Everyday Account",
                  "accountType": "personal"
                }
                """;

        //when
        client.perform(post("/v1/accounts")
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", matchesPattern("^01\\d{6}$")))
                .andExpect(jsonPath("$.sortCode").value("10-10-10"))
                .andExpect(jsonPath("$.balance").value(0.0));

        List<BankAccount> savedAccounts = accountRepository.findAllByUserIdOrderByCreatedTimestampAsc(userId);
        assertEquals(1, savedAccounts.size());
        assertEquals("Everyday Account", savedAccounts.getFirst().getName());
        assertEquals(new BigDecimal("0.00"), savedAccounts.getFirst().getBalance());
        assertNotNull(savedAccounts.getFirst().getVersion());
    }

    @Test
    @DisplayName("Should NOT create bank account on POST /v1/accounts when payload or enum is invalid")
    void givenInvalidBodyOrEnum_whenCreateAccount_thenShouldReturnBadRequestAndNotPersist() throws Exception {
        //given
        String userId = "usr-alice1";
        String badEnumBody = """
                {
                  "name": "Bad Enum Account",
                  "accountType": "unsupported_type"
                }
                """;

        //when & then
        client.perform(post("/v1/accounts")
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid details supplied"))
                .andExpect(jsonPath("$.details").isArray());

        client.perform(post("/v1/accounts")
                        .with(customerJwt(userId))
                        .contentType(APPLICATION_JSON)
                        .content(badEnumBody)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());

        assertEquals(0, accountRepository.count());
    }

    @Test
    @DisplayName("Should list customer bank accounts on GET /v1/accounts")
    void givenSeededAccounts_whenListAccounts_thenShouldReturnOnlyAuthenticatedUserAccounts() throws Exception {
        //given
        String userId = "usr-alice1";
        accountRepository.save(dummyAccount("01234567", userId, new BigDecimal("100.00")));
        accountRepository.save(dummyAccount("01999999", "usr-bob2", new BigDecimal("500.00")));

        //when
        client.perform(get("/v1/accounts")
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts", hasSize(1)))
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("01234567"))
                .andExpect(jsonPath("$.accounts[0].balance").value(100.0));
    }

    @Test
    @DisplayName("Should fetch bank account by accountNumber on GET /v1/accounts/{accountNumber}")
    void givenSeededAccount_whenFetchAccountByAccountNumberAsOwner_thenShouldReturnAccount() throws Exception {
        //given
        String userId = "usr-alice1";
        accountRepository.save(dummyAccount("01234567", userId, new BigDecimal("250.50")));

        //when
        client.perform(get("/v1/accounts/{accountNumber}", "01234567")
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("01234567"))
                .andExpect(jsonPath("$.balance").value(250.50));
    }

    @Test
    @DisplayName("Should enforce 401, 403 and 404 on GET and POST /v1/accounts")
    void givenUnauthenticatedForbiddenOrMissingAccount_whenCallingAccountEndpoints_thenShouldReturnExpectedStatus() throws Exception {
        //given
        String userId = "usr-alice1";
        accountRepository.save(dummyAccount("01234567", userId, new BigDecimal("0.00")));

        //when & then
        client.perform(get("/v1/accounts/{accountNumber}", "01234567")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Access token is missing or invalid"));

        client.perform(post("/v1/accounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_accounts:read")))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Account",
                                  "accountType": "personal"
                                }
                                """)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The user is not allowed to access this resource"));

        client.perform(get("/v1/accounts/{accountNumber}", "01234567")
                        .with(customerJwt("usr-bob2"))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the bank account details"));

        client.perform(get("/v1/accounts/{accountNumber}", "01999999")
                        .with(customerJwt(userId))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    private RequestPostProcessor customerJwt(String userId) {
        return jwt()
                .jwt(builder -> builder.subject(userId).claim("scope", Permission.Role.CUSTOMER.toScopeClaim()))
                .authorities(Permission.Role.CUSTOMER.getAuthorities());
    }

    private BankAccount dummyAccount(String accountNumber, String userId, BigDecimal balance) {
        return new BankAccount(
                accountNumber,
                "10-10-10",
                userId,
                "Everyday Account",
                AccountType.PERSONAL,
                balance,
                Currency.GBP,
                FIXED_NOW,
                FIXED_NOW
        );
    }
}
