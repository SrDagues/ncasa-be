package ncasa.expense.application;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException() { super("Expense not found"); }
}
