package ncasa.expense.domain;

import java.util.*;

public record PercentageTemplateSplit(List<PercentageAllocation> percentages) implements TemplateSplit {
    public PercentageTemplateSplit { percentages = List.copyOf(Objects.requireNonNull(percentages)); }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.PERCENTAGE; }
    @Override public List<MemberRef> participants() { return percentages.stream().map(PercentageAllocation::memberId).toList(); }
    @Override public ExpenseSplit materialize(Money total) { return ExpenseSplit.percentage(total, percentages); }
}
