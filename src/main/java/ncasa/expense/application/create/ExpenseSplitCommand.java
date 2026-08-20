package ncasa.expense.application.create;

public sealed interface ExpenseSplitCommand permits EqualSplitCommand, ExactSplitCommand {}
