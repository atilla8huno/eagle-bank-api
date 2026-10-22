package com.eaglebank.security;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test suite of the class: Permission")
class PermissionTest {

    @Test
    @DisplayName("Should map CUSTOMER role to all required OAuth2 scopes and authorities")
    void givenCustomerRole_whenInspectingPermissions_thenShouldContainAllScopesAndAuthorities() {
        //given
        Permission.Role role = Permission.Role.CUSTOMER;

        //when
        String scopeClaim = role.toScopeClaim();
        var authorities = role.getAuthorities();

        //then
        assertEquals(6, role.getPermissions().size());
        assertTrue(scopeClaim.contains("accounts:read"));
        assertTrue(scopeClaim.contains("transactions:write"));
        assertEquals(7, authorities.size());
        assertTrue(Permission.allCustomerScopes().contains("users:read"));
    }
}
