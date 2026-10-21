package com.eaglebank.service.specification;

import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Money;
import com.eaglebank.web.exception.DomainExceptions.ForbiddenOperationException;
import com.eaglebank.web.exception.DomainExceptions.InsufficientFundsException;
import com.eaglebank.web.exception.DomainExceptions.InvalidTransactionException;

public final class AccountSpecifications {

    private AccountSpecifications() {
    }

    public static Specification<BankAccount> isOwnedBy(String authenticatedUserId) {
        return account -> account != null && account.getUserId().equals(authenticatedUserId);
    }

    public static Specification<Money> hasSufficientFunds(Money requestedWithdrawal) {
        return currentBalance -> !currentBalance.isLessThan(requestedWithdrawal);
    }

    public static Specification<Money> isWithinMaxAccountBalance() {
        return resultingBalance -> !resultingBalance.exceedsMaxLimit();
    }

    public static void assertOwnedBy(BankAccount account, String authenticatedUserId, String forbiddenMessage) {
        if (!isOwnedBy(authenticatedUserId).isSatisfiedBy(account)) {
            throw new ForbiddenOperationException(forbiddenMessage);
        }
    }

    public static void assertSufficientFunds(Money currentBalance, Money withdrawalAmount) {
        if (!hasSufficientFunds(withdrawalAmount).isSatisfiedBy(currentBalance)) {
            throw new InsufficientFundsException("Insufficient funds to process transaction");
        }
    }

    public static void assertWithinMaxLimit(Money resultingBalance) {
        if (!isWithinMaxAccountBalance().isSatisfiedBy(resultingBalance)) {
            throw new InvalidTransactionException("Account balance cannot exceed maximum limit of 10000.00 GBP");
        }
    }
}
