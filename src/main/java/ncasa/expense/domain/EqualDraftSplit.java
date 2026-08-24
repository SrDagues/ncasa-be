package ncasa.expense.domain;

import java.util.HashSet;
import java.util.List;

public record EqualDraftSplit(List<MemberRef> members) implements DraftSplit {
    public EqualDraftSplit {
        members = members == null ? List.of() : List.copyOf(members);
        if (new HashSet<>(members).size() != members.size()) {
            throw new ExpenseRuleViolationException("A member cannot appear twice in a draft split");
        }
    }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.EQUAL; }
}
