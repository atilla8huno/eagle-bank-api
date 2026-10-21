package com.eaglebank.web.dto;

import com.eaglebank.domain.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        AddressDto address,
        String phoneNumber,
        String email,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                AddressDto.fromDomain(user.getAddress()),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }
}
