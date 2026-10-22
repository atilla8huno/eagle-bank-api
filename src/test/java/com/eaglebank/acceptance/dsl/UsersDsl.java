package com.eaglebank.acceptance.dsl;

import com.eaglebank.acceptance.driver.EagleBankApiProtocolDriver;
import com.eaglebank.acceptance.utils.Params;

public class UsersDsl {

    private final Params.DslContext context;
    private final EagleBankApiProtocolDriver driver;

    public UsersDsl(Params.DslContext context, EagleBankApiProtocolDriver driver) {
        this.context = context;
        this.driver = driver;
    }

    public void registerCustomer(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String phoneNumber = params.optional("phoneNumber", "+447700900123");
        String postcode = params.optional("postcode", "E14 5HP");
        String password = params.optional("password", "Barclays2026!");

        driver.registerAndAuthenticateCustomer(customer, phoneNumber, postcode, password);
    }

    public void confirmCustomerProfile(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String target = params.alias("target", params.decodeAlias(customer));

        driver.confirmCustomerProfile(customer, target);
    }
}
