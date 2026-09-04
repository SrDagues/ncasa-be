package ncasa.expense.application.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DebtSummary(UUID householdId, LocalDate asOf, List<CurrencySummary> currencies) {
    public record CurrencySummary(String currency,List<MemberSummary> members,List<Suggestion> suggestedSettlements) {}
    public record MemberSummary(UUID memberId,BigDecimal paid,BigDecimal allocated,BigDecimal settledOut,BigDecimal settledIn,BigDecimal net) {}
    public record Suggestion(UUID fromMemberId,UUID toMemberId,BigDecimal amount) {}
}
