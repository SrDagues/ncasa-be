package ncasa.expense.application.create;

import java.util.List;
import java.util.UUID;

public record EqualSplitCommand(List<UUID> memberIds) implements ExpenseSplitCommand {
    public EqualSplitCommand { memberIds = memberIds == null ? List.of() : List.copyOf(memberIds); }
}
