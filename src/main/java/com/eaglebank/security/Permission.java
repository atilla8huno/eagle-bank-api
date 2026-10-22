package com.eaglebank.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Permission {

    USERS_READ("users:read"),
    USERS_WRITE("users:write"),
    ACCOUNTS_READ("accounts:read"),
    ACCOUNTS_WRITE("accounts:write"),
    TRANSACTIONS_READ("transactions:read"),
    TRANSACTIONS_WRITE("transactions:write");

    private final String scope;

    Permission(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public String getAuthority() {
        return "SCOPE_" + scope;
    }

    public enum Role {
        CUSTOMER(Set.of(
                USERS_READ,
                USERS_WRITE,
                ACCOUNTS_READ,
                ACCOUNTS_WRITE,
                TRANSACTIONS_READ,
                TRANSACTIONS_WRITE
        ));

        private final Set<Permission> permissions;

        Role(Set<Permission> permissions) {
            this.permissions = permissions;
        }

        public Set<Permission> getPermissions() {
            return permissions;
        }

        public String toScopeClaim() {
            return permissions.stream()
                    .map(Permission::getScope)
                    .sorted()
                    .collect(Collectors.joining(" "));
        }

        public List<GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = permissions.stream()
                    .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getAuthority()))
                    .collect(Collectors.toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + name()));
            return authorities;
        }
    }

    public static String allCustomerScopes() {
        return Arrays.stream(values())
                .map(Permission::getScope)
                .collect(Collectors.joining(" "));
    }
}
