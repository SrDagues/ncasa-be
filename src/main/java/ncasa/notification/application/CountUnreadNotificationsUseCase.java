package ncasa.notification.application;
import ncasa.notification.application.port.out.*;import ncasa.notification.domain.AccountRef;
public final class CountUnreadNotificationsUseCase{private final NotificationRepository notifications;private final HouseholdNotificationDirectoryPort households;public CountUnreadNotificationsUseCase(NotificationRepository n,HouseholdNotificationDirectoryPort h){notifications=n;households=h;}public long execute(Long account){return notifications.countUnread(new AccountRef(account),NotificationAccess.households(households,account));}}
