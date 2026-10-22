package com.eaglebank.acceptance.driver;

import com.eaglebank.acceptance.utils.Params;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
    public void confirmCustomerProfile(String actingCustomerAlias, String targetCustomerAlias) {
        try {
            String targetUserId = userIdsByAlias.get(targetCustomerAlias);
            MvcResult result = client.perform(get("/v1/users/{userId}", targetUserId)
                            .header(HttpHeaders.AUTHORIZATION, bearerTokensByAlias.get(actingCustomerAlias))
                            .accept(APPLICATION_JSON))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 403) {
                fail("Customer '" + decode(actingCustomerAlias) + "' is not allowed to access profile of '" + decode(targetCustomerAlias) + "'");
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

    private String decode(String alias) {
        Params params = new Params(context);
        String decoded = params.decodeAlias(alias);
        return decoded.isEmpty() ? alias : decoded;
    }
}
