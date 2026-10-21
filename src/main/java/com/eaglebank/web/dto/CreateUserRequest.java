package com.eaglebank.web.dto;

public record CreateUserRequest(
        String name,
        AddressDto address,
        String phoneNumber,
        String email,
        String password
) {
}
