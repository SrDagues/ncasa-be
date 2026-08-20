package ncasa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import ncasa.household.application.port.out.InvitationDeliveryPort;
import ncasa.household.domain.HouseholdInvitation;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Import(HouseholdIntegrationTests.TestDeliveryConfiguration.class)
class HouseholdIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired CapturingDelivery delivery;

    @BeforeEach
    void cleanDatabase() {
        delivery.rawToken = null;
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void shouldCreateInviteDiscoverAndAcceptMembership() throws Exception {
        String ownerToken = register("owner@example.com");
        String memberToken = register("member@example.com");

        String householdBody = mvc.perform(post("/api/households")
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Casa Azul\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.members[0].owner").value(true))
                .andReturn().getResponse().getContentAsString();
        String householdId = json.readTree(householdBody).get("id").asString();

        mvc.perform(get("/api/households").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(householdId))
                .andExpect(jsonPath("$[0].currentRole").value("ADMIN"))
                .andExpect(jsonPath("$[0].owner").value(true));
        mvc.perform(get("/api/households/{id}", householdId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.members[0].email").value("owner@example.com"));

        mvc.perform(post("/api/households/{id}/invitations", householdId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\",\"role\":\"MEMBER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(get("/api/households/{id}/invitations", householdId)
                        .header("Authorization", bearer(ownerToken)).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("member@example.com"))
                .andExpect(jsonPath("$[0].tokenHash").doesNotExist());

        String pendingBody = mvc.perform(get("/api/household-invitations/pending")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].householdName").value("Casa Azul"))
                .andReturn().getResponse().getContentAsString();
        String invitationId = json.readTree(pendingBody).get(0).get("id").asString();

        mvc.perform(post("/api/household-invitations/{id}/accept", invitationId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2))
                .andExpect(jsonPath("$.members[1].email").value("member@example.com"));

        mvc.perform(get("/api/households/{id}/invitations", householdId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectHouseholdEndpointsWithoutJwt() throws Exception {
        mvc.perform(get("/api/household-invitations/pending")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptByTokenThenTransferLeaveAndArchive() throws Exception {
        String ownerToken = register("owner@example.com");
        String memberToken = register("member@example.com");
        JsonNode created = json.readTree(mvc.perform(post("/api/households")
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Casa Azul\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String householdId = created.get("id").asString();

        mvc.perform(post("/api/households/{id}/invitations", householdId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\",\"role\":\"MEMBER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.deliverySucceeded").value(true));

        JsonNode accepted = json.readTree(mvc.perform(post("/api/household-invitations/accept-by-token")
                        .header("Authorization", bearer(memberToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("token", delivery.rawToken))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String newOwnerMemberId = accepted.get("members").get(1).get("id").asString();

        mvc.perform(post("/api/households/{id}/ownership-transfers", householdId)
                        .header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("memberId", newOwnerMemberId))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ownerMemberId").value(newOwnerMemberId));
        mvc.perform(post("/api/households/{id}/leave", householdId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/households/{id}", householdId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isNoContent());
    }

    private String register(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("email", email, "password", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode body = json.readTree(response);
        return body.get("accessToken").asString();
    }

    private String bearer(String token) { return "Bearer " + token; }

    @TestConfiguration
    static class TestDeliveryConfiguration {
        @Bean @Primary CapturingDelivery capturingDelivery() { return new CapturingDelivery(); }
    }

    static class CapturingDelivery implements InvitationDeliveryPort {
        String rawToken;
        public void deliver(HouseholdInvitation invitation, String rawToken) { this.rawToken = rawToken; }
    }
}
