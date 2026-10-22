package com.eaglebank.acceptance.driver;

public interface EagleBankApiProtocolDriver {

    void registerAndAuthenticateCustomer(String customerAlias, String phoneNumber, String postcode, String password);

    void confirmCustomerProfile(String actingCustomerAlias, String targetCustomerAlias);
}
