package ncasa.expense.domain;

import java.util.Objects;

public record SuggestedSettlement(MemberRef fromMemberId, MemberRef toMemberId, Money amount) {
    public SuggestedSettlement {
        Objects.requireNonNull(fromMemberId); Objects.requireNonNull(toMemberId); Objects.requireNonNull(amount);
        if (fromMemberId.equals(toMemberId) || !amount.isPositive())
            throw new ExpenseRuleViolationException("Suggested settlement must transfer a positive amount between members");
    }
}
