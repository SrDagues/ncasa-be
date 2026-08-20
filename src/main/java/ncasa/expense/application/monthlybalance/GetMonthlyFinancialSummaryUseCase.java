package ncasa.expense.application.monthlybalance;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import ncasa.expense.application.port.out.FinancialLedgerReadPort;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.HouseholdRef;

public final class GetMonthlyFinancialSummaryUseCase {
    private final FinancialLedgerReadPort ledger; private final HouseholdExpenseAccessPort access;
    public GetMonthlyFinancialSummaryUseCase(FinancialLedgerReadPort ledger, HouseholdExpenseAccessPort access) { this.ledger=ledger; this.access=access; }
    public MonthlyFinancialSummary execute(Long actorAccountId, java.util.UUID householdId, YearMonth month) {
        if(month==null) throw new IllegalArgumentException("Month is required");
        var household=new HouseholdRef(householdId); var context=access.getContext(household,actorAccountId);
        var rows=ledger.expenseTotals(household,month.atDay(1),month.atEndOfMonth());
        var byCurrency=new TreeMap<String,java.util.Map<java.util.UUID,ncasa.expense.application.port.out.ExpenseLedgerRow>>();
        rows.forEach(r->byCurrency.computeIfAbsent(r.currency(),ignored->new java.util.HashMap<>()).put(r.memberId().value(),r));
        var currencies=new ArrayList<MonthlyFinancialSummary.CurrencySummary>();
        for(var entry:byCurrency.entrySet()){
            var members=context.allMemberIds().stream().sorted(Comparator.comparing(m->m.value())).map(member->{
                var row=entry.getValue().get(member.value()); var paid=row==null?BigDecimal.ZERO:row.paid(); var allocated=row==null?BigDecimal.ZERO:row.allocated();
                return new MonthlyFinancialSummary.MemberSummary(member.value(),paid,allocated,paid.subtract(allocated));
            }).toList();
            var total=members.stream().map(MonthlyFinancialSummary.MemberSummary::paid).reduce(BigDecimal.ZERO,BigDecimal::add);
            currencies.add(new MonthlyFinancialSummary.CurrencySummary(entry.getKey(),total,members));
        }
        return new MonthlyFinancialSummary(householdId,month,List.copyOf(currencies));
    }
}
