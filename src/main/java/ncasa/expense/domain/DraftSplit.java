package ncasa.expense.domain;

public sealed interface DraftSplit permits EqualDraftSplit, ExactDraftSplit, PercentageDraftSplit {
    ExpenseSplitType type();
}
