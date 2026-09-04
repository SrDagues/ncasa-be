package ncasa.expense.application.debt;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class GetDebtSummaryUseCase {
    private record Totals(BigDecimal paid,BigDecimal allocated,BigDecimal out,BigDecimal in) {
        static Totals zero(){return new Totals(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);}
    }
    private final FinancialLedgerReadPort ledger; private final HouseholdExpenseAccessPort access; private final Clock clock; private final DebtCalculator calculator;
    public GetDebtSummaryUseCase(FinancialLedgerReadPort ledger,HouseholdExpenseAccessPort access,Clock clock,DebtCalculator calculator){this.ledger=ledger;this.access=access;this.clock=clock;this.calculator=calculator;}
    public DebtSummary execute(Long actorAccountId,UUID householdId){
        var household=new HouseholdRef(householdId);var context=access.getContext(household,actorAccountId);var asOf=LocalDate.now(clock);
        var data=new TreeMap<String,Map<MemberRef,Totals>>();
        ledger.expenseTotals(household,null,asOf).forEach(r->data.computeIfAbsent(r.currency(),x->new HashMap<>()).put(r.memberId(),new Totals(r.paid(),r.allocated(),BigDecimal.ZERO,BigDecimal.ZERO)));
        ledger.settlementTotals(household,asOf).forEach(r->{var map=data.computeIfAbsent(r.currency(),x->new HashMap<>());var old=map.getOrDefault(r.memberId(),Totals.zero());map.put(r.memberId(),new Totals(old.paid(),old.allocated(),r.settledOut(),r.settledIn()));});
        var currencies=new ArrayList<DebtSummary.CurrencySummary>();
        for(var entry:data.entrySet()){
            var members=context.allMemberIds().stream().sorted(Comparator.comparing(m->m.value())).map(member->{var t=entry.getValue().getOrDefault(member,Totals.zero());var net=t.paid().subtract(t.allocated()).add(t.out()).subtract(t.in());return new DebtSummary.MemberSummary(member.value(),t.paid(),t.allocated(),t.out(),t.in(),net);}).toList();
            var positions=members.stream().map(m->new MemberFinancialPosition(new MemberRef(m.memberId()),new Money(m.net(),entry.getKey()))).toList();
            var suggestions=calculator.calculate(positions).stream().map(s->new DebtSummary.Suggestion(s.fromMemberId().value(),s.toMemberId().value(),s.amount().amount())).toList();
            currencies.add(new DebtSummary.CurrencySummary(entry.getKey(),members,suggestions));
        }
        return new DebtSummary(householdId,asOf,List.copyOf(currencies));
    }
}
