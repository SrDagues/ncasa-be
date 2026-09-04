package ncasa.expense.infrastructure.outbox;
import java.time.Instant;import java.util.UUID;
public record PublishedOutboxEvent(UUID eventId,String eventType,String payload,Instant occurredAt){}
