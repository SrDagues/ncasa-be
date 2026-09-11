package ncasa.calendar.infrastructure.persistence;

import java.time.Instant;
import java.util.*;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCalendarEntryRepositoryAdapter implements CalendarEntryRepository {
    private final SpringDataCalendarEntryRepository repository;
    public JpaCalendarEntryRepositoryAdapter(SpringDataCalendarEntryRepository repository){this.repository=repository;}
    @Override public CalendarEntry save(CalendarEntry entry){
        var row=repository.findById(entry.id()).orElseGet(JpaCalendarEntryEntity::new);
        boolean created=row.id==null; row.id=entry.id();row.seriesId=entry.seriesId();row.householdId=entry.householdId();
        row.kind=entry.kind().name();row.title=entry.title();row.allDay=entry.timing().allDay();row.startDate=entry.timing().startDate();
        row.startTime=entry.timing().startTime();row.endDate=entry.timing().endDate();row.endTime=entry.timing().endTime();
        row.color=entry.color();row.location=entry.location();row.note=entry.note();row.link=entry.link();
        row.recurrenceFrequency=entry.recurrence()==null?null:entry.recurrence().frequency().name();
        row.recurrenceEndType=entry.recurrence()==null?null:entry.recurrence().endType().name();
        row.recurrenceUntil=entry.recurrence()==null?null:entry.recurrence().untilDate();
        row.recurrenceCount=entry.recurrence()==null?null:entry.recurrence().totalOccurrences();
        row.specialDateType=entry.specialDateType()==null?null:entry.specialDateType().name();row.relatedMemberId=entry.relatedMemberId();
        row.createdByMemberId=entry.createdByMemberId();row.deletedAt=entry.deletedAt();
        Instant now=Instant.now();if(created)row.createdAt=now;row.updatedAt=now;
        row.participants.clear();row.participants.addAll(entry.participants());row.reminderRecipients.clear();row.reminderRecipients.addAll(entry.reminderRecipients());
        row.completedOccurrences.clear();row.completedOccurrences.addAll(entry.completedOccurrences());row.reminders.clear();
        entry.reminders().forEach(x->row.reminders.add(new JpaReminder(x.daysBefore(),x.enabled())));
        return domain(repository.saveAndFlush(row));
    }
    @Override public Optional<CalendarEntry> find(UUID id,UUID householdId){return repository.findByIdAndHouseholdId(id,householdId).map(this::domain);}
    @Override public List<CalendarEntry> findActive(UUID householdId){return repository.findByHouseholdIdAndDeletedAtIsNullOrderByStartDateAscIdAsc(householdId).stream().map(this::domain).toList();}
    @Override public List<CalendarEntry> findTrashed(UUID householdId){return repository.findByHouseholdIdAndDeletedAtIsNotNullOrderByDeletedAtDescIdAsc(householdId).stream().map(this::domain).toList();}
    @Override public void delete(CalendarEntry entry){repository.deleteById(entry.id());repository.flush();}
    private CalendarEntry domain(JpaCalendarEntryEntity r){
        var timing=new CalendarTiming(r.allDay,r.startDate,r.startTime,r.endDate,r.endTime);
        RecurrenceRule recurrence=r.recurrenceFrequency==null?null:new RecurrenceRule(RecurrenceFrequency.valueOf(r.recurrenceFrequency),
                RecurrenceEndType.valueOf(r.recurrenceEndType),r.recurrenceUntil,r.recurrenceCount);
        return CalendarEntry.rehydrate(r.id,r.seriesId,r.householdId,CalendarEntryKind.valueOf(r.kind),r.title,timing,r.color,
                r.location,r.note,r.link,r.participants,recurrence,r.reminders.stream().sorted(Comparator.comparingInt(x->x.daysBefore))
                .map(x->new ReminderRule(x.daysBefore,x.enabled)).toList(),r.reminderRecipients,
                r.specialDateType==null?null:SpecialDateType.valueOf(r.specialDateType),r.relatedMemberId,r.createdByMemberId,
                r.completedOccurrences,r.deletedAt,r.version);
    }
}
