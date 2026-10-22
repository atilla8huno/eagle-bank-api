package com.eaglebank.acceptance.driver;

public interface EagleBankApiProtocolDriver {

    void registerAndAuthenticateCustomer(String customerAlias, String phoneNumber, String postcode, String password);

    void confirmCustomerProfile(String actingCustomerAlias, String targetCustomerAlias);

    void openBankAccount(String customerAlias, String accountAlias, String accountType);

    void confirmAccountBalance(String actingCustomerAlias, String accountAlias, String expectedBalance, String currency);

    void confirmAccountListed(String actingCustomerAlias, String accountAlias);

    void deposit(String actingCustomerAlias, String accountAlias, String amount, String currency, String reference, String transactionAlias);

    void withdraw(String actingCustomerAlias, String accountAlias, String amount, String currency, String reference, String transactionAlias);

    void confirmTransactionRecorded(String actingCustomerAlias, String accountAlias, String type, String amount, String reference);

    void confirmTransactionsListed(String actingCustomerAlias, String accountAlias, int expectedCount);

    void confirmTransactionDetails(String actingCustomerAlias, String accountAlias, String transactionAlias, String expectedType, String expectedAmount);
}
