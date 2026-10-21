package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test suite of the class: User")
class UserTest {

    @Test
    @DisplayName("Should instantiate and update User entity")
    void givenUserParameters_whenNewAndUpdate_thenShouldMaintainState() {
        //given
        Instant t1 = Instant.parse("2026-10-20T10:00:00Z");
        Instant t2 = Instant.parse("2026-10-20T11:00:00Z");
        Address addr1 = new Address("1 Churchill Place", "Canary Wharf", "Floor 10", "London", "Greater London", "E14 5HP");
        Address addr2 = new Address("221B Baker St", null, null, "London", "Greater London", "NW1 6XE");

        //when
        User user = new User("usr-abc12345", "Atilla Barros", addr1, "+447700900111", "atilla@eaglebank.com", "hash123", t1, t1);
        user.updateDetails(null, null, null, null, t2);
        user.updateDetails("Atilla Updated", addr2, "+447700900222", "atilla2@eaglebank.com", t2);

        //then
        assertEquals("usr-abc12345", user.getId());
        assertEquals("Atilla Updated", user.getName());
        assertEquals(addr2, user.getAddress());
        assertEquals("+447700900222", user.getPhoneNumber());
        assertEquals("atilla2@eaglebank.com", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals(t1, user.getCreatedTimestamp());
        assertEquals(t2, user.getUpdatedTimestamp());
    }
}
