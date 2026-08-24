package ncasa.expense.application.create;

import java.util.List;

public record PercentageSplitCommand(List<PercentageAllocationCommand> allocations) implements ExpenseSplitCommand {
    public PercentageSplitCommand { allocations = allocations == null ? List.of() : List.copyOf(allocations); }
}
