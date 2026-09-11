package ncasa.calendar.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import ncasa.calendar.domain.*;

public record CalendarEntryCommand(CalendarEntryKind kind, String title, CalendarTiming timing, String color,
        String location, String note, String link, Set<UUID> participantMemberIds, RecurrenceRule recurrence,
        List<ReminderRule> reminders, Set<UUID> reminderRecipientMemberIds, SpecialDateType specialDateType,
        UUID relatedMemberId) {
    public CalendarEntryCommand {
        participantMemberIds=Set.copyOf(participantMemberIds==null?Set.of():participantMemberIds);
        reminders=List.copyOf(reminders==null?List.of():reminders);
        reminderRecipientMemberIds=Set.copyOf(reminderRecipientMemberIds==null?Set.of():reminderRecipientMemberIds);
    }
}
