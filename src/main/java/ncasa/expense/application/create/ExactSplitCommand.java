package ncasa.expense.application.create;

import java.util.List;

public record ExactSplitCommand(List<ExactAllocationCommand> allocations) implements ExpenseSplitCommand {
    public ExactSplitCommand { allocations = allocations == null ? List.of() : List.copyOf(allocations); }
}
