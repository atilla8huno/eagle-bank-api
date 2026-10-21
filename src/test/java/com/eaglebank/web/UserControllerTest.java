package com.eaglebank.web;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import com.eaglebank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Test suite of the class: UserController")
class UserControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    @Autowired
    private MockMvc client;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a new user on POST /v1/users and persist in database")
    void givenValidRequest_whenCreateUser_thenShouldPersistAndReturnCreatedUser() throws Exception {
        //given
        String body = """
                {
                  "name": "Alice Smith",
                  "address": {
                    "line1": "1 Churchill Place",
                    "line2": "Canary Wharf",
                    "town": "London",
                    "county": "Greater London",
                    "postcode": "E14 5HP"
                  },
                  "phoneNumber": "+447700900123",
                  "email": "alice@eaglebank.com"
                }
                """;

        //when
        client.perform(post("/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", matchesPattern("^usr-[A-Za-z0-9]+$")))
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@eaglebank.com"));

        Optional<User> savedUser = userRepository.findFirstByEmailIgnoreCase("alice@eaglebank.com");
        assertTrue(savedUser.isPresent());
        assertEquals("Alice Smith", savedUser.get().getName());
        assertNotNull(savedUser.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should fetch user by ID on GET /v1/users/{userId}")
    void givenSeededUser_whenFetchUserByIdAsOwner_thenShouldReturnUser() throws Exception {
        //given
        String userId = "usr-alice1";
        userRepository.save(dummyUser(userId, "alice@eaglebank.com"));

        //when
        client.perform(get("/v1/users/{userId}", userId)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    @DisplayName("Should NOT fetch user on GET /v1/users/{userId} when user does not exist")
    void givenNonExistentUser_whenFetchUserById_thenShouldReturnNotFound() throws Exception {
        //given
        String missingUserId = "usr-missing99";

        //when
        client.perform(get("/v1/users/{userId}", missingUserId)
                        .accept(APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User was not found"));
    }

    private User dummyUser(String userId, String email) {
        return new User(
                userId,
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                email,
                "$2a$10$dummyBcryptHashValueForTesting12345678901234567890",
                FIXED_NOW,
                FIXED_NOW
        );
    }
}
