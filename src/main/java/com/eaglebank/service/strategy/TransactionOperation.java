package com.eaglebank.service.strategy;

import com.eaglebank.domain.Money;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.service.specification.AccountSpecifications;

public sealed interface TransactionOperation
        permits TransactionOperation.Deposit, TransactionOperation.Withdrawal {

    Money apply(Money currentBalance, Money transactionAmount);

    static TransactionOperation forType(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> new Deposit();
            case WITHDRAWAL -> new Withdrawal();
        };
    }

    record Deposit() implements TransactionOperation {
        @Override
        public Money apply(Money currentBalance, Money transactionAmount) {
            Money updated = currentBalance.add(transactionAmount);
            AccountSpecifications.assertWithinMaxLimit(updated);
            return updated;
        }
    }

    record Withdrawal() implements TransactionOperation {
        @Override
        public Money apply(Money currentBalance, Money transactionAmount) {
            AccountSpecifications.assertSufficientFunds(currentBalance, transactionAmount);
            return currentBalance.subtract(transactionAmount);
        }
    }
}
