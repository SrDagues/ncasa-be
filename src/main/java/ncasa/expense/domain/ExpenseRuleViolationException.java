package ncasa.expense.domain;

public class ExpenseRuleViolationException extends RuntimeException {
    public ExpenseRuleViolationException(String message) {
        super(message);
    }
}
