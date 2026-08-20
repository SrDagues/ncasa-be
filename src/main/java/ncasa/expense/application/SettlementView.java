package ncasa.expense.application;

import java.math.BigDecimal; import java.time.*; import java.util.UUID;
import ncasa.expense.domain.*;
public record SettlementView(UUID id,UUID householdId,UUID createdByMemberId,UUID fromMemberId,UUID toMemberId,
        BigDecimal amount,String currency,LocalDate settlementDate,String note,SettlementStatus status,String voidReason,
        Instant createdAt,Instant updatedAt,Instant voidedAt,long version){
    public static SettlementView from(Settlement s){return new SettlementView(s.id().value(),s.householdId().value(),s.createdByMemberId().value(),s.fromMemberId().value(),s.toMemberId().value(),s.money().amount(),s.money().currency(),s.settlementDate(),s.note(),s.status(),s.voidReason(),s.createdAt(),s.updatedAt(),s.voidedAt(),s.version());}
}
