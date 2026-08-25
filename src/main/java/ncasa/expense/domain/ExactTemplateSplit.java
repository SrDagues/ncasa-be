package ncasa.expense.domain;

import java.util.*;

public record ExactTemplateSplit(List<ExpenseAllocation> allocations) implements TemplateSplit {
    public ExactTemplateSplit { allocations = List.copyOf(Objects.requireNonNull(allocations)); }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.EXACT; }
    @Override public List<MemberRef> participants() { return allocations.stream().map(ExpenseAllocation::memberId).toList(); }
    @Override public ExpenseSplit materialize(Money total) { return ExpenseSplit.exact(total, allocations); }
}
