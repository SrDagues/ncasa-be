package ncasa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ExpenseIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM expense_allocations");
        jdbc.update("DELETE FROM expenses");
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void shouldCreateRetrieveListAndVoidSharedExpense() throws Exception {
        String ownerToken = register("owner@example.com");
        String memberToken = register("member@example.com");
        JsonNode household = createHousehold(ownerToken);
        String householdId = household.get("id").asString();
        String ownerMemberId = household.get("members").get(0).get("id").asString();

        mvc.perform(post("/api/households/{id}/invitations", householdId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\",\"role\":\"MEMBER\"}"))
                .andExpect(status().isCreated());
        JsonNode pending = json.readTree(mvc.perform(get("/api/household-invitations/pending")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode accepted = json.readTree(mvc.perform(post("/api/household-invitations/{id}/accept",
                        pending.get(0).get("id").asString()).header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String memberId = accepted.get("members").get(1).get("id").asString();

        String createBody = """
                {
                  "description":"Compra semanal",
                  "amount":"10.00",
                  "currency":"EUR",
                  "expenseDate":"2026-08-20",
                  "payerMemberId":"%s",
                  "split":{"type":"EQUAL","memberIds":["%s","%s"]}
                }
                """.formatted(memberId, ownerMemberId, memberId);

        String incoherentSplit = """
                {
                  "description":"Inválido",
                  "amount":"10.00",
                  "currency":"EUR",
                  "expenseDate":"2026-08-20",
                  "payerMemberId":"%s",
                  "split":{
                    "type":"EQUAL",
                    "memberIds":["%s","%s"],
                    "allocations":[{"memberId":"%s","amount":"10.00"}]
                  }
                }
                """.formatted(memberId, ownerMemberId, memberId, memberId);
        mvc.perform(post("/api/households/{id}/expenses", householdId)
                        .header("Authorization", bearer(memberToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(incoherentSplit))
                .andExpect(status().isBadRequest());

        JsonNode created = json.readTree(mvc.perform(post("/api/households/{id}/expenses", householdId)
                        .header("Authorization", bearer(memberToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value("10.00"))
                .andExpect(jsonPath("$.allocations[0].amount").value("5.00"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString());
        String expenseId = created.get("id").asString();

        String exactBody = """
                {
                  "description":"Compra exacta",
                  "amount":"10.00",
                  "currency":"EUR",
                  "expenseDate":"2026-08-19",
                  "payerMemberId":"%s",
                  "split":{"type":"EXACT","allocations":[
                    {"memberId":"%s","amount":"4.00"},
                    {"memberId":"%s","amount":"6.00"}
                  ]}
                }
                """.formatted(memberId, ownerMemberId, memberId);
        mvc.perform(post("/api/households/{id}/expenses", householdId)
                        .header("Authorization", bearer(memberToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(exactBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.splitType").value("EXACT"));

        mvc.perform(get("/api/households/{householdId}/expenses/{expenseId}", householdId, expenseId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Compra semanal"));
        mvc.perform(get("/api/households/{id}/expenses", householdId)
                        .header("Authorization", bearer(ownerToken)).param("from", "2026-08-01")
                        .param("to", "2026-08-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(post("/api/households/{householdId}/expenses/{expenseId}/void", householdId, expenseId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Duplicado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VOIDED"))
                .andExpect(jsonPath("$.voidReason").value("Duplicado"));
        mvc.perform(post("/api/households/{householdId}/expenses/{expenseId}/void", householdId, expenseId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Otra vez\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectExpenseEndpointsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/households/{id}/expenses", java.util.UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode createHousehold(String token) throws Exception {
        String body = mvc.perform(post("/api/households").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Casa Azul\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private String register(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asString();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
