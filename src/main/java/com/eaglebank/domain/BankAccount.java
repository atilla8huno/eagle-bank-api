package com.eaglebank.domain;

import java.math.BigDecimal;
import java.time.Instant;

import static com.eaglebank.domain.Money.ROUNDING_MODE;
import static com.eaglebank.domain.Money.SCALE;

public class BankAccount {
    public static final String DEFAULT_SORT_CODE = "10-10-10";

    private String accountNumber;
    private String sortCode;
    private String userId;
    private String name;
    private AccountType accountType;
    private BigDecimal balance;
    private Currency currency;
    private Instant createdTimestamp;
    private Instant updatedTimestamp;
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
}
