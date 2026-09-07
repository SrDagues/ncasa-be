package ncasa.notification.application;

import java.math.BigDecimal;import java.time.*;import java.util.UUID;import ncasa.notification.domain.*;
public record NotificationView(UUID id,NotificationKind kind,UUID householdId,UUID planId,String subject,
        BigDecimal amount,String currency,LocalDate occurrenceDate,int occurrenceNumber,Integer totalOccurrences,
        String attentionReason,Instant occurredAt,Instant createdAt,Instant readAt){
    public static NotificationView from(Notification n){return new NotificationView(n.id().value(),n.kind(),n.householdId().value(),n.planId().value(),n.subject(),n.amount().amount(),n.amount().currency(),n.occurrenceDate(),n.occurrenceNumber(),n.totalOccurrences(),n.attentionReason(),n.occurredAt(),n.createdAt(),n.readAt());}
}
