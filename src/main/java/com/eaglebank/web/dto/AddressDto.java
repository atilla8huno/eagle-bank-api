package com.eaglebank.web.dto;

import com.eaglebank.domain.Address;

public record AddressDto(
        String line1,
        String line2,
        String line3,
        String town,
        String county,
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
