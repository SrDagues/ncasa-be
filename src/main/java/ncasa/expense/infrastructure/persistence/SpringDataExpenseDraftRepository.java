package ncasa.expense.infrastructure.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface SpringDataExpenseDraftRepository extends JpaRepository<JpaExpenseDraftEntity,UUID>{
    @Query("select distinct d from JpaExpenseDraftEntity d left join fetch d.allocations where d.id=:id and d.householdId=:householdId")
    Optional<JpaExpenseDraftEntity> findComplete(@Param("id")UUID id,@Param("householdId")UUID householdId);
    @Query("select distinct d from JpaExpenseDraftEntity d left join fetch d.allocations where d.householdId=:householdId and d.createdByMemberId=:creator and (:status is null or d.status=:status) order by d.updatedAt desc,d.id desc")
    List<JpaExpenseDraftEntity> findByCreator(@Param("householdId")UUID householdId,@Param("creator")UUID creator,@Param("status")String status);
}
