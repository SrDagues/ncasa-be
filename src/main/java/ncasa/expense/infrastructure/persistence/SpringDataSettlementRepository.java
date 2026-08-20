package ncasa.expense.infrastructure.persistence;
import java.time.LocalDate;import java.util.*;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
interface SpringDataSettlementRepository extends JpaRepository<JpaSettlementEntity,UUID>{
 Optional<JpaSettlementEntity> findByIdAndHouseholdId(UUID id,UUID householdId);
 Optional<JpaSettlementEntity> findByHouseholdIdAndCreatedByMemberIdAndIdempotencyKey(UUID household,UUID creator,UUID key);
 @Query(value="select s from JpaSettlementEntity s where s.householdId=:household and (:fromDate is null or s.settlementDate>=:fromDate) and (:toDate is null or s.settlementDate<=:toDate) and (:status is null or s.status=:status) and (:member is null or s.fromMemberId=:member or s.toMemberId=:member) order by s.settlementDate desc,s.createdAt desc,s.id desc",
 countQuery="select count(s) from JpaSettlementEntity s where s.householdId=:household and (:fromDate is null or s.settlementDate>=:fromDate) and (:toDate is null or s.settlementDate<=:toDate) and (:status is null or s.status=:status) and (:member is null or s.fromMemberId=:member or s.toMemberId=:member)")
 Page<JpaSettlementEntity> findPage(@Param("household") UUID household,@Param("fromDate") LocalDate from,@Param("toDate") LocalDate to,@Param("status") String status,@Param("member") UUID member,Pageable pageable);
}
