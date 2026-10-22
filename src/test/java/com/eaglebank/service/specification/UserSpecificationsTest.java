package com.eaglebank.service.specification;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import com.eaglebank.web.exception.DomainExceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test suite of the class: UserSpecifications")
class UserSpecificationsTest {

    @Test
    @DisplayName("Should evaluate user ownership specification and enforce assertOwnedBy")
    void givenUser_whenEvaluatingOwnership_thenShouldEnforceOwnerRule() {
        //given
        Instant now = Instant.parse("2026-10-22T12:00:00Z");
        User user = new User(
                "usr-owner",
                "Alice Smith",
                new Address("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP"),
                "+447700900123",
                "alice@eaglebank.com",
                "hash",
                now,
                now
        );

        //when
        Executable assertOwner = () -> UserSpecifications.assertOwnedBy(user, "usr-owner", "Forbidden");
        Executable assertIntruder = () -> UserSpecifications.assertOwnedBy(user, "usr-intruder", "Forbidden");

        //then
        assertTrue(UserSpecifications.isOwnedBy("usr-owner").isSatisfiedBy(user));
        assertFalse(UserSpecifications.isOwnedBy("usr-intruder").isSatisfiedBy(user));
        assertFalse(UserSpecifications.isOwnedBy("usr-owner").isSatisfiedBy(null));
        assertDoesNotThrow(assertOwner);
        assertThrows(DomainExceptions.ForbiddenOperationException.class, assertIntruder);
    }
}
