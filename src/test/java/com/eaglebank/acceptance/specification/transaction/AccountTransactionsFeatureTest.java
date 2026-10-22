package com.eaglebank.acceptance.specification.transaction;

import com.eaglebank.acceptance.dsl.EagleBankDsl;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/account_transactions.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.eaglebank.acceptance.specification.transaction")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class AccountTransactionsFeatureTest {

    @Autowired
    private MockMvc client;

    @Autowired
    private ObjectMapper objectMapper;

    private EagleBankDsl dsl;
    private AssertionFailedError rejection;

    @Before
    public void newScenario() {
        dsl = new EagleBankDsl(client, objectMapper);
        rejection = null;
    }

    @Given("a registered customer {string}")
    public void aRegisteredCustomer(String customer) {
        dsl.users().registerCustomer("customer: " + customer);
    }

    @Given("{string} has a {string} bank account {string}")
    public void hasABankAccount(String customer, String accountType, String account) {
        dsl.accounts().openBankAccount(
                "customer: " + customer,
                "account: " + account,
                "accountType: " + accountType
        );
    }

    @When("{string} deposits {string} {string} into {string} as {string}")
    public void depositsIntoAs(String customer, String amount, String currency, String account, String transaction) {
        dsl.transactions().deposit(
                "customer: " + customer,
                "account: " + account,
                "amount: " + amount,
                "currency: " + currency,
                "transaction: " + transaction
        );
    }

    @When("{string} withdraws {string} {string} from {string} as {string}")
    public void withdrawsFromAs(String customer, String amount, String currency, String account, String transaction) {
        dsl.transactions().withdraw(
                "customer: " + customer,
                "account: " + account,
                "amount: " + amount,
                "currency: " + currency,
                "transaction: " + transaction
        );
    }

    @When("{string} attempts to withdraw {string} {string} from {string}")
    public void attemptsToWithdrawFrom(String customer, String amount, String currency, String account) {
        try {
            dsl.transactions().withdraw(
                    "customer: " + customer,
                    "account: " + account,
                    "amount: " + amount,
                    "currency: " + currency
            );
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @When("{string} attempts a transaction on {string} without required details")
    public void attemptsATransactionWithoutRequiredDetails(String customer, String account) {
        try {
            dsl.transactions().submitTransactionWithoutRequiredDetails(
                    "customer: " + customer,
                    "account: " + account
            );
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @When("{string} attempts to view transactions on {string}")
    public void attemptsToViewTransactionsOn(String customer, String account) {
        try {
            dsl.transactions().confirmTransactionsListed(
                    "customer: " + customer,
                    "account: " + account,
                    "expectedCount: 0"
            );
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @When("{string} attempts to fetch transaction {string} on {string}")
    public void attemptsToFetchTransactionOn(String customer, String transaction, String account) {
        try {
            dsl.transactions().confirmTransactionDetails(
                    "customer: " + customer,
                    "account: " + account,
                    "transaction: " + transaction
            );
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @Then("{string} sees a balance of {string} {string} in {string}")
    public void seesABalanceOf(String customer, String balance, String currency, String account) {
        dsl.accounts().confirmAccountBalance(
                "customer: " + customer,
                "account: " + account,
                "balance: " + balance,
                "currency: " + currency
        );
    }

    @Then("a {string} of {string} is recorded on {string} for {string}")
    public void aTransactionIsRecordedOn(String type, String amount, String account, String customer) {
        dsl.transactions().confirmTransactionRecorded(
                "customer: " + customer,
                "account: " + account,
                "type: " + type,
                "amount: " + amount
        );
    }

    @Then("{string} sees {int} transactions listed on {string}")
    public void seesTransactionsListedOn(String customer, int count, String account) {
        dsl.transactions().confirmTransactionsListed(
                "customer: " + customer,
                "account: " + account,
                "expectedCount: " + count
        );
    }

    @Then("{string} can fetch transaction {string} on {string} with type {string} and amount {string}")
    public void canFetchTransactionOn(String customer, String transaction, String account, String type, String amount) {
        dsl.transactions().confirmTransactionDetails(
                "customer: " + customer,
                "account: " + account,
                "transaction: " + transaction,
                "type: " + type,
                "amount: " + amount
        );
    }

    @Then("the transaction is rejected with {string}")
    public void theTransactionIsRejectedWith(String message) {
        assertNotNull(rejection, "Expected transaction to be rejected");
        assertEquals(message, rejection.getMessage());
    }
}

