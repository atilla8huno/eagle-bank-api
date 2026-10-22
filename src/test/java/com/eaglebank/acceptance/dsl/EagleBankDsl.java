package com.eaglebank.acceptance.dsl;


import com.eaglebank.acceptance.driver.EagleBankApiProtocolDriver;
import com.eaglebank.acceptance.driver.EagleBankApiProtocolDriverImpl;
import com.eaglebank.acceptance.utils.Params;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

public class EagleBankDsl {

    private final Params.DslContext context = new Params.DslContext();
    private final UsersDsl users;
    private final AccountsDsl accounts;
    private final TransactionsDsl transactions;

    public EagleBankDsl(MockMvc client, ObjectMapper objectMapper) {
        EagleBankApiProtocolDriver driver = new EagleBankApiProtocolDriverImpl(context, client, objectMapper);
        this.users = new UsersDsl(context, driver);
        this.accounts = new AccountsDsl(context, driver);
        this.transactions = new TransactionsDsl(context, driver);
    }

    public UsersDsl users() {
        return users;
    }

    public AccountsDsl accounts() {
        return accounts;
    }

    public TransactionsDsl transactions() {
        return transactions;
    }
}
