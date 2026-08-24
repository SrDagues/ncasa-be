package ncasa.expense.domain;

public record CategoryName(String value) {
    public static final int MAX_LENGTH = 80;

    public CategoryName {
        if (value == null || value.isBlank()) throw new ExpenseRuleViolationException("Category name is required");
        value = value.trim();
        if (value.length() > MAX_LENGTH) throw new ExpenseRuleViolationException("Category name is too long");
    }
}
