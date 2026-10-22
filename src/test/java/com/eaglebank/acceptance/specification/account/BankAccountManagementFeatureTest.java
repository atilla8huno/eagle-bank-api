package com.eaglebank.acceptance.specification.account;

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
@SelectClasspathResource("features/bank_account_management.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.eaglebank.acceptance.specification.account")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class BankAccountManagementFeatureTest {

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

    @When("{string} opens a {string} bank account {string}")
    public void opensABankAccount(String customer, String accountType, String account) {
        dsl.accounts().openBankAccount(
                "customer: " + customer,
                "account: " + account,
                "accountType: " + accountType
        );
    }

    @When("{string} attempts to view {string}")
    public void attemptsToView(String customer, String account) {
        try {
            dsl.accounts().confirmAccountBalance("customer: " + customer, "account: " + account);
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @Then("{string} is listed for {string}")
    public void isListedFor(String account, String customer) {
        dsl.accounts().confirmAccountListed("customer: " + customer, "account: " + account);
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

    @Then("the account access is rejected with {string}")
    public void theAccountAccessIsRejectedWith(String message) {
        assertNotNull(rejection, "Expected account access to be rejected");
        assertEquals(message, rejection.getMessage());
    }
}
