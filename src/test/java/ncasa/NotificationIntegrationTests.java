package ncasa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;import java.time.Instant;import java.util.UUID;import ncasa.expense.infrastructure.outbox.PublishedOutboxEvent;import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.context.ApplicationEventPublisher;import org.springframework.http.MediaType;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.test.web.servlet.MockMvc;import tools.jackson.databind.*;

@SpringBootTest @org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class NotificationIntegrationTests{
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;@Autowired ApplicationEventPublisher events;
    @BeforeEach void clean(){jdbc.update("DELETE FROM in_app_notifications");jdbc.update("DELETE FROM outbox_messages");jdbc.update("DELETE FROM expense_plan_allocations");jdbc.update("DELETE FROM expenses");jdbc.update("DELETE FROM expense_plans");jdbc.update("DELETE FROM expense_categories");jdbc.update("DELETE FROM household_invitations");jdbc.update("DELETE FROM household_members");jdbc.update("DELETE FROM households");jdbc.update("DELETE FROM refresh_tokens");jdbc.update("DELETE FROM auth_identities");jdbc.update("DELETE FROM user_roles");jdbc.update("DELETE FROM users");}
    @Test void consumesReminderAndExposesSecureReadLifecycle()throws Exception{
        String token=register("notify@example.com");JsonNode household=json.readTree(mvc.perform(post("/api/households").header("Authorization",bearer(token)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Home\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());String householdId=household.get("id").asString(),memberId=household.get("members").get(0).get("id").asString();
        String planBody="""
                {"template":{"description":"Rent","amount":"20.00","currency":"EUR","payerMemberId":"%s","split":{"type":"EQUAL","memberIds":["%s"]}},"schedule":{"frequency":"MONTHLY","startDate":"2026-10-01","zoneId":"Europe/Madrid"},"endCondition":{"type":"AFTER_OCCURRENCES","totalOccurrences":12},"reminderDaysBefore":1}
                """.formatted(memberId,memberId);
        JsonNode plan=json.readTree(mvc.perform(post("/api/households/{id}/expense-plans",householdId).header("Authorization",bearer(token)).contentType(MediaType.APPLICATION_JSON).content(planBody)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        UUID eventId=UUID.randomUUID();Instant occurredAt=Instant.now().minusSeconds(1);String payload="""
                {"eventId":"%s","eventType":"ExpensePlanOccurrenceApproaching","occurredAt":"%s","occurrenceDate":"2026-10-01","data":{"householdId":"%s","planId":"%s","amount":"20.00","currency":"EUR","occurrenceNumber":1,"totalOccurrences":12}}
                """.formatted(eventId,occurredAt,householdId,plan.get("id").asString());events.publishEvent(new PublishedOutboxEvent(eventId,"ExpensePlanOccurrenceApproaching",payload,occurredAt));
        JsonNode page=json.readTree(mvc.perform(get("/api/notifications").header("Authorization",bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.items[0].subject").value("Rent")).andExpect(jsonPath("$.items[0].amount").value("20.00")).andReturn().getResponse().getContentAsString());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization",bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$.unreadCount").value(1));
        mvc.perform(post("/api/notifications/{id}/read",page.get("items").get(0).get("id").asString()).header("Authorization",bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$.readAt").isNotEmpty());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization",bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$.unreadCount").value(0));
        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());mvc.perform(get("/api/notifications").header("Authorization",bearer(token)).param("size","51")).andExpect(status().isBadRequest());
    }
    private String register(String email)throws Exception{String body=mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+email+"\",\"password\":\"password123\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();return json.readTree(body).get("accessToken").asString();}
    private String bearer(String token){return "Bearer "+token;}
}
