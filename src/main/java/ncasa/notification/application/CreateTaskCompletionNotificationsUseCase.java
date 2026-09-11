package ncasa.notification.application;

import java.time.Clock;
import java.util.UUID;
import ncasa.notification.application.port.out.HouseholdNotificationDirectoryPort;
import ncasa.notification.application.port.out.NotificationRepository;
import ncasa.notification.domain.*;

public final class CreateTaskCompletionNotificationsUseCase {
    private final NotificationRepository notifications;
    private final HouseholdNotificationDirectoryPort households;
    private final NotificationRecipientPolicy policy;
    private final Clock clock;

    public CreateTaskCompletionNotificationsUseCase(NotificationRepository notifications,
            HouseholdNotificationDirectoryPort households, NotificationRecipientPolicy policy, Clock clock) {
        this.notifications=notifications;this.households=households;this.policy=policy;this.clock=clock;
    }

    public int execute(CreateTaskCompletionNotificationsCommand command) {
        var directory=households.get(command.householdId());
        if(!directory.active())return 0;
        var candidates=directory.members().stream().map(member->new NotificationRecipientPolicy.Candidate(
                member.memberId(),member.accountId(),member.active(),member.administrator())).toList();
        var recipients=policy.taskCompletionRecipients(command.participantMemberIds(),command.completedByMemberId(),candidates);
        var eventId=new IntegrationEventId(UUID.randomUUID());
        var householdId=new HouseholdRef(command.householdId());
        var entryId=new CalendarEntryRef(command.entryId());
        var now=clock.instant();
        var created=recipients.stream().map(recipient->Notification.taskCompleted(new NotificationId(UUID.randomUUID()),
                eventId,recipient,householdId,entryId,command.title(),command.occurrenceDate(),
                command.completedByMemberId(),command.occurredAt(),now)).toList();
        if(!created.isEmpty())notifications.saveAll(created);
        return created.size();
    }
}
