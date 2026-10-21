package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test suite of the class: Address")
class AddressTest {

    @Test
    @DisplayName("Should instantiate Address value object and evaluate structural equality")
    void givenAddressFields_whenNewAddress_thenShouldMatchValueEquality() {
        // given
        Address address1 = new Address("1 Churchill Place", "Canary Wharf", "Floor 10", "London", "Greater London", "E14 5HP");
        Address address1Copy = new Address("1 Churchill Place", "Canary Wharf", "Floor 10", "London", "Greater London", "E14 5HP");
        Address address2 = new Address("221B Baker St", null, null, "London", "Greater London", "NW1 6XE");

        // when
        // then
        assertEquals("1 Churchill Place", address1.getLine1());
        assertEquals("Canary Wharf", address1.getLine2());
        assertEquals("Floor 10", address1.getLine3());
        assertEquals("London", address1.getTown());
        assertEquals("Greater London", address1.getCounty());
        assertEquals("E14 5HP", address1.getPostcode());
        assertEquals(address1, address1);
        assertEquals(address1, address1Copy);
        assertEquals(address1.hashCode(), address1Copy.hashCode());
        assertFalse(address1.equals(address2));
    }
}
