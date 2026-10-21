package com.eaglebank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_idempotency_account_key", columnNames = {"account_number", "idempotency_key"})
        }
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, updatable = false, length = 16)
    private String accountNumber;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "transaction_id", nullable = false, updatable = false, length = 32)
    private String transactionId;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof IdempotencyRecord that)) return false;

        return accountNumber.equals(that.accountNumber) && idempotencyKey.equals(that.idempotencyKey);
    }

    @Override
    public int hashCode() {
        int result = accountNumber.hashCode();
        result = 31 * result + idempotencyKey.hashCode();
        return result;
    }
}
