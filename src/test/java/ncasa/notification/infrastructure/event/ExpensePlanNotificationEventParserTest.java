package ncasa.notification.infrastructure.event;

import static org.junit.jupiter.api.Assertions.*;import java.time.Instant;import java.util.UUID;import ncasa.expense.infrastructure.outbox.PublishedOutboxEvent;import ncasa.notification.domain.NotificationKind;import org.junit.jupiter.api.Test;import tools.jackson.databind.ObjectMapper;

class ExpensePlanNotificationEventParserTest{
    private final ExpensePlanNotificationEventParser parser=new ExpensePlanNotificationEventParser(new ObjectMapper());private final UUID eventId=UUID.randomUUID();private final Instant at=Instant.parse("2026-09-04T10:00:00Z");
    @Test void parsesSupportedReminder(){var result=parser.parse(event("ExpensePlanOccurrenceApproaching",payload("ExpensePlanOccurrenceApproaching"))).orElseThrow();assertEquals(NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,result.kind());assertEquals(2,result.occurrenceNumber());assertEquals(12,result.totalOccurrences());}
    @Test void ignoresUnknownEventWithoutParsingPayload(){assertTrue(parser.parse(event("ExpensePlanCreated","not-json")).isEmpty());}
    @Test void rejectsMismatchedEnvelope(){assertThrows(IllegalArgumentException.class,()->parser.parse(event("ExpensePlanOccurrenceApproaching",payload("ExpensePlanAttentionRequired"))));}
    private PublishedOutboxEvent event(String type,String payload){return new PublishedOutboxEvent(eventId,type,payload,at);}
    private String payload(String type){return "{\"eventId\":\""+eventId+"\",\"eventType\":\""+type+"\",\"occurredAt\":\""+at+"\",\"occurrenceDate\":\"2026-09-05\",\"data\":{\"householdId\":\""+UUID.randomUUID()+"\",\"planId\":\""+UUID.randomUUID()+"\",\"amount\":\"20.00\",\"currency\":\"EUR\",\"occurrenceNumber\":2,\"totalOccurrences\":12}}";}
}
