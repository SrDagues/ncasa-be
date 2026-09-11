package ncasa.notification.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class Notification {
    private final NotificationId id;
    private final IntegrationEventId eventId;
    private final AccountRef recipient;
    private final HouseholdRef householdId;
    private final PlanRef planId;
    private final CalendarEntryRef calendarEntryId;
    private final NotificationKind kind;
    private final String subject;
    private final NotificationAmount amount;
    private final LocalDate occurrenceDate;
    private final Integer occurrenceNumber;
    private final Integer totalOccurrences;
    private final String attentionReason;
    private final java.util.UUID completedByMemberId;
    private final Instant occurredAt;
    private final Instant createdAt;
    private Instant readAt;

    private Notification(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, CalendarEntryRef calendarEntryId, NotificationKind kind,
            String subject, NotificationAmount amount, LocalDate occurrenceDate, Integer occurrenceNumber,
            Integer totalOccurrences, String attentionReason, java.util.UUID completedByMemberId,
            Instant occurredAt, Instant createdAt, Instant readAt) {
        this.id = Objects.requireNonNull(id);
        this.eventId = Objects.requireNonNull(eventId);
        this.recipient = Objects.requireNonNull(recipient);
        this.householdId = Objects.requireNonNull(householdId);
        this.kind = Objects.requireNonNull(kind);
        boolean taskCompletion = kind == NotificationKind.CALENDAR_TASK_COMPLETED;
        this.planId = taskCompletion ? planId : Objects.requireNonNull(planId);
        this.calendarEntryId = taskCompletion ? Objects.requireNonNull(calendarEntryId) : calendarEntryId;
        if (taskCompletion && planId != null) throw new IllegalArgumentException("Task notification cannot reference an expense plan");
        if (!taskCompletion && calendarEntryId != null) throw new IllegalArgumentException("Expense notification cannot reference a calendar entry");
        this.subject = required(subject, 240, "Subject");
        this.amount = taskCompletion ? amount : Objects.requireNonNull(amount);
        if (taskCompletion && amount != null) throw new IllegalArgumentException("Task notification cannot contain an amount");
        this.occurrenceDate = Objects.requireNonNull(occurrenceDate, "Occurrence date is required");
        if (!taskCompletion && (occurrenceNumber == null || occurrenceNumber <= 0))
            throw new IllegalArgumentException("Occurrence number must be positive");
        if (totalOccurrences != null && (occurrenceNumber == null || totalOccurrences < occurrenceNumber)) {
            throw new IllegalArgumentException("Total occurrences cannot precede current occurrence");
        }
        if (taskCompletion && (occurrenceNumber != null || totalOccurrences != null))
            throw new IllegalArgumentException("Task notification cannot contain installment data");
        this.occurrenceNumber = occurrenceNumber;
        this.totalOccurrences = totalOccurrences;
        boolean attention = kind == NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED;
        if (attention)
            this.attentionReason = required(attentionReason, 500, "Attention reason");
        else {
            if (attentionReason != null && !attentionReason.isBlank()) {
                throw new IllegalArgumentException("Reminder notification cannot contain an attention reason");
            }
            this.attentionReason = null;
        }
        this.completedByMemberId = taskCompletion ? Objects.requireNonNull(completedByMemberId) : completedByMemberId;
        if (!taskCompletion && completedByMemberId != null)
            throw new IllegalArgumentException("Expense notification cannot contain a completing member");
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        if (occurredAt.isAfter(createdAt))
            throw new IllegalArgumentException("Event cannot occur after notification creation");
        if (readAt != null && readAt.isBefore(createdAt))
            throw new IllegalArgumentException("Read time cannot precede creation");
        this.readAt = readAt;
    }

    public static Notification create(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, NotificationKind kind, String subject,
            NotificationAmount amount, LocalDate occurrenceDate, int occurrenceNumber,
            Integer totalOccurrences, String attentionReason, Instant occurredAt, Instant createdAt) {
        return new Notification(id, eventId, recipient, householdId, planId, null, kind, subject, amount, occurrenceDate,
                occurrenceNumber, totalOccurrences, attentionReason, null, occurredAt, createdAt, null);
    }

    public static Notification taskCompleted(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, CalendarEntryRef calendarEntryId, String subject, LocalDate occurrenceDate,
            java.util.UUID completedByMemberId, Instant occurredAt, Instant createdAt) {
        return new Notification(id,eventId,recipient,householdId,null,calendarEntryId,
                NotificationKind.CALENDAR_TASK_COMPLETED,subject,null,occurrenceDate,null,null,null,
                completedByMemberId,occurredAt,createdAt,null);
    }

    public static Notification rehydrate(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, NotificationKind kind, String subject,
            NotificationAmount amount, LocalDate occurrenceDate, int occurrenceNumber,
            Integer totalOccurrences, String attentionReason, Instant occurredAt, Instant createdAt,
            Instant readAt) {
        return new Notification(id, eventId, recipient, householdId, planId, null, kind, subject, amount, occurrenceDate,
                occurrenceNumber, totalOccurrences, attentionReason, null, occurredAt, createdAt, readAt);
    }

    public static Notification rehydrate(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, CalendarEntryRef calendarEntryId, NotificationKind kind,
            String subject, NotificationAmount amount, LocalDate occurrenceDate, Integer occurrenceNumber,
            Integer totalOccurrences, String attentionReason, java.util.UUID completedByMemberId,
            Instant occurredAt, Instant createdAt, Instant readAt) {
        return new Notification(id,eventId,recipient,householdId,planId,calendarEntryId,kind,subject,amount,
                occurrenceDate,occurrenceNumber,totalOccurrences,attentionReason,completedByMemberId,
                occurredAt,createdAt,readAt);
    }

    public void markRead(Instant now) {
        Objects.requireNonNull(now);
        if (now.isBefore(createdAt))
            throw new IllegalArgumentException("Read time cannot precede creation");
        if (readAt == null)
            readAt = now;
    }

    public boolean isUnread() {
        return readAt == null;
    }

    private static String required(String value, int max, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " is required");
        var normalized = value.trim();
        if (normalized.length() > max)
            throw new IllegalArgumentException(field + " is too long");
        return normalized;
    }

    public NotificationId id() {
        return id;
    }

    public IntegrationEventId eventId() {
        return eventId;
    }

    public AccountRef recipient() {
        return recipient;
    }

    public HouseholdRef householdId() {
        return householdId;
    }

    public PlanRef planId() {
        return planId;
    }

    public CalendarEntryRef calendarEntryId() { return calendarEntryId; }

    public NotificationKind kind() {
        return kind;
    }

    public String subject() {
        return subject;
    }

    public NotificationAmount amount() {
        return amount;
    }

    public LocalDate occurrenceDate() {
        return occurrenceDate;
    }

    public Integer occurrenceNumber() {
        return occurrenceNumber;
    }

    public Integer totalOccurrences() {
        return totalOccurrences;
    }

    public String attentionReason() {
        return attentionReason;
    }

    public java.util.UUID completedByMemberId() { return completedByMemberId; }

    public Instant occurredAt() {
        return occurredAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant readAt() {
        return readAt;
    }
}
