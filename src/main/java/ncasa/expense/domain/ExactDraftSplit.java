package ncasa.expense.domain;

import java.util.HashSet;
import java.util.List;

public record ExactDraftSplit(List<ExpenseAllocation> allocations) implements DraftSplit {
    public ExactDraftSplit {
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
        if (new HashSet<>(allocations.stream().map(ExpenseAllocation::memberId).toList()).size() != allocations.size()) {
            throw new ExpenseRuleViolationException("A member cannot appear twice in a draft split");
        }
    }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.EXACT; }
}
