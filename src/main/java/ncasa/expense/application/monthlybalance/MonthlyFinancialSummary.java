package ncasa.expense.application.monthlybalance;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record MonthlyFinancialSummary(UUID householdId, YearMonth month, List<CurrencySummary> currencies) {
    public record CurrencySummary(String currency, BigDecimal totalExpenses, List<MemberSummary> members,
            List<CategorySummary> categories) {
        public CurrencySummary(String currency,BigDecimal totalExpenses,List<MemberSummary> members){this(currency,totalExpenses,members,List.of());}
    }
    public record MemberSummary(UUID memberId, BigDecimal paid, BigDecimal allocated, BigDecimal net) {}
    public record CategorySummary(UUID categoryId,String name,BigDecimal total){}
}
