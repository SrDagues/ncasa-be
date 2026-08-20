package ncasa.expense.application;

public class ExpenseAccessDeniedException extends RuntimeException {
    public ExpenseAccessDeniedException(String message) { super(message); }
}
