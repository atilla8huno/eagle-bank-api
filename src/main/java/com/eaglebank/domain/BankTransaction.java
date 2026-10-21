package com.eaglebank.domain;

import java.math.BigDecimal;
import java.time.Instant;

import static com.eaglebank.domain.Money.ROUNDING_MODE;
import static com.eaglebank.domain.Money.SCALE;

public class BankTransaction {

    private String id;
    private String accountNumber;
    private String userId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionType type;
    private String reference;
    private Instant createdTimestamp;

    protected BankTransaction() {
    }

    public BankTransaction(
            String id,
            String accountNumber,
            String userId,
            BigDecimal amount,
            Currency currency,
            TransactionType type,
            String reference,
            Instant createdTimestamp
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.type = type;
        this.reference = reference;
        this.createdTimestamp = createdTimestamp;
    }

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransactionType getType() {
        return type;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }
}
