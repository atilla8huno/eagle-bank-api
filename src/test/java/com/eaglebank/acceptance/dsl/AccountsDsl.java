package com.eaglebank.acceptance.dsl;

import com.eaglebank.acceptance.driver.EagleBankApiProtocolDriver;
import com.eaglebank.acceptance.utils.Params;

public class AccountsDsl {

    private final Params.DslContext context;
    private final EagleBankApiProtocolDriver driver;

    public AccountsDsl(Params.DslContext context, EagleBankApiProtocolDriver driver) {
        this.context = context;
        this.driver = driver;
    }

    public void openBankAccount(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String accountType = params.optional("accountType", "personal");

        driver.openBankAccount(customer, account, accountType);
    }

    public void openBankAccountWithoutRequiredDetails(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");

        driver.openBankAccountWithoutRequiredDetails(customer);
    }

    public void confirmAccountBalance(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String balance = params.optional("balance", "0.00");
        String currency = params.optional("currency", "GBP");

        driver.confirmAccountBalance(customer, account, balance, currency);
    }

    public void confirmAccountListed(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");

        driver.confirmAccountListed(customer, account);
    }
}
