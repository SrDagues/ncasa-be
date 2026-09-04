package ncasa.expense.domain;

import java.time.*;
import java.util.*;

public final class ExpensePlan {
    private final ExpensePlanId id; private final HouseholdRef householdId; private final MemberRef createdByMemberId;
    private final ExpenseTemplate template; private final Schedule schedule; private final EndCondition endCondition;
    private final ZoneId zoneId; private final int reminderDaysBefore; private long scheduleCursor;
    private int materializedOccurrences; private LocalDate nextOccurrence; private ExpensePlanStatus status;
    private String remindedOccurrenceKey;
    private String pauseReason; private String cancellationReason; private final Instant createdAt; private Instant updatedAt;
    private Instant pausedAt; private Instant cancelledAt; private Instant completedAt; private long version;
    private final List<ExpensePlanEvent> events = new ArrayList<>();

    private ExpensePlan(ExpensePlanId id, HouseholdRef householdId, MemberRef creator, ExpenseTemplate template,
            Schedule schedule, EndCondition endCondition, ZoneId zoneId, int reminderDaysBefore, long cursor,
            int materialized, LocalDate next, ExpensePlanStatus status, String remindedOccurrenceKey, String pauseReason, String cancellationReason,
            Instant createdAt, Instant updatedAt, Instant pausedAt, Instant cancelledAt, Instant completedAt, long version) {
        this.id=Objects.requireNonNull(id);this.householdId=Objects.requireNonNull(householdId);this.createdByMemberId=Objects.requireNonNull(creator);
        this.template=Objects.requireNonNull(template);this.schedule=Objects.requireNonNull(schedule);this.endCondition=Objects.requireNonNull(endCondition);
        this.zoneId=Objects.requireNonNull(zoneId);if(reminderDaysBefore<0||reminderDaysBefore>30)throw new ExpenseRuleViolationException("Reminder days must be between 0 and 30");
        this.reminderDaysBefore=reminderDaysBefore;this.scheduleCursor=cursor;this.materializedOccurrences=materialized;this.nextOccurrence=next;
        this.status=Objects.requireNonNull(status);this.remindedOccurrenceKey=remindedOccurrenceKey;this.pauseReason=pauseReason;this.cancellationReason=cancellationReason;this.createdAt=Objects.requireNonNull(createdAt);
        this.updatedAt=Objects.requireNonNull(updatedAt);this.pausedAt=pausedAt;this.cancelledAt=cancelledAt;this.completedAt=completedAt;this.version=version;
        validateLifecycle();
    }

    public static ExpensePlan create(ExpensePlanId id, HouseholdRef household, MemberRef creator, ExpenseTemplate template,
            Schedule schedule, EndCondition end, ZoneId zone, int reminderDays, Instant now) {
        if (schedule.frequency()==ExpensePlanFrequency.ONCE) end=EndCondition.afterOccurrences(1);
        LocalDate first=schedule.occurrence(0); if(!end.allows(first,0))throw new ExpenseRuleViolationException("End condition must allow an occurrence");
        var plan=new ExpensePlan(id,household,creator,template,schedule,end,zone,reminderDays,0,0,first,
                ExpensePlanStatus.ACTIVE,null,null,null,now,now,null,null,null,0);
        plan.event("ExpensePlanCreated", first, now);
        return plan;
    }

    public static ExpensePlan rehydrate(ExpensePlanId id, HouseholdRef household, MemberRef creator, ExpenseTemplate template,
            Schedule schedule, EndCondition end, ZoneId zone, int reminderDays, long cursor, int materialized,
            LocalDate next, ExpensePlanStatus status, String remindedOccurrenceKey, String pauseReason, String cancellationReason, Instant createdAt,
            Instant updatedAt, Instant pausedAt, Instant cancelledAt, Instant completedAt, long version) {
        return new ExpensePlan(id,household,creator,template,schedule,end,zone,reminderDays,cursor,materialized,next,status,
                remindedOccurrenceKey,pauseReason,cancellationReason,createdAt,updatedAt,pausedAt,cancelledAt,completedAt,version);
    }

