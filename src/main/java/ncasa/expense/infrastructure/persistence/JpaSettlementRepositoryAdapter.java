package ncasa.expense.infrastructure.persistence;
import java.time.LocalDate;import java.util.*;import ncasa.expense.application.port.out.*;import ncasa.expense.domain.*;import org.springframework.data.domain.PageRequest;import org.springframework.stereotype.Repository;
@Repository
public class JpaSettlementRepositoryAdapter implements SettlementRepository {
 private final SpringDataSettlementRepository repository;public JpaSettlementRepositoryAdapter(SpringDataSettlementRepository repository){this.repository=repository;}
 public Settlement save(Settlement s,UUID key){if(key==null)key=repository.findById(s.id().value()).orElseThrow().idempotencyKey;return toDomain(repository.saveAndFlush(toEntity(s,key)));}
 public Optional<Settlement> findByIdAndHousehold(SettlementId id,HouseholdRef h){return repository.findByIdAndHouseholdId(id.value(),h.value()).map(this::toDomain);}
 public Optional<Settlement> findByIdempotency(HouseholdRef h,MemberRef creator,UUID key){return repository.findByHouseholdIdAndCreatedByMemberIdAndIdempotencyKey(h.value(),creator.value(),key).map(this::toDomain);}
 public SettlementPageSlice findPage(HouseholdRef h,LocalDate from,LocalDate to,SettlementStatus status,MemberRef member,int page,int size){var result=repository.findPage(h.value(),from,to,status==null?null:status.name(),member==null?null:member.value(),PageRequest.of(page,size));return new SettlementPageSlice(result.getContent().stream().map(this::toDomain).toList(),result.getTotalElements());}
 private JpaSettlementEntity toEntity(Settlement s,UUID key){return new JpaSettlementEntity(s.id().value(),s.householdId().value(),s.createdByMemberId().value(),s.fromMemberId().value(),s.toMemberId().value(),s.money().amount(),s.money().currency(),s.settlementDate(),s.note(),s.status().name(),s.voidReason(),key,s.createdAt(),s.updatedAt(),s.voidedAt(),s.version());}
 private Settlement toDomain(JpaSettlementEntity e){return Settlement.rehydrate(new SettlementId(e.id),new HouseholdRef(e.householdId),new MemberRef(e.createdByMemberId),new MemberRef(e.fromMemberId),new MemberRef(e.toMemberId),new Money(e.amount,e.currency),e.settlementDate,e.note,SettlementStatus.valueOf(e.status),e.voidReason,e.createdAt,e.updatedAt,e.voidedAt,e.version);}
}
