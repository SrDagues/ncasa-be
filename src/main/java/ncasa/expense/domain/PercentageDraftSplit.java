package ncasa.expense.domain;

import java.util.HashSet;
import java.util.List;

public record PercentageDraftSplit(List<PercentageAllocation> allocations) implements DraftSplit {
    public PercentageDraftSplit {
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
        if (new HashSet<>(allocations.stream().map(PercentageAllocation::memberId).toList()).size() != allocations.size()) {
            throw new ExpenseRuleViolationException("A member cannot appear twice in a draft split");
        }
    }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.PERCENTAGE; }
}
