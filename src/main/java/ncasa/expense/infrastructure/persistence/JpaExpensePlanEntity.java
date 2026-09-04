package ncasa.expense.infrastructure.persistence;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.*;import java.util.*;
@Entity @Table(name="expense_plans") class JpaExpensePlanEntity{
 @Id UUID id;@Column(name="household_id",nullable=false)UUID householdId;@Column(name="created_by_member_id",nullable=false)UUID createdByMemberId;
 @Column(nullable=false,length=240)String description;@Column(nullable=false,precision=19,scale=4)BigDecimal amount;@Column(nullable=false,length=3)String currency;
 @Column(name="payer_member_id",nullable=false)UUID payerMemberId;@Column(name="category_id")UUID categoryId;@Column(name="split_type",nullable=false)String splitType;
 @Column(nullable=false)String frequency;@Column(name="start_date",nullable=false)LocalDate startDate;@Column(name="zone_id",nullable=false)String zoneId;@Column(name="end_type",nullable=false)String endType;@Column(name="end_date")LocalDate endDate;@Column(name="total_occurrences")Integer totalOccurrences;
 @Column(name="reminder_days_before",nullable=false)int reminderDaysBefore;@Column(name="schedule_cursor",nullable=false)long scheduleCursor;@Column(name="materialized_occurrences",nullable=false)int materializedOccurrences;@Column(name="next_occurrence")LocalDate nextOccurrence;@Column(name="next_occurrence_due_at")Instant nextOccurrenceDueAt;@Column(name="next_reminder_at")Instant nextReminderAt;@Column(name="reminded_occurrence_key")String remindedOccurrenceKey;
 @Column(nullable=false)String status;@Column(name="pause_reason")String pauseReason;@Column(name="cancellation_reason")String cancellationReason;@Column(name="created_at",nullable=false,updatable=false)Instant createdAt;@Column(name="updated_at",nullable=false)Instant updatedAt;@Column(name="paused_at")Instant pausedAt;@Column(name="cancelled_at")Instant cancelledAt;@Column(name="completed_at")Instant completedAt;@Version long version;
 @OneToMany(mappedBy="plan",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY)List<JpaExpensePlanAllocationEntity> allocations=new ArrayList<>();protected JpaExpensePlanEntity(){}
 void add(JpaExpensePlanAllocationEntity a){allocations.add(a);a.plan=this;}
}
