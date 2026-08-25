package ncasa.expense.domain;

import java.util.List;

public sealed interface TemplateSplit permits EqualTemplateSplit, ExactTemplateSplit, PercentageTemplateSplit {
    ExpenseSplitType type();
    List<MemberRef> participants();
    ExpenseSplit materialize(Money total);
}
