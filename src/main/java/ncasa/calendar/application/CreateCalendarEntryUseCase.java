package ncasa.calendar.application;

import java.time.Clock;
import java.util.HashSet;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.domain.CalendarEntry;

public final class CreateCalendarEntryUseCase {
    private final CalendarEntryRepository entries; private final CalendarHouseholdAccessPort households; private final Clock clock;
    public CreateCalendarEntryUseCase(CalendarEntryRepository entries, CalendarHouseholdAccessPort households, Clock clock) {
        this.entries=entries; this.households=households; this.clock=clock;
    }
    public CalendarEntry execute(Long actor, UUID householdId, CalendarEntryCommand command) {
        var context=households.getContext(householdId,actor);
        var referenced=new HashSet<>(command.participantMemberIds()); referenced.addAll(command.reminderRecipientMemberIds());
        context.requireMembers(referenced); context.requireMember(command.relatedMemberId());
        var entry=CalendarEntry.create(UUID.randomUUID(),householdId,command.kind(),command.title(),command.timing(),
                command.color(),command.location(),command.note(),command.link(),command.participantMemberIds(),
                command.recurrence(),command.reminders(),command.reminderRecipientMemberIds(),command.specialDateType(),
                command.relatedMemberId(),context.actorMemberId());
        return entries.save(entry);
    }
}
