package com.eaglebank.acceptance.specification.user;

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
@SelectClasspathResource("features/user_management.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.eaglebank.acceptance.specification.user")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class UserManagementFeatureTest {

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

    @When("a visitor registers as customer {string}")
    public void aVisitorRegistersAsCustomer(String customer) {
        dsl.users().registerCustomer("customer: " + customer);
    }

    @When("{string} attempts to view the profile of {string}")
    public void attemptsToViewProfile(String customer, String target) {
        try {
            dsl.users().confirmCustomerProfile("customer: " + customer, "target: " + target);
        } catch (AssertionFailedError e) {
            rejection = e;
        }
    }

    @Then("{string} can view the profile of {string}")
    public void canViewProfile(String customer, String target) {
        dsl.users().confirmCustomerProfile("customer: " + customer, "target: " + target);
    }

    @Then("the profile request is rejected with {string}")
    public void theProfileRequestIsRejectedWith(String message) {
        assertNotNull(rejection, "Expected the profile request to be rejected");
        assertEquals(message, rejection.getMessage());
    }
}
