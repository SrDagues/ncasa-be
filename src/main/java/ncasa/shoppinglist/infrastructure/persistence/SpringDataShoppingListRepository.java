package ncasa.shoppinglist.infrastructure.persistence;
import jakarta.persistence.LockModeType;import java.util.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
interface SpringDataShoppingListRepository extends JpaRepository<JpaShoppingListEntity,UUID>{
 Optional<JpaShoppingListEntity> findByIdAndHouseholdId(UUID id,UUID householdId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)@Query("select l from JpaShoppingListEntity l where l.id=:id and l.householdId=:household")
 Optional<JpaShoppingListEntity> findForContentUpdate(@Param("id")UUID id,@Param("household")UUID householdId);
 List<JpaShoppingListEntity> findByHouseholdIdAndStatusOrderByUpdatedAtDescIdAsc(UUID householdId,String status);
 Optional<JpaShoppingListEntity> findByCalendarSeriesIdAndStatus(UUID seriesId,String status);
 @Query("select count(l)>0 from JpaShoppingListEntity l where l.householdId=:household and l.status='ACTIVE' and l.normalizedName=:name and (:excluded is null or l.id<>:excluded)")
 boolean existsActiveName(@Param("household")UUID household,@Param("name")String name,@Param("excluded")UUID excluded);
 @Modifying(clearAutomatically=true,flushAutomatically=true)
 @Query("update JpaShoppingListEntity l set l.contentRevision=l.contentRevision+1,l.updatedAt=:now where l.id=:id and l.status='ACTIVE'")
 int touchContent(@Param("id")UUID id,@Param("now")java.time.Instant now);
 @Modifying(clearAutomatically=true,flushAutomatically=true)
 @Query("update JpaShoppingListEntity l set l.contentRevision=l.contentRevision+1,l.updatedAt=:now where l.id=:id and l.status='ACTIVE' and l.contentRevision=:revision")
 int advanceContentRevision(@Param("id")UUID id,@Param("revision")long revision,@Param("now")java.time.Instant now);
}
