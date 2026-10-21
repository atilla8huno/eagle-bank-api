package com.eaglebank.web.dto;

import com.eaglebank.domain.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Physical postal address of a user")
public record AddressDto(
        @Schema(example = "10 Downing Street")
        @NotBlank(message = "line1 is required")
        String line1,
        @Schema(example = "Westminster")
        String line2,
        @Schema(example = "Central")
        String line3,
        @Schema(example = "London")
        @NotBlank(message = "town is required")
        String town,
        @Schema(example = "Greater London")
        String county,
        @Schema(example = "SW1A 2AA")
        @NotBlank(message = "postcode is required")
        String postcode
) {
    public Address toDomain() {
        return new Address(line1, line2, line3, town, county, postcode);
    }

    public static AddressDto fromDomain(Address address) {
        return new AddressDto(
                address.getLine1(),
                address.getLine2(),
                address.getLine3(),
                address.getTown(),
                address.getCounty(),
                address.getPostcode()
        );
    }
}
