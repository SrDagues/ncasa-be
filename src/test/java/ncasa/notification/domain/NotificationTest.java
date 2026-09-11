package ncasa.notification.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;import java.time.*;import java.util.*;import org.junit.jupiter.api.Test;

class NotificationTest {
    private static final Instant NOW=Instant.parse("2026-09-04T10:00:00Z");
    @Test void marksReadOnce(){var n=notification(NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,null);n.markRead(NOW.plusSeconds(1));n.markRead(NOW.plusSeconds(2));assertEquals(NOW.plusSeconds(1),n.readAt());}
    @Test void attentionRequiresReason(){assertThrows(IllegalArgumentException.class,()->notification(NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED,null));}
    @Test void reminderRejectsAttentionReason(){assertThrows(IllegalArgumentException.class,()->notification(NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,"reason"));}
    @Test void rejectsInvalidOccurrence(){assertThrows(IllegalArgumentException.class,()->Notification.create(new NotificationId(UUID.randomUUID()),new IntegrationEventId(UUID.randomUUID()),new AccountRef(1L),new HouseholdRef(UUID.randomUUID()),new PlanRef(UUID.randomUUID()),NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,"Rent",new NotificationAmount(BigDecimal.ONE,"EUR"),LocalDate.now(),0,null,null,NOW,NOW));}
    @Test void createsTaskCompletionWithoutExpenseData(){var entry=UUID.randomUUID();var actor=UUID.randomUUID();var n=Notification.taskCompleted(new NotificationId(UUID.randomUUID()),new IntegrationEventId(UUID.randomUUID()),new AccountRef(1L),new HouseholdRef(UUID.randomUUID()),new CalendarEntryRef(entry),"Comprar",LocalDate.of(2026,9,12),actor,NOW,NOW);assertEquals(NotificationKind.CALENDAR_TASK_COMPLETED,n.kind());assertEquals(entry,n.calendarEntryId().value());assertEquals(actor,n.completedByMemberId());assertNull(n.planId());assertNull(n.amount());}
    private Notification notification(NotificationKind kind,String reason){return Notification.create(new NotificationId(UUID.randomUUID()),new IntegrationEventId(UUID.randomUUID()),new AccountRef(1L),new HouseholdRef(UUID.randomUUID()),new PlanRef(UUID.randomUUID()),kind,"Rent",new NotificationAmount(new BigDecimal("20.00"),"EUR"),LocalDate.of(2026,9,5),1,12,reason,NOW,NOW);}
}
