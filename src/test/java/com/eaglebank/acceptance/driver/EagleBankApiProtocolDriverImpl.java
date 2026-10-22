package com.eaglebank.acceptance.driver;

import com.eaglebank.acceptance.utils.Params;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class EagleBankApiProtocolDriverImpl implements EagleBankApiProtocolDriver {

    private final Params.DslContext context;
    private final MockMvc client;
    private final ObjectMapper objectMapper;
    private final Map<String, String> userIdsByAlias = new HashMap<>();
    private final Map<String, String> bearerTokensByAlias = new HashMap<>();
    private final Map<String, String> accountNumbersByAlias = new HashMap<>();
    private final Map<String, String> transactionIdsByAlias = new HashMap<>();

    public EagleBankApiProtocolDriverImpl(Params.DslContext context, MockMvc client, ObjectMapper objectMapper) {
        this.context = context;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerAndAuthenticateCustomer(String customerAlias, String phoneNumber, String postcode, String password) {
        try {
            String email = customerAlias.toLowerCase().replaceAll("[^a-z0-9]", "") + "@eaglebank.com";
            String createPayload = """
                    {
                      "name": "%s",
                      "address": {
                        "line1": "1 Churchill Place",
                        "line2": "Canary Wharf",
                        "town": "London",
                        "county": "Greater London",
                        "postcode": "%s"
                      },
                      "phoneNumber": "%s",
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(customerAlias, postcode, phoneNumber, email, password);

            MvcResult createResult = client.perform(post("/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content(createPayload)
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (createResult.getResponse().getStatus() != 201) {
                fail("Unable to register customer: " + decode(customerAlias));
            }

            JsonNode userNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
            String userId = userNode.get("id").asString();
            userIdsByAlias.put(customerAlias, userId);

            String tokenPayload = """
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(userId, password);

            MvcResult tokenResult = client.perform(post("/v1/auth/token")
                            .contentType(APPLICATION_JSON)
                            .content(tokenPayload)
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (tokenResult.getResponse().getStatus() != 200) {
                fail("Unable to authenticate customer: " + decode(customerAlias));
            }

            String accessToken = objectMapper.readTree(tokenResult.getResponse().getContentAsString())
                    .get("accessToken").asString();
            bearerTokensByAlias.put(customerAlias, "Bearer " + accessToken);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void registerCustomerWithoutRequiredDetails() {
        try {
            MvcResult result = client.perform(post("/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content("{}")
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (result.getResponse().getStatus() == 400) {
                fail("Registration was rejected because required details were missing");
            }
            fail("Expected registration to be rejected with 400 but received " + result.getResponse().getStatus());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmCustomerProfile(String actingCustomerAlias, String targetCustomerAlias) {
        try {
            String targetUserId = userIdsByAlias.getOrDefault(targetCustomerAlias, "usr-missing00");
            MvcResult result = client.perform(get("/v1/users/{userId}", targetUserId)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access profile of '" + decode(targetCustomerAlias) + "'");
            }
            if (status == 404) {
                fail("Customer profile '" + decode(targetCustomerAlias) + "' was not found");
            }
            if (status != 200) {
                fail("Expected customer profile for '" + decode(targetCustomerAlias) + "' but received status " + status);
            }

            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(targetCustomerAlias, body.get("name").asString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void openBankAccount(String customerAlias, String accountAlias, String accountType) {
        try {
            String payload = """
                    {
                      "name": "%s",
                      "accountType": "%s"
                    }
                    """.formatted(accountAlias, accountType);

            MvcResult result = client.perform(post("/v1/accounts")
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(customerAlias))
                            .contentType(APPLICATION_JSON)
                            .content(payload)
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (result.getResponse().getStatus() != 201) {
                fail("Unable to open bank account '" + decode(accountAlias) + "' for customer '" + decode(customerAlias) + "'");
            }

            String accountNumber = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("accountNumber").asString();
            accountNumbersByAlias.put(accountAlias, accountNumber);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void openBankAccountWithoutRequiredDetails(String customerAlias) {
        try {
            MvcResult result = client.perform(post("/v1/accounts")
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(customerAlias))
                            .contentType(APPLICATION_JSON)
                            .content("{}")
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (result.getResponse().getStatus() == 400) {
                fail("Opening bank account was rejected because required details were missing");
            }
            fail("Expected opening bank account to be rejected with 400 but received " + result.getResponse().getStatus());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmAccountBalance(String actingCustomerAlias, String accountAlias, String expectedBalance, String currency) {
        try {
            String accountNumber = accountNumbersByAlias.getOrDefault(accountAlias, "01999999");
            MvcResult result = client.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access bank account '" + decode(accountAlias) + "'");
            }
            if (status == 404) {
                fail("Bank account '" + decode(accountAlias) + "' was not found");
            }
            if (status != 200) {
                fail("Unable to fetch bank account '" + decode(accountAlias) + "', status: " + status);
            }

            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            BigDecimal actualBalance = new BigDecimal(body.get("balance").asString()).setScale(2);
            BigDecimal expected = new BigDecimal(expectedBalance).setScale(2);
            assertEquals(expected, actualBalance);
            assertEquals(currency, body.get("currency").asString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmAccountListed(String actingCustomerAlias, String accountAlias) {
        try {
            String expectedAccountNumber = accountNumbersByAlias.get(accountAlias);
            MvcResult result = client.perform(get("/v1/accounts")
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (result.getResponse().getStatus() != 200) {
                fail("Unable to list accounts for '" + decode(actingCustomerAlias) + "'");
            }

            JsonNode accounts = objectMapper.readTree(result.getResponse().getContentAsString()).get("accounts");
            for (JsonNode account : accounts) {
                if (expectedAccountNumber.equals(account.get("accountNumber").asString())) {
                    return;
                }
            }
            fail("Account '" + decode(accountAlias) + "' was not listed for customer '" + decode(actingCustomerAlias) + "'");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void deposit(String actingCustomerAlias, String accountAlias, String amount, String currency, String reference, String transactionAlias) {
        submitTransaction(actingCustomerAlias, accountAlias, "deposit", amount, currency, reference, transactionAlias);
    }

    @Override
    public void withdraw(String actingCustomerAlias, String accountAlias, String amount, String currency, String reference, String transactionAlias) {
        submitTransaction(actingCustomerAlias, accountAlias, "withdrawal", amount, currency, reference, transactionAlias);
    }

    @Override
    public void submitTransactionWithoutRequiredDetails(String actingCustomerAlias, String accountAlias) {
        try {
            String accountNumber = accountNumbersByAlias.getOrDefault(accountAlias, "01999999");
            MvcResult result = client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .contentType(APPLICATION_JSON)
                            .content("{}")
                            .accept(APPLICATION_JSON))
                    .andReturn();

            if (result.getResponse().getStatus() == 400) {
                fail("Transaction was rejected because required details were missing");
            }
            fail("Expected transaction to be rejected with 400 but received " + result.getResponse().getStatus());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmTransactionRecorded(String actingCustomerAlias, String accountAlias, String type, String amount, String reference) {
        try {
            String accountNumber = accountNumbersByAlias.get(accountAlias);
            MvcResult result = client.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access transactions of '" + decode(accountAlias) + "'");
            }
            if (status != 200) {
                fail("Unable to list transactions for '" + decode(accountAlias) + "'");
            }

            BigDecimal expectedAmount = new BigDecimal(amount).setScale(2);
            JsonNode transactions = objectMapper.readTree(result.getResponse().getContentAsString()).get("transactions");
            for (JsonNode tx : transactions) {
                BigDecimal txAmount = new BigDecimal(tx.get("amount").asString()).setScale(2);
                String txType = tx.get("type").asString();
                if (txType.equals(type) && txAmount.equals(expectedAmount)) {
                    return;
                }
            }
            fail("Transaction of type '" + type + "' and amount '" + amount + "' was not found on '" + decode(accountAlias) + "'");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmTransactionsListed(String actingCustomerAlias, String accountAlias, int expectedCount) {
        try {
            String accountNumber = accountNumbersByAlias.getOrDefault(accountAlias, "01999999");
            MvcResult result = client.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access transactions of '" + decode(accountAlias) + "'");
            }
            if (status == 404) {
                fail("Bank account '" + decode(accountAlias) + "' was not found");
            }
            if (status != 200) {
                fail("Unable to list transactions for '" + decode(accountAlias) + "'");
            }

            JsonNode transactions = objectMapper.readTree(result.getResponse().getContentAsString()).get("transactions");
            assertEquals(expectedCount, transactions.size());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void confirmTransactionDetails(
            String actingCustomerAlias,
            String accountAlias,
            String transactionAlias,
            String expectedType,
            String expectedAmount
    ) {
        try {
            String accountNumber = accountNumbersByAlias.getOrDefault(accountAlias, "01999999");
            String transactionId = transactionIdsByAlias.getOrDefault(transactionAlias, "tan-missing00");

            MvcResult result = client.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", accountNumber, transactionId)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access transaction '" + decode(transactionAlias) + "' on '" + decode(accountAlias) + "'");
            }
            if (status == 404) {
                JsonNode errorBody = objectMapper.readTree(result.getResponse().getContentAsString());
                if ("Bank account was not found".equals(errorBody.get("message").asString())) {
                    fail("Bank account '" + decode(accountAlias) + "' was not found");
                }
                fail("Transaction '" + decode(transactionAlias) + "' was not found on '" + decode(accountAlias) + "'");
            }
            if (status != 200) {
                fail("Unexpected status fetching transaction: " + status);
            }

            JsonNode tx = objectMapper.readTree(result.getResponse().getContentAsString());
            if (expectedType != null && !expectedType.isEmpty()) {
                assertEquals(expectedType, tx.get("type").asString());
            }
            if (expectedAmount != null && !expectedAmount.isEmpty()) {
                assertEquals(new BigDecimal(expectedAmount).setScale(2), new BigDecimal(tx.get("amount").asString()).setScale(2));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void submitTransaction(
            String actingCustomerAlias,
            String accountAlias,
            String type,
            String amount,
            String currency,
            String reference,
            String transactionAlias
    ) {
        try {
            String accountNumber = accountNumbersByAlias.getOrDefault(accountAlias, "01999999");
            String payload = """
                    {
                      "amount": %s,
                      "currency": "%s",
                      "type": "%s",
                      "reference": "%s"
                    }
                    """.formatted(amount, currency, type, reference);

            MvcResult result = client.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .contentType(APPLICATION_JSON)
                            .content(payload)
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 422) {
                fail("Insufficient funds in '" + decode(accountAlias) + "' to process withdrawal of " + amount);
            }
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to transact on '" + decode(accountAlias) + "'");
            }
            if (status == 404) {
                fail("Bank account '" + decode(accountAlias) + "' was not found");
            }
            if (status != 201) {
                fail("Unexpected status creating transaction: " + status);
            }

            if (transactionAlias != null && !transactionAlias.isEmpty()) {
                String txId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();
                transactionIdsByAlias.put(transactionAlias, txId);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String decode(String alias) {
        Params params = new Params(context);
        String decoded = params.decodeAlias(alias);
        return decoded.isEmpty() ? alias : decoded;
    }
}
