package ncasa.calendar.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CalendarEntry {
    private final UUID id;
    private final UUID seriesId;
    private final UUID householdId;
    private CalendarEntryKind kind;
    private String title;
    private CalendarTiming timing;
    private String color;
    private String location;
    private String note;
    private String link;
    private Set<UUID> participants;
    private RecurrenceRule recurrence;
    private List<ReminderRule> reminders;
    private Set<UUID> reminderRecipients;
    private SpecialDateType specialDateType;
    private UUID relatedMemberId;
    private final UUID createdByMemberId;
    private final Set<String> completedOccurrences;
    private Instant deletedAt;
    private long version;

    private CalendarEntry(UUID id, UUID seriesId, UUID householdId, CalendarEntryKind kind, String title,
            CalendarTiming timing, String color, String location, String note, String link, Set<UUID> participants,
            RecurrenceRule recurrence, List<ReminderRule> reminders, Set<UUID> reminderRecipients,
            SpecialDateType specialDateType, UUID relatedMemberId, UUID createdByMemberId,
            Set<String> completedOccurrences, Instant deletedAt, long version) {
        this.id = Objects.requireNonNull(id); this.seriesId = Objects.requireNonNull(seriesId);
        this.householdId = Objects.requireNonNull(householdId); this.createdByMemberId = Objects.requireNonNull(createdByMemberId);
        this.completedOccurrences = new HashSet<>(completedOccurrences); this.deletedAt = deletedAt; this.version = version;
        replace(kind, title, timing, color, location, note, link, participants, recurrence, reminders,
                reminderRecipients, specialDateType, relatedMemberId);
    }

    public static CalendarEntry create(UUID id, UUID householdId, CalendarEntryKind kind, String title,
            CalendarTiming timing, String color, String location, String note, String link, Set<UUID> participants,
            RecurrenceRule recurrence, List<ReminderRule> reminders, Set<UUID> reminderRecipients,
            SpecialDateType specialDateType, UUID relatedMemberId, UUID createdByMemberId) {
        return new CalendarEntry(id, id, householdId, kind, title, timing, color, location, note, link, participants,
                recurrence, reminders, reminderRecipients, specialDateType, relatedMemberId, createdByMemberId,
                Set.of(), null, 0);
    }

    public static CalendarEntry rehydrate(UUID id, UUID seriesId, UUID householdId, CalendarEntryKind kind,
            String title, CalendarTiming timing, String color, String location, String note, String link,
            Set<UUID> participants, RecurrenceRule recurrence, List<ReminderRule> reminders,
            Set<UUID> reminderRecipients, SpecialDateType specialDateType, UUID relatedMemberId,
            UUID createdByMemberId, Set<String> completedOccurrences, Instant deletedAt, long version) {
        return new CalendarEntry(id, seriesId, householdId, kind, title, timing, color, location, note, link,
                participants, recurrence, reminders, reminderRecipients, specialDateType, relatedMemberId,
                createdByMemberId, completedOccurrences, deletedAt, version);
    }

    public void replace(CalendarEntryKind kind, String title, CalendarTiming timing, String color, String location,
            String note, String link, Set<UUID> participants, RecurrenceRule recurrence, List<ReminderRule> reminders,
            Set<UUID> reminderRecipients, SpecialDateType specialDateType, UUID relatedMemberId) {
        Objects.requireNonNull(kind); Objects.requireNonNull(timing);
        String normalizedTitle = text(title, 240, true, "title");
        String normalizedColor = text(color, 7, true, "color");
        if (!normalizedColor.matches("#[0-9a-fA-F]{6}")) throw new CalendarRuleViolationException("Color must use #RRGGBB");
        if (kind == CalendarEntryKind.EVENT && recurrence != null) throw new CalendarRuleViolationException("Events cannot recur");
        if (kind == CalendarEntryKind.SPECIAL_DATE && specialDateType == null) throw new CalendarRuleViolationException("Special date type is required");
        if (kind != CalendarEntryKind.SPECIAL_DATE && (specialDateType != null || relatedMemberId != null))
            throw new CalendarRuleViolationException("Special-date fields are not allowed");
        if (kind == CalendarEntryKind.SPECIAL_DATE && recurrence == null
                && (specialDateType == SpecialDateType.BIRTHDAY || specialDateType == SpecialDateType.ANNIVERSARY))
            recurrence=RecurrenceRule.never(RecurrenceFrequency.YEARLY);
        if (recurrence != null && recurrence.endType() == RecurrenceEndType.UNTIL_DATE
                && recurrence.untilDate().isBefore(timing.startDate())) throw new CalendarRuleViolationException("Recurrence end precedes start");
        this.kind=kind; this.title=normalizedTitle; this.timing=timing; this.color=normalizedColor.toLowerCase();
        this.location=text(location,240,false,"location"); this.note=text(note,1000,false,"note");
        this.link=text(link,1000,false,"link");
        if (this.link != null && !(this.link.startsWith("http://") || this.link.startsWith("https://")))
            throw new CalendarRuleViolationException("Link must use http or https");
        this.participants=Set.copyOf(participants == null ? Set.of() : participants);
        this.recurrence=recurrence; this.reminders=List.copyOf(reminders == null ? List.of() : reminders);
        this.reminderRecipients=Set.copyOf(reminderRecipients == null ? Set.of() : reminderRecipients);
        this.specialDateType=specialDateType; this.relatedMemberId=relatedMemberId;
    }

    public List<CalendarOccurrence> occurrencesBetween(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new CalendarRuleViolationException("Invalid date range");
        if (deletedAt != null) return List.of();
        var result = new ArrayList<CalendarOccurrence>();
        if (recurrence == null) { addIfVisible(result, timing.startDate(), from, to, false); return List.copyOf(result); }
        for (long index=0; index<100_000; index++) {
            LocalDate date=recurrence.occurrence(timing.startDate(),index);
            if (!recurrence.allows(date,index) || date.isAfter(to)) break;
            addIfVisible(result,date,from,to,true);
        }
        return List.copyOf(result);
    }

    private void addIfVisible(List<CalendarOccurrence> target, LocalDate date, LocalDate from, LocalDate to, boolean recurring) {
        if (date.isBefore(from) || date.isAfter(to)) return;
        String key=date.toString();
        target.add(new CalendarOccurrence(id,key,kind,title,timing.shiftedTo(date),color,
                completedOccurrences.contains(key) ? CalendarEntryStatus.COMPLETED : CalendarEntryStatus.PENDING,recurring));
    }

    public boolean complete(String occurrenceKey) { requireCompletable(occurrenceKey); return completedOccurrences.add(occurrenceKey); }
    public void reopen(String occurrenceKey) { requireCompletable(occurrenceKey); completedOccurrences.remove(occurrenceKey); }
    private void requireCompletable(String key) {
        if (kind == CalendarEntryKind.SPECIAL_DATE) throw new CalendarRuleViolationException("Special dates cannot be completed");
        LocalDate date;
        try { date=LocalDate.parse(key); } catch (RuntimeException ex) { throw new CalendarRuleViolationException("Invalid occurrence key"); }
        if (occurrencesBetween(date,date).isEmpty()) throw new CalendarRuleViolationException("Occurrence does not belong to entry");
    }
    public void trash(Instant at) { if (deletedAt != null) throw new CalendarRuleViolationException("Entry is already in trash"); deletedAt=Objects.requireNonNull(at); }
    public void restore() { if (deletedAt == null) throw new CalendarRuleViolationException("Entry is not in trash"); deletedAt=null; }

    public void truncateBefore(LocalDate effectiveFrom) {
        if (recurrence == null || effectiveFrom.equals(timing.startDate()))
            throw new CalendarRuleViolationException("Entry cannot be split at this date");
        var occurrence=occurrencesBetween(effectiveFrom,effectiveFrom);
        if (occurrence.isEmpty()) throw new CalendarRuleViolationException("Effective date is not an occurrence");
        LocalDate previous=null;
        for(long index=0;index<100_000;index++){
            LocalDate date=recurrence.occurrence(timing.startDate(),index);
            if(!recurrence.allows(date,index)||!date.isBefore(effectiveFrom))break;
            previous=date;
        }
        if(previous==null)throw new CalendarRuleViolationException("Entry cannot be split before its first occurrence");
        recurrence=RecurrenceRule.until(recurrence.frequency(),previous);
    }

    public CalendarEntry successor(UUID successorId, CalendarEntryKind nextKind, String nextTitle,
            CalendarTiming nextTiming, String nextColor, String nextLocation, String nextNote, String nextLink,
            Set<UUID> nextParticipants, RecurrenceRule nextRecurrence, List<ReminderRule> nextReminders,
            Set<UUID> nextRecipients, SpecialDateType nextSpecialDateType, UUID nextRelatedMemberId) {
        return new CalendarEntry(successorId,seriesId,householdId,nextKind,nextTitle,nextTiming,nextColor,nextLocation,
                nextNote,nextLink,nextParticipants,nextRecurrence,nextReminders,nextRecipients,nextSpecialDateType,
                nextRelatedMemberId,createdByMemberId,Set.of(),null,0);
    }

    public CalendarEntry trashedSuccessor(UUID successorId, LocalDate effectiveFrom, Instant at) {
        if (recurrence == null) throw new CalendarRuleViolationException("Only recurring entries can be split");
        var next=successor(successorId,kind,title,timing.shiftedTo(effectiveFrom),color,location,note,link,
                participants,recurrence,reminders,reminderRecipients,specialDateType,relatedMemberId);
        truncateBefore(effectiveFrom);
        next.trash(at);
        return next;
    }

    private static String text(String value,int max,boolean required,String field) {
        String normalized=value==null?"":value.trim();
        if ((required&&normalized.isEmpty()) || normalized.length()>max) throw new CalendarRuleViolationException("Invalid "+field);
        return normalized.isEmpty()?null:normalized;
    }

    public UUID id(){return id;} public UUID seriesId(){return seriesId;} public UUID householdId(){return householdId;}
    public CalendarEntryKind kind(){return kind;} public String title(){return title;} public CalendarTiming timing(){return timing;}
    public String color(){return color;} public String location(){return location;} public String note(){return note;} public String link(){return link;}
    public Set<UUID> participants(){return participants;} public RecurrenceRule recurrence(){return recurrence;}
    public List<ReminderRule> reminders(){return reminders;} public Set<UUID> reminderRecipients(){return reminderRecipients;}
    public SpecialDateType specialDateType(){return specialDateType;} public UUID relatedMemberId(){return relatedMemberId;}
    public UUID createdByMemberId(){return createdByMemberId;} public Set<String> completedOccurrences(){return Set.copyOf(completedOccurrences);}
    public Instant deletedAt(){return deletedAt;} public long version(){return version;}
}
