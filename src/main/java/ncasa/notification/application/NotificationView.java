package ncasa.notification.application;

import java.math.BigDecimal;import java.time.*;import java.util.UUID;import ncasa.notification.domain.*;
public record NotificationView(UUID id,NotificationKind kind,UUID householdId,UUID planId,UUID calendarEntryId,String subject,
        BigDecimal amount,String currency,LocalDate occurrenceDate,Integer occurrenceNumber,Integer totalOccurrences,
        String attentionReason,UUID completedByMemberId,Instant occurredAt,Instant createdAt,Instant readAt){
    public static NotificationView from(Notification n){return new NotificationView(n.id().value(),n.kind(),n.householdId().value(),n.planId()==null?null:n.planId().value(),n.calendarEntryId()==null?null:n.calendarEntryId().value(),n.subject(),n.amount()==null?null:n.amount().amount(),n.amount()==null?null:n.amount().currency(),n.occurrenceDate(),n.occurrenceNumber(),n.totalOccurrences(),n.attentionReason(),n.completedByMemberId(),n.occurredAt(),n.createdAt(),n.readAt());}
}
