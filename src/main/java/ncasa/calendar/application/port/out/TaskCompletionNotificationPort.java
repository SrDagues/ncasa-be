package ncasa.calendar.application.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface TaskCompletionNotificationPort {
    void notify(TaskCompleted notification);

    record TaskCompleted(UUID entryId, UUID householdId, String title, LocalDate occurrenceDate,
            Set<UUID> participantMemberIds, UUID completedByMemberId, Instant occurredAt) {
        public TaskCompleted {
            participantMemberIds = Set.copyOf(participantMemberIds);
        }
    }

    TaskCompletionNotificationPort NONE = notification -> {};
}
