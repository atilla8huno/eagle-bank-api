package com.eaglebank.acceptance.driver;

public interface EagleBankApiProtocolDriver {

    void registerAndAuthenticateCustomer(String customerAlias, String phoneNumber, String postcode, String password);

    void confirmCustomerProfile(String actingCustomerAlias, String targetCustomerAlias);

    void openBankAccount(String customerAlias, String accountAlias, String accountType);

    void confirmAccountBalance(String actingCustomerAlias, String accountAlias, String expectedBalance, String currency);

    void confirmAccountListed(String actingCustomerAlias, String accountAlias);

}
