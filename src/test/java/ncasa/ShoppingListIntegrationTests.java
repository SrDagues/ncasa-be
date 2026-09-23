package ncasa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ShoppingListIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @Test
    void shouldCreateAddPurchasePollTrashAndRestoreAList() throws Exception {
        String token = register("shopping-list-owner@example.com");
        JsonNode household = createHousehold(token);
        String householdId = household.get("id").asString();
        String memberId = household.get("members").get(0).get("id").asString();

        JsonNode list = json.readTree(mvc.perform(post("/api/households/{id}/shopping-lists", householdId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Compra semanal\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString());
        String listId = list.get("id").asString();

        var collection = mvc.perform(get("/api/households/{id}/shopping-lists", householdId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk()).andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$[0].id").value(listId)).andReturn().getResponse();
        mvc.perform(get("/api/households/{id}/shopping-lists", householdId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .header(HttpHeaders.IF_NONE_MATCH, collection.getHeader(HttpHeaders.ETAG)))
                .andExpect(status().isNotModified()).andExpect(content().string(""));

        mvc.perform(post("/api/households/{id}/shopping-lists", householdId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  COMPRA   SEMANAL \"}"))
                .andExpect(status().isConflict());

        JsonNode added = json.readTree(mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/items", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tomates","quantity":2,"unit":"UNIT","note":"Para ensalada",
                                 "customUnit":null,"responsibleMemberId":"%s"}
                                """.formatted(memberId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.item.status").value("PENDING"))
                .andExpect(jsonPath("$.list.contentRevision").value(1))
                .andReturn().getResponse().getContentAsString());
        JsonNode item = added.get("item");

        var detail = mvc.perform(get("/api/households/{id}/shopping-lists/{listId}", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk()).andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.pending[0].name").value("Tomates"))
                .andReturn().getResponse();
        String etag = detail.getHeader(HttpHeaders.ETAG);

        mvc.perform(get("/api/households/{id}/shopping-lists/{listId}", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified()).andExpect(content().string(""));

        mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/items/{itemId}/purchase", householdId, listId, item.get("id").asString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + item.get("version").asLong() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PURCHASED"));

        JsonNode latest = json.readTree(mvc.perform(get("/api/households/{id}/shopping-lists/{listId}", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.purchased[0].purchasedByMemberId").value(memberId))
                .andReturn().getResponse().getContentAsString());
        JsonNode reused = json.readTree(mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/items/reuse-purchased", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentRevision\":" + latest.get("list").get("contentRevision").asLong() + "}"))
                .andExpect(status().isOk()).andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.pending[0].name").value("Tomates"))
                .andExpect(jsonPath("$.pending[0].note").value("Para ensalada"))
                .andExpect(jsonPath("$.pending[0].responsibleMemberId").value(memberId))
                .andExpect(jsonPath("$.pending[0].purchasedAt").doesNotExist())
                .andExpect(jsonPath("$.pending[0].purchasedByMemberId").doesNotExist())
                .andExpect(jsonPath("$.purchased").isEmpty())
                .andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/items/reuse-purchased", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentRevision\":" + latest.get("list").get("contentRevision").asLong() + "}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/items/reuse-purchased", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentRevision\":" + reused.get("list").get("contentRevision").asLong() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.contentRevision").value(reused.get("list").get("contentRevision").asLong()));
        JsonNode trashed = json.readTree(mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/trash", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + reused.get("list").get("version").asLong() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("TRASHED"))
                .andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/households/{id}/shopping-lists/{listId}/restore", householdId, listId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + trashed.get("version").asLong() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private JsonNode createHousehold(String token) throws Exception {
        return json.readTree(mvc.perform(post("/api/households").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Casa Compra\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private String register(String email) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isCreated());
        jdbc.update("UPDATE users SET email_verified_at = CURRENT_TIMESTAMP WHERE email = ?", email);
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asString();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