    public void pause(String reason, Instant now) { requireStatus(ExpensePlanStatus.ACTIVE); pauseReason=trimOptional(reason,500);status=ExpensePlanStatus.PAUSED;pausedAt=now;updatedAt=now;event("ExpensePlanPaused",nextOccurrence,now); }
    public void reactivate(LocalDate today, Instant now) {
        requireStatus(ExpensePlanStatus.PAUSED); pauseReason=null;pausedAt=null;
        while(nextOccurrence!=null && nextOccurrence.isBefore(today)){scheduleCursor++;nextOccurrence=nextCandidate();}
        if(nextOccurrence==null){complete(now);}else{status=ExpensePlanStatus.ACTIVE;updatedAt=now;event("ExpensePlanReactivated",nextOccurrence,now);}
    }
    public void cancel(String reason, Instant now) { if(status==ExpensePlanStatus.CANCELLED||status==ExpensePlanStatus.COMPLETED)throw new ExpensePlanStateException("Terminal plan cannot be cancelled"); cancellationReason=required(reason,500);status=ExpensePlanStatus.CANCELLED;cancelledAt=now;nextOccurrence=null;updatedAt=now;event("ExpensePlanCancelled",null,now); }
    public void occurrenceMaterialized(LocalDate date, Instant now) { requireStatus(ExpensePlanStatus.ACTIVE);if(!Objects.equals(date,nextOccurrence))throw new ExpensePlanStateException("Unexpected occurrence");materializedOccurrences++;scheduleCursor++;nextOccurrence=nextCandidate();updatedAt=now;event("ExpenseOccurrenceMaterialized",date,now);if(nextOccurrence==null)complete(now); }
    public void pauseForAttention(String reason, Instant now) { requireStatus(ExpensePlanStatus.ACTIVE);pauseReason=required(reason,500);status=ExpensePlanStatus.PAUSED;pausedAt=now;updatedAt=now;event("ExpensePlanAttentionRequired",nextOccurrence,now); }
    public void reminderPublished(Instant now){requireStatus(ExpensePlanStatus.ACTIVE);if(nextOccurrence==null)throw new ExpensePlanStateException("Plan has no next reminder");String type=isLastOccurrence()?"ExpensePlanLastInstallmentApproaching":"ExpensePlanOccurrenceApproaching";event(type,nextOccurrence,now);remindedOccurrenceKey=nextOccurrence.toString();updatedAt=now;}
    private LocalDate nextCandidate(){if(schedule.frequency()==ExpensePlanFrequency.ONCE)return null;LocalDate candidate=schedule.occurrence(scheduleCursor);return endCondition.allows(candidate, endCondition instanceof AfterOccurrences ? materializedOccurrences : scheduleCursor)?candidate:null;}
    private void complete(Instant now){status=ExpensePlanStatus.COMPLETED;completedAt=now;nextOccurrence=null;updatedAt=now;event("ExpensePlanCompleted",null,now);}
    private void event(String type,LocalDate date,Instant now){var attributes=new LinkedHashMap<String,Object>();attributes.put("planId",id.value().toString());attributes.put("householdId",householdId.value().toString());attributes.put("currency",template.total().currency());attributes.put("amount",template.total().amount().toPlainString());if(date!=null){attributes.put("occurrenceNumber",materializedOccurrences+(type.equals("ExpenseOccurrenceMaterialized")?0:1));if(endCondition.occurrenceLimit()!=null)attributes.put("totalOccurrences",endCondition.occurrenceLimit());}events.add(new ExpensePlanEvent(type,id.value()+":"+type+":"+(date==null?now.toEpochMilli():date),now,date,attributes));}
    public List<ExpensePlanEvent> pullEvents(){var copy=List.copyOf(events);events.clear();return copy;}
    private void requireStatus(ExpensePlanStatus expected){if(status!=expected)throw new ExpensePlanStateException("Plan must be "+expected);}
    private void validateLifecycle(){if(status==ExpensePlanStatus.ACTIVE&&nextOccurrence==null)throw new ExpenseRuleViolationException("Active plan needs next occurrence");if(status==ExpensePlanStatus.CANCELLED&&(cancelledAt==null||cancellationReason==null))throw new ExpenseRuleViolationException("Cancelled plan needs audit data");if(status==ExpensePlanStatus.COMPLETED&&completedAt==null)throw new ExpenseRuleViolationException("Completed plan needs timestamp");}
    private static String trimOptional(String value,int max){if(value==null||value.isBlank())return null;return required(value,max);}
    private static String required(String value,int max){if(value==null||value.isBlank())throw new ExpenseRuleViolationException("Reason is required");String result=value.trim();if(result.length()>max)throw new ExpenseRuleViolationException("Reason is too long");return result;}
    public ExpensePlanId id(){return id;} public HouseholdRef householdId(){return householdId;} public MemberRef createdByMemberId(){return createdByMemberId;} public ExpenseTemplate template(){return template;} public Schedule schedule(){return schedule;} public EndCondition endCondition(){return endCondition;} public ZoneId zoneId(){return zoneId;} public int reminderDaysBefore(){return reminderDaysBefore;} public long scheduleCursor(){return scheduleCursor;} public int materializedOccurrences(){return materializedOccurrences;} public LocalDate nextOccurrence(){return nextOccurrence;} public ExpensePlanStatus status(){return status;} public String pauseReason(){return pauseReason;} public String cancellationReason(){return cancellationReason;} public Instant createdAt(){return createdAt;} public Instant updatedAt(){return updatedAt;} public Instant pausedAt(){return pausedAt;} public Instant cancelledAt(){return cancelledAt;} public Instant completedAt(){return completedAt;} public long version(){return version;}
    public Instant nextOccurrenceDueAt(){return nextOccurrence==null?null:nextOccurrence.atStartOfDay(zoneId).toInstant();}
    public Instant nextReminderAt(){return nextOccurrence==null||nextOccurrence.toString().equals(remindedOccurrenceKey)?null:nextOccurrence.minusDays(reminderDaysBefore).atStartOfDay(zoneId).toInstant();}
    public String remindedOccurrenceKey(){return remindedOccurrenceKey;}
    public boolean isLastOccurrence(){if(nextOccurrence==null)return false;if(schedule.frequency()==ExpensePlanFrequency.ONCE)return true;if(endCondition instanceof AfterOccurrences a)return materializedOccurrences+1>=a.totalOccurrences();return !endCondition.allows(schedule.occurrence(scheduleCursor+1),scheduleCursor+1);}
}
