package com.eaglebank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

import static com.eaglebank.domain.Money.ROUNDING_MODE;
import static com.eaglebank.domain.Money.SCALE;

@Entity
@Table(
        name = "bank_accounts",
        indexes = {
                @Index(name = "idx_bank_accounts_user_id", columnList = "user_id")
        }
)
public class BankAccount {
    public static final String DEFAULT_SORT_CODE = "10-10-10";

    @Id
    @Column(name = "account_number", nullable = false, updatable = false, length = 16)
    private String accountNumber;

    @Column(name = "sort_code", nullable = false, updatable = false, length = 16)
    private String sortCode;

    @Column(name = "user_id", nullable = false, updatable = false, length = 32)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 32)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 8)
    private Currency currency;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private Instant updatedTimestamp;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected BankAccount() {
    }

    public BankAccount(
            String accountNumber,
            String sortCode,
            String userId,
            String name,
            AccountType accountType,
            BigDecimal balance,
            Currency currency,
            Instant createdTimestamp,
            Instant updatedTimestamp
    ) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.userId = userId;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
    }

    public Money getMoneyBalance() {
        return new Money(balance, currency);
    }

    public void updateBalance(Money newBalance, Instant updatedAt) {
        this.balance = newBalance.amount();
        this.updatedTimestamp = updatedAt;
    }

    public void updateDetails(String name, AccountType accountType, Instant updatedAt) {
        if (name != null) {
            this.name = name;
        }
        if (accountType != null) {
            this.accountType = accountType;
        }
        this.updatedTimestamp = updatedAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Instant getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BankAccount that)) return false;

        return accountNumber.equals(that.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }
}
