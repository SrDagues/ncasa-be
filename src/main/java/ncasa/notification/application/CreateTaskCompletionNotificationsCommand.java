package ncasa.notification.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateTaskCompletionNotificationsCommand(UUID entryId, UUID householdId, String title,
        LocalDate occurrenceDate, Set<UUID> participantMemberIds, UUID completedByMemberId, Instant occurredAt) {
    public CreateTaskCompletionNotificationsCommand {
        participantMemberIds = Set.copyOf(participantMemberIds);
    }
}
