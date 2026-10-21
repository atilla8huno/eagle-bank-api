package com.eaglebank.domain;

import java.time.Instant;

public class IdempotencyRecord {

    private Long id;
    private String accountNumber;
    private String idempotencyKey;
    private String transactionId;
    private Instant createdTimestamp;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String accountNumber, String idempotencyKey, String transactionId, Instant createdTimestamp) {
        this.accountNumber = accountNumber;
        this.idempotencyKey = idempotencyKey;
        this.transactionId = transactionId;
        this.createdTimestamp = createdTimestamp;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }
}
