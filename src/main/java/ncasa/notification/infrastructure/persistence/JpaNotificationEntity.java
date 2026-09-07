package ncasa.notification.infrastructure.persistence;

import jakarta.persistence.*;import java.math.BigDecimal;import java.time.*;import java.util.UUID;

@Entity @Table(name="in_app_notifications",uniqueConstraints=@UniqueConstraint(name="uk_notification_event_recipient",columnNames={"event_id","recipient_account_id"}))
class JpaNotificationEntity {
    @Id UUID id;@Column(name="event_id",nullable=false)UUID eventId;@Column(name="recipient_account_id",nullable=false)Long recipientAccountId;
    @Column(name="household_id",nullable=false)UUID householdId;@Column(name="plan_id",nullable=false)UUID planId;
    @Column(nullable=false,length=80)String kind;@Column(nullable=false,length=240)String subject;
    @Column(nullable=false,precision=19,scale=4)BigDecimal amount;@Column(nullable=false,length=3)String currency;
    @Column(name="occurrence_date",nullable=false)LocalDate occurrenceDate;@Column(name="occurrence_number",nullable=false)int occurrenceNumber;
    @Column(name="total_occurrences")Integer totalOccurrences;@Column(name="attention_reason",length=500)String attentionReason;
    @Column(name="occurred_at",nullable=false)Instant occurredAt;@Column(name="created_at",nullable=false)Instant createdAt;@Column(name="read_at")Instant readAt;
    protected JpaNotificationEntity(){}
}
