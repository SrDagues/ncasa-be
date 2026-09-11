package ncasa.notification.infrastructure.config;

import java.time.Clock;import ncasa.notification.application.*;import ncasa.notification.application.port.out.*;import ncasa.notification.domain.NotificationRecipientPolicy;import org.springframework.context.annotation.*;

@Configuration public class NotificationConfiguration{
    @Bean NotificationRecipientPolicy notificationRecipientPolicy(){return new NotificationRecipientPolicy();}
    @Bean ConsumeExpensePlanNotificationUseCase consumeExpensePlanNotification(NotificationRepository n,ExpensePlanNotificationSourcePort p,HouseholdNotificationDirectoryPort h,NotificationRecipientPolicy r,Clock c){return new ConsumeExpensePlanNotificationUseCase(n,p,h,r,c);}
    @Bean CreateTaskCompletionNotificationsUseCase createTaskCompletionNotifications(NotificationRepository n,HouseholdNotificationDirectoryPort h,NotificationRecipientPolicy r,Clock c){return new CreateTaskCompletionNotificationsUseCase(n,h,r,c);}
    @Bean ListNotificationsUseCase listNotifications(NotificationRepository n,HouseholdNotificationDirectoryPort h){return new ListNotificationsUseCase(n,h);}
    @Bean CountUnreadNotificationsUseCase countUnreadNotifications(NotificationRepository n,HouseholdNotificationDirectoryPort h){return new CountUnreadNotificationsUseCase(n,h);}
    @Bean MarkNotificationReadUseCase markNotificationRead(NotificationRepository n,HouseholdNotificationDirectoryPort h,Clock c){return new MarkNotificationReadUseCase(n,h,c);}
    @Bean MarkAllNotificationsReadUseCase markAllNotificationsRead(NotificationRepository n,HouseholdNotificationDirectoryPort h,Clock c){return new MarkAllNotificationsReadUseCase(n,h,c);}
}
