package ncasa.expense.application.settlement;
import java.time.LocalDate;import java.util.UUID;import ncasa.expense.domain.SettlementStatus;
public record ListSettlementsQuery(Long actorAccountId,UUID householdId,LocalDate from,LocalDate to,SettlementStatus status,UUID memberId,int page,int size){}
