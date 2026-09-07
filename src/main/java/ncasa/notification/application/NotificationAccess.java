package ncasa.notification.application;
import java.util.Set;import java.util.stream.Collectors;import ncasa.notification.application.port.out.HouseholdNotificationDirectoryPort;import ncasa.notification.domain.*;
final class NotificationAccess{private NotificationAccess(){}static Set<HouseholdRef> households(HouseholdNotificationDirectoryPort directory,Long account){return directory.activeHouseholdIds(account).stream().map(HouseholdRef::new).collect(Collectors.toUnmodifiableSet());}}
