package ncasa.expense.domain;

public record ExpenseDescription(String value) {
    public static final int MAX_LENGTH = 240;

    public ExpenseDescription {
        if (value == null || value.isBlank()) throw new ExpenseRuleViolationException("Description is required");
        value = value.trim();
        if (value.length() > MAX_LENGTH) throw new ExpenseRuleViolationException("Description is too long");
    }
}
