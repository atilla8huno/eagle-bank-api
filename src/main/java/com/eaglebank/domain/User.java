package com.eaglebank.domain;

import java.time.Instant;

public class User {
    private String id;
    private String name;
    private Address address;
    private String phoneNumber;
    private String email;
    private String passwordHash;
    private Instant createdTimestamp;
    private Instant updatedTimestamp;

    protected User() {
    }

    public User(
            String id,
            String name,
            Address address,
            String phoneNumber,
            String email,
            String passwordHash,
            Instant createdTimestamp,
            Instant updatedTimestamp
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
    }

    public void updateDetails(String name, Address address, String phoneNumber, String email, Instant updatedAt) {
        if (name != null) {
            this.name = name;
        }
        if (address != null) {
            this.address = address;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (email != null) {
            this.email = email;
        }
        this.updatedTimestamp = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Instant getUpdatedTimestamp() {
        return updatedTimestamp;
    }
}
