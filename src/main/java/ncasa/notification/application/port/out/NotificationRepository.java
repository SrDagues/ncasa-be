package ncasa.notification.application.port.out;

import java.time.Instant;import java.util.*;import ncasa.notification.domain.*;

public interface NotificationRepository {
    boolean exists(IntegrationEventId eventId,AccountRef recipient);
    void saveAll(List<Notification> notifications);
    NotificationPageSlice findPage(AccountRef account,Set<HouseholdRef> accessibleHouseholds,boolean unreadOnly,int page,int size);
    long countUnread(AccountRef account,Set<HouseholdRef> accessibleHouseholds);
    Optional<Notification> findAccessible(NotificationId id,AccountRef account,Set<HouseholdRef> accessibleHouseholds);
    Notification save(Notification notification);
    int markAllRead(AccountRef account,Set<HouseholdRef> accessibleHouseholds,Instant readAt);
}
