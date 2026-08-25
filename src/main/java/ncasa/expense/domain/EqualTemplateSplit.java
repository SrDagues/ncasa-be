package ncasa.expense.domain;

import java.util.*;

public record EqualTemplateSplit(List<MemberRef> participants) implements TemplateSplit {
    public EqualTemplateSplit { participants = validated(participants); }
    private static List<MemberRef> validated(List<MemberRef> values) {
        if (values == null || values.isEmpty()) throw new ExpenseRuleViolationException("At least one participant is required");
        var copy = List.copyOf(values);
        if (new HashSet<>(copy).size() != copy.size()) throw new ExpenseRuleViolationException("Participants must be unique");
        return copy;
    }
    @Override public ExpenseSplitType type() { return ExpenseSplitType.EQUAL; }
    @Override public ExpenseSplit materialize(Money total) { return ExpenseSplit.equal(total, participants); }
}
