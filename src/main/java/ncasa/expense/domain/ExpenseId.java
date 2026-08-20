package ncasa.expense.domain;

import java.util.UUID;

public record ExpenseId(UUID value) {
    public ExpenseId {
        if (value == null) throw new IllegalArgumentException("Expense id is required");
    }
}
