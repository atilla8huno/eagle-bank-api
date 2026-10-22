package com.eaglebank;

import com.eaglebank.web.AccountController;
import com.eaglebank.web.AuthController;
import com.eaglebank.web.TransactionController;
import com.eaglebank.web.UserController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Test suite of the class: EagleBankApiApplication")
class EagleBankApiApplicationTest {

    @Autowired
    private UserController userController;

    @Autowired
    private AuthController authController;

    @Autowired
    private AccountController accountController;

    @Autowired
    private TransactionController transactionController;

    @Test
    void givenSpringContext_whenAutowiring_thenShouldInjectFromContext() {
        assertNotNull(userController);
        assertNotNull(authController);
        assertNotNull(accountController);
        assertNotNull(transactionController);
    }
}
