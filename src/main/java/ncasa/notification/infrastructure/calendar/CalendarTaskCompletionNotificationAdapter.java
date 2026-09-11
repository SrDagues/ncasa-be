package ncasa.notification.infrastructure.calendar;

import ncasa.calendar.application.port.out.TaskCompletionNotificationPort;
import ncasa.notification.application.CreateTaskCompletionNotificationsCommand;
import ncasa.notification.application.CreateTaskCompletionNotificationsUseCase;
import org.springframework.stereotype.Component;

@Component
public final class CalendarTaskCompletionNotificationAdapter implements TaskCompletionNotificationPort {
    private final CreateTaskCompletionNotificationsUseCase notifications;
    public CalendarTaskCompletionNotificationAdapter(CreateTaskCompletionNotificationsUseCase notifications){this.notifications=notifications;}
    @Override public void notify(TaskCompleted value){notifications.execute(new CreateTaskCompletionNotificationsCommand(
            value.entryId(),value.householdId(),value.title(),value.occurrenceDate(),value.participantMemberIds(),
            value.completedByMemberId(),value.occurredAt()));}
}
