package ncasa.expense.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DebtCalculator {
    private record Balance(MemberRef member, BigDecimal remaining) {}

    public List<SuggestedSettlement> calculate(List<MemberFinancialPosition> positions) {
        if (positions.isEmpty()) return List.of();
        String currency = positions.getFirst().net().currency();
        BigDecimal sum = BigDecimal.ZERO;
        var debtors = new ArrayList<Balance>();
        var creditors = new ArrayList<Balance>();
        for (var position : positions) {
            if (!currency.equals(position.net().currency()))
                throw new ExpenseRuleViolationException("Debt positions must use one currency");
            var amount = position.net().amount();
            sum = sum.add(amount);
            if (amount.signum() < 0) debtors.add(new Balance(position.memberId(), amount.abs()));
            if (amount.signum() > 0) creditors.add(new Balance(position.memberId(), amount));
        }
        if (sum.compareTo(BigDecimal.ZERO) != 0)
            throw new ExpenseRuleViolationException("Financial positions must sum to zero");
        Comparator<Balance> order = Comparator.comparing(Balance::remaining).reversed()
                .thenComparing(balance -> balance.member().value());
        debtors.sort(order); creditors.sort(order);
        var result = new ArrayList<SuggestedSettlement>();
        int debtor = 0, creditor = 0;
        while (debtor < debtors.size() && creditor < creditors.size()) {
            var d = debtors.get(debtor); var c = creditors.get(creditor);
            var amount = d.remaining().min(c.remaining());
            result.add(new SuggestedSettlement(d.member(), c.member(), new Money(amount, currency)));
            debtors.set(debtor, new Balance(d.member(), d.remaining().subtract(amount)));
            creditors.set(creditor, new Balance(c.member(), c.remaining().subtract(amount)));
            if (debtors.get(debtor).remaining().signum() == 0) debtor++;
            if (creditors.get(creditor).remaining().signum() == 0) creditor++;
        }
        return List.copyOf(result);
    }
}
