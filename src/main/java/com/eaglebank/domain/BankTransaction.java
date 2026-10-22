package com.eaglebank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

import static com.eaglebank.domain.Money.ROUNDING_MODE;
import static com.eaglebank.domain.Money.SCALE;

@Entity
@Immutable
@Table(
        name = "bank_transactions",
        indexes = {
                @Index(name = "idx_tx_account_created", columnList = "account_number, created_timestamp DESC")
        }
)
public class BankTransaction {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 32)
    private String id;

    @Column(name = "account_number", nullable = false, updatable = false, length = 16)
    private String accountNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_bank_transaction_user")
    )
    private User user;

    @Column(name = "amount", nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, updatable = false, length = 8)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 16)
    private TransactionType type;

    @Column(name = "reference", updatable = false)
    private String reference;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    protected BankTransaction() {
    }

    public BankTransaction(
            String id,
            String accountNumber,
            User user,
            BigDecimal amount,
            Currency currency,
            TransactionType type,
            String reference,
            Instant createdTimestamp
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public String getUserId() {
        return user.getId();
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BankTransaction that)) return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
