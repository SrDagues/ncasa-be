package ncasa.expense.application.port.out;
import java.time.Instant;import java.util.UUID;
public record OutboxMessage(UUID id,String eventType,String payload,int attempts,Instant occurredAt){}
