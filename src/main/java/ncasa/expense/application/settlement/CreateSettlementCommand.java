package ncasa.expense.application.settlement;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record CreateSettlementCommand(Long actorAccountId,UUID householdId,UUID idempotencyKey,UUID fromMemberId,UUID toMemberId,BigDecimal amount,String currency,LocalDate settlementDate,String note){}
