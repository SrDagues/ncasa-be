package ncasa.expense.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataExpenseCategoryRepository extends JpaRepository<JpaExpenseCategoryEntity, UUID> {
    Optional<JpaExpenseCategoryEntity> findByIdAndHouseholdId(UUID id, UUID householdId);
    List<JpaExpenseCategoryEntity> findByHouseholdIdOrderByNameAscIdAsc(UUID householdId);
    List<JpaExpenseCategoryEntity> findByHouseholdIdAndStatusOrderByNameAscIdAsc(UUID householdId, String status);
    @Query("select count(c)>0 from JpaExpenseCategoryEntity c where c.householdId=:householdId and c.status='ACTIVE' and lower(c.name)=lower(:name) and (:excludedId is null or c.id<>:excludedId)")
    boolean existsActiveByName(@Param("householdId") UUID householdId, @Param("name") String name,
            @Param("excludedId") UUID excludedId);
}
