package com.eaglebank.web.dto;

import com.eaglebank.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User profile details")
public record UserResponse(
        @Schema(example = "usr-abc12345")
        String id,
        @Schema(example = "Test User")
        String name,
        AddressDto address,
        @Schema(example = "+447700900000")
        String phoneNumber,
        @Schema(example = "test.user@eaglebank.com")
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
