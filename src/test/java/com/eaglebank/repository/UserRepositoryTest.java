package com.eaglebank.repository;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Test suite of the class: UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save user and find by id")
    void givenUserEntity_whenSaveAndFindById_thenShouldPersistAndFindUser() {
        //given
        Instant now = Instant.parse("2026-10-21T09:00:00Z");
        Address address = new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP");
        User user = new User("usr-h2100", "Atilla Barros", address, "+447700900100", "atilla@eaglebank.com", "bcrypt-hash", now, now);

        //when
        repository.saveAndFlush(user);
        entityManager.clear();

        Optional<User> foundById = repository.findById("usr-h2100");

        //then
        assertTrue(foundById.isPresent());
        assertEquals("Atilla Barros", foundById.get().getName());
        assertEquals("1 Churchill Place", foundById.get().getAddress().getLine1());
    }

    @Test
    @DisplayName("Should find user by email case-insensitively")
    void givenExistingUser_whenFindFirstByEmailIgnoreCase_thenShouldReturnUser() {
        //given
        Instant now = Instant.parse("2026-10-21T09:00:00Z");
        Address address = new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP");
        User user = new User("usr-h2101", "Atilla Barros", address, "+447700900100", "Atilla.Barros@EagleBank.com", "bcrypt-hash", now, now);
        repository.saveAndFlush(user);
        entityManager.clear();

        //when
        Optional<User> foundByEmail = repository.findFirstByEmailIgnoreCase("atilla.barros@eaglebank.com");

        //then
        assertTrue(foundByEmail.isPresent());
        assertEquals("usr-h2101", foundByEmail.get().getId());
    }
}
