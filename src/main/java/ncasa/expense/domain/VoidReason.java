package ncasa.expense.domain;

public record VoidReason(String value) {
    public static final int MAX_LENGTH = 500;

    public VoidReason {
        if (value == null || value.isBlank()) throw new ExpenseRuleViolationException("Void reason is required");
        value = value.trim();
        if (value.length() > MAX_LENGTH) throw new ExpenseRuleViolationException("Void reason is too long");
    }
}
