package com.eaglebank.acceptance.dsl;

import com.eaglebank.acceptance.driver.EagleBankApiProtocolDriver;
import com.eaglebank.acceptance.utils.Params;

public class TransactionsDsl {

    private final Params.DslContext context;
    private final EagleBankApiProtocolDriver driver;

    public TransactionsDsl(Params.DslContext context, EagleBankApiProtocolDriver driver) {
        this.context = context;
        this.driver = driver;
    }

    public void deposit(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String amount = params.optional("amount", "100.00");
        String currency = params.optional("currency", "GBP");
        String reference = params.optional("reference", "Deposit");
        String transaction = params.alias("transaction", reference);

        driver.deposit(customer, account, amount, currency, reference, transaction);
    }

    public void withdraw(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String amount = params.optional("amount", "50.00");
        String currency = params.optional("currency", "GBP");
        String reference = params.optional("reference", "Withdrawal");
        String transaction = params.alias("transaction", reference);

        driver.withdraw(customer, account, amount, currency, reference, transaction);
    }

    public void confirmTransactionRecorded(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String type = params.optional("type", "deposit");
        String amount = params.optional("amount", "100.00");
        String reference = params.optional("reference", "Deposit");

        driver.confirmTransactionRecorded(customer, account, type, amount, reference);
    }

    public void confirmTransactionsListed(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        int expectedCount = Integer.parseInt(params.optional("expectedCount", "1"));

        driver.confirmTransactionsListed(customer, account, expectedCount);
    }

    public void confirmTransactionDetails(String... args) {
        Params params = new Params(context, args);
        String customer = params.alias("customer", "Alice");
        String account = params.alias("account", "PrimaryAccount");
        String transaction = params.alias("transaction", "Tx1");
        String type = params.optional("type", "");
        String amount = params.optional("amount", "");

        driver.confirmTransactionDetails(customer, account, transaction, type, amount);
    }
}
