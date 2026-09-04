package ncasa.expense.domain;

import java.util.Objects;

public record ExpenseTemplate(ExpenseDescription description, Money total, MemberRef payerMemberId,
        ExpenseCategoryId categoryId, TemplateSplit split) {
    public ExpenseTemplate {
        Objects.requireNonNull(description); Objects.requireNonNull(total); Objects.requireNonNull(payerMemberId); Objects.requireNonNull(split);
        if (!total.isPositive()) throw new ExpenseRuleViolationException("Expense plan amount must be positive");
        split.materialize(total);
    }
}
