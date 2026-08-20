package ncasa.expense.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataExpenseRepository extends JpaRepository<JpaExpenseEntity, UUID> {
    @EntityGraph(attributePaths = "allocations")
    Optional<JpaExpenseEntity> findByIdAndHouseholdId(UUID id, UUID householdId);

    @Query(value = "select e.id from JpaExpenseEntity e where e.householdId = :householdId "
            + "and (:fromDate is null or e.expenseDate >= :fromDate) "
            + "and (:toDate is null or e.expenseDate <= :toDate) "
            + "and (:status is null or e.status = :status) "
            + "order by e.expenseDate desc, e.createdAt desc, e.id desc",
            countQuery = "select count(e) from JpaExpenseEntity e where e.householdId = :householdId "
                    + "and (:fromDate is null or e.expenseDate >= :fromDate) "
                    + "and (:toDate is null or e.expenseDate <= :toDate) "
                    + "and (:status is null or e.status = :status)")
    Page<UUID> findPageIds(@Param("householdId") UUID householdId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("status") String status, Pageable pageable);

    @EntityGraph(attributePaths = "allocations")
    List<JpaExpenseEntity> findByIdIn(Collection<UUID> ids);
}
