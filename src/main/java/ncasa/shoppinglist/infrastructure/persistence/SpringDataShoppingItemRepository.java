package ncasa.shoppinglist.infrastructure.persistence;
import java.time.Instant;import java.util.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
interface SpringDataShoppingItemRepository extends JpaRepository<JpaShoppingItemEntity,UUID>{
 Optional<JpaShoppingItemEntity> findByIdAndListId(UUID id,UUID listId);List<JpaShoppingItemEntity> findByListId(UUID listId);
 @Query("select coalesce(max(i.position),-1)+1 from JpaShoppingItemEntity i where i.listId=:list and i.status='PENDING'")long nextPendingPosition(@Param("list")UUID listId);
 @Modifying @Query("delete from JpaShoppingItemEntity i where i.listId=:list and i.status='PURCHASED'")int deletePurchased(@Param("list")UUID listId);
 @Modifying @Query("update JpaShoppingItemEntity i set i.responsibleMemberId=null,i.updatedAt=:now where i.responsibleMemberId=:member and i.listId in (select l.id from JpaShoppingListEntity l where l.householdId=:household)")
 int unassign(@Param("household")UUID household,@Param("member")UUID member,@Param("now")Instant now);
 @Query("select distinct i.listId from JpaShoppingItemEntity i where i.responsibleMemberId=:member and i.listId in (select l.id from JpaShoppingListEntity l where l.householdId=:household)")
 List<UUID> assignedListIds(@Param("household")UUID household,@Param("member")UUID member);
}
