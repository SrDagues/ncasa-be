package ncasa.household.infrastructure.persistence;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataHouseholdRepository extends JpaRepository<JpaHouseholdEntity, UUID> {
    @EntityGraph(attributePaths = "members")
    @Query("select distinct h from JpaHouseholdEntity h join h.members m "
            + "where h.status = 'ACTIVE' and m.accountId = :accountId and m.status = 'ACTIVE'")
    List<JpaHouseholdEntity> findActiveByMemberAccountId(@Param("accountId") Long accountId);
}
