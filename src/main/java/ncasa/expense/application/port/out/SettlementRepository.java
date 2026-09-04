package ncasa.expense.application.port.out;
import java.time.LocalDate; import java.util.*; import ncasa.expense.domain.*;
public interface SettlementRepository {
    Settlement save(Settlement settlement,UUID idempotencyKey);
    Optional<Settlement> findByIdAndHousehold(SettlementId id,HouseholdRef householdId);
    Optional<Settlement> findByIdempotency(HouseholdRef householdId,MemberRef creator,UUID key);
    SettlementPageSlice findPage(HouseholdRef householdId,LocalDate from,LocalDate to,SettlementStatus status,MemberRef member,int page,int size);
}
