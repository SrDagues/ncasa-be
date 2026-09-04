package ncasa.notification.application;

import java.math.BigDecimal;import java.time.*;import java.util.UUID;import ncasa.notification.domain.NotificationKind;

public record ConsumeExpensePlanNotificationCommand(UUID eventId,NotificationKind kind,UUID householdId,UUID planId,
        BigDecimal amount,String currency,LocalDate occurrenceDate,int occurrenceNumber,Integer totalOccurrences,Instant occurredAt){}
