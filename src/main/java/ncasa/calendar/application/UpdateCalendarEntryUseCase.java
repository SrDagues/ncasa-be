package ncasa.calendar.application;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;
import ncasa.calendar.application.port.out.*;
import ncasa.calendar.domain.CalendarEntry;

public final class UpdateCalendarEntryUseCase {
    private final CalendarEntryRepository entries;private final CalendarHouseholdAccessPort households;
    public UpdateCalendarEntryUseCase(CalendarEntryRepository e,CalendarHouseholdAccessPort h){entries=e;households=h;}
    public CalendarEntry execute(Long actor,UUID householdId,UUID id,long version,LocalDate effectiveFrom,CalendarEntryCommand command){
        var context=households.getContext(householdId,actor);var referenced=new HashSet<>(command.participantMemberIds());
        referenced.addAll(command.reminderRecipientMemberIds());context.requireMembers(referenced);context.requireMember(command.relatedMemberId());
        var current=entries.find(id,householdId).orElseThrow(CalendarEntryNotFoundException::new);
        if(current.version()!=version)throw new CalendarEntryConflictException("Calendar entry changed concurrently");
        if(current.deletedAt()!=null)throw new CalendarEntryConflictException("Trashed entries cannot be edited");
        if(current.recurrence()!=null&&effectiveFrom!=null&&!effectiveFrom.equals(current.timing().startDate())){
            if(!command.timing().startDate().equals(effectiveFrom))throw new IllegalArgumentException("Replacement must start on effective date");
            current.truncateBefore(effectiveFrom);entries.save(current);
            var next=current.successor(UUID.randomUUID(),command.kind(),command.title(),command.timing(),command.color(),
                    command.location(),command.note(),command.link(),command.participantMemberIds(),command.recurrence(),
                    command.reminders(),command.reminderRecipientMemberIds(),command.specialDateType(),command.relatedMemberId());
            return entries.save(next);
        }
        current.replace(command.kind(),command.title(),command.timing(),command.color(),command.location(),command.note(),
                command.link(),command.participantMemberIds(),command.recurrence(),command.reminders(),
                command.reminderRecipientMemberIds(),command.specialDateType(),command.relatedMemberId());
        return entries.save(current);
    }
}
