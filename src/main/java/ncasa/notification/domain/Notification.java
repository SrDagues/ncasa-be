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
    private final NotificationKind kind;
    private final String subject;
    private final NotificationAmount amount;
    private final LocalDate occurrenceDate;
    private final int occurrenceNumber;
    private final Integer totalOccurrences;
    private final String attentionReason;
    private final Instant occurredAt;
    private final Instant createdAt;
    private Instant readAt;

    private Notification(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, NotificationKind kind, String subject,
            NotificationAmount amount, LocalDate occurrenceDate, int occurrenceNumber,
            Integer totalOccurrences, String attentionReason, Instant occurredAt, Instant createdAt,
            Instant readAt) {
        this.id = Objects.requireNonNull(id);
        this.eventId = Objects.requireNonNull(eventId);
        this.recipient = Objects.requireNonNull(recipient);
        this.householdId = Objects.requireNonNull(householdId);
        this.planId = Objects.requireNonNull(planId);
        this.kind = Objects.requireNonNull(kind);
        this.subject = required(subject, 240, "Subject");
        this.amount = Objects.requireNonNull(amount);
        this.occurrenceDate = Objects.requireNonNull(occurrenceDate, "Occurrence date is required");
        if (occurrenceNumber <= 0)
            throw new IllegalArgumentException("Occurrence number must be positive");
        if (totalOccurrences != null && totalOccurrences < occurrenceNumber) {
            throw new IllegalArgumentException("Total occurrences cannot precede current occurrence");
        }
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
        return new Notification(id, eventId, recipient, householdId, planId, kind, subject, amount, occurrenceDate,
                occurrenceNumber, totalOccurrences, attentionReason, occurredAt, createdAt, null);
    }

    public static Notification rehydrate(NotificationId id, IntegrationEventId eventId, AccountRef recipient,
            HouseholdRef householdId, PlanRef planId, NotificationKind kind, String subject,
            NotificationAmount amount, LocalDate occurrenceDate, int occurrenceNumber,
            Integer totalOccurrences, String attentionReason, Instant occurredAt, Instant createdAt,
            Instant readAt) {
        return new Notification(id, eventId, recipient, householdId, planId, kind, subject, amount, occurrenceDate,
                occurrenceNumber, totalOccurrences, attentionReason, occurredAt, createdAt, readAt);
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

    public int occurrenceNumber() {
        return occurrenceNumber;
    }

    public Integer totalOccurrences() {
        return totalOccurrences;
    }

    public String attentionReason() {
        return attentionReason;
    }

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
