package com.eaglebank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Address {

    @Column(name = "address_line1", nullable = false)
    private String line1;

    @Column(name = "address_line2")
    private String line2;

    @Column(name = "address_line3")
    private String line3;

    @Column(name = "address_town", nullable = false)
    private String town;

    @Column(name = "address_county")
    private String county;

    @Column(name = "address_postcode", nullable = false)
    private String postcode;

    public Address(String line1, String line2, String line3, String town, String county, String postcode) {
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
    }

    protected Address() {
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    public String getTown() {
        return town;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address that)) {
            return false;
        }
        return Objects.equals(line1, that.line1)
                && Objects.equals(line2, that.line2)
                && Objects.equals(line3, that.line3)
                && Objects.equals(town, that.town)
                && Objects.equals(county, that.county)
                && Objects.equals(postcode, that.postcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, line3, town, county, postcode);
    }
}
