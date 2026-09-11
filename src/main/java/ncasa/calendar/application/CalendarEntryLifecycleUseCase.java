package ncasa.calendar.application;

import java.time.Clock;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.application.port.out.TaskCompletionNotificationPort;
import ncasa.calendar.domain.CalendarEntry;
import ncasa.calendar.domain.CalendarEntryKind;

public final class CalendarEntryLifecycleUseCase {
    private final CalendarEntryRepository entries;private final CalendarHouseholdAccessPort households;private final TaskCompletionNotificationPort completionNotifications;private final Clock clock;
    public CalendarEntryLifecycleUseCase(CalendarEntryRepository e,CalendarHouseholdAccessPort h,Clock c){this(e,h,TaskCompletionNotificationPort.NONE,c);}
    public CalendarEntryLifecycleUseCase(CalendarEntryRepository e,CalendarHouseholdAccessPort h,TaskCompletionNotificationPort n,Clock c){entries=e;households=h;completionNotifications=n;clock=c;}
    public CalendarEntry trash(Long actor,UUID householdId,UUID id,long version){var e=load(actor,householdId,id,version);e.trash(clock.instant());return entries.save(e);}
    public CalendarEntry trashFrom(Long actor,UUID householdId,UUID id,long version,java.time.LocalDate effectiveFrom){
        var e=load(actor,householdId,id,version);
        if(e.recurrence()==null||effectiveFrom==null||effectiveFrom.equals(e.timing().startDate()))return trash(actor,householdId,id,version);
        var deleted=e.trashedSuccessor(UUID.randomUUID(),effectiveFrom,clock.instant());entries.save(e);return entries.save(deleted);
    }
    public CalendarEntry restore(Long actor,UUID householdId,UUID id,long version){var e=load(actor,householdId,id,version);e.restore();return entries.save(e);}
    public void purge(Long actor,UUID householdId,UUID id,long version){var e=load(actor,householdId,id,version);if(e.deletedAt()==null)throw new CalendarEntryConflictException("Only trashed entries can be permanently deleted");entries.delete(e);}
    public CalendarEntry complete(Long actor,UUID householdId,UUID id,String key,long version){
        var context=households.getContext(householdId,actor);
        var e=find(householdId,id,version);
        boolean changed=e.complete(key);
        var saved=entries.save(e);
        if(changed&&e.kind()==CalendarEntryKind.TASK)completionNotifications.notify(new TaskCompletionNotificationPort.TaskCompleted(
                e.id(),e.householdId(),e.title(),java.time.LocalDate.parse(key),e.participants(),context.actorMemberId(),clock.instant()));
        return saved;
    }
    public CalendarEntry reopen(Long actor,UUID householdId,UUID id,String key,long version){var e=load(actor,householdId,id,version);e.reopen(key);return entries.save(e);}
    private CalendarEntry load(Long actor,UUID householdId,UUID id,long version){households.getContext(householdId,actor);return find(householdId,id,version);}
    private CalendarEntry find(UUID householdId,UUID id,long version){var e=entries.find(id,householdId).orElseThrow(CalendarEntryNotFoundException::new);if(e.version()!=version)throw new CalendarEntryConflictException("Calendar entry changed concurrently");return e;}
}
