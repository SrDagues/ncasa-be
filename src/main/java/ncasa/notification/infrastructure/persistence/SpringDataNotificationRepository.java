package ncasa.notification.infrastructure.persistence;

import java.time.Instant;import java.util.*;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;

interface SpringDataNotificationRepository extends JpaRepository<JpaNotificationEntity,UUID>{
    boolean existsByEventIdAndRecipientAccountId(UUID eventId,Long recipientAccountId);
    @Query("select n from JpaNotificationEntity n where n.recipientAccountId=:account and n.householdId in :households and (:unread=false or n.readAt is null) order by n.createdAt desc,n.id desc")
    Page<JpaNotificationEntity> page(@Param("account")Long account,@Param("households")Set<UUID> households,@Param("unread")boolean unread,Pageable pageable);
    @Query("select count(n) from JpaNotificationEntity n where n.recipientAccountId=:account and n.householdId in :households and n.readAt is null")
    long unread(@Param("account")Long account,@Param("households")Set<UUID> households);
    @Query("select n from JpaNotificationEntity n where n.id=:id and n.recipientAccountId=:account and n.householdId in :households")
    Optional<JpaNotificationEntity> accessible(@Param("id")UUID id,@Param("account")Long account,@Param("households")Set<UUID> households);
    @Modifying(clearAutomatically=true,flushAutomatically=true)
    @Query("update JpaNotificationEntity n set n.readAt=:readAt where n.recipientAccountId=:account and n.householdId in :households and n.readAt is null")
    int markAllRead(@Param("account")Long account,@Param("households")Set<UUID> households,@Param("readAt")Instant readAt);
}
