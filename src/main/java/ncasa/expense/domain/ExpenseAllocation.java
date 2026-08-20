package ncasa.expense.domain;

import java.util.Objects;

public record ExpenseAllocation(MemberRef memberId, Money amount) {
    public ExpenseAllocation {
        Objects.requireNonNull(memberId, "Allocation member is required");
        Objects.requireNonNull(amount, "Allocation amount is required");
        if (!amount.isPositive()) throw new ExpenseRuleViolationException("Allocation amount must be positive");
    }
}
