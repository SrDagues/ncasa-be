package ncasa.expense.domain;

public class ExpenseStateException extends ExpenseRuleViolationException {
    public ExpenseStateException(String message) { super(message); }
}
