package ncasa.expense.domain;

import java.util.Objects;
import java.util.UUID;

public record ExpenseCategoryId(UUID value) implements Comparable<ExpenseCategoryId> {
    public ExpenseCategoryId { Objects.requireNonNull(value, "Category id is required"); }
    @Override public int compareTo(ExpenseCategoryId other) { return value.compareTo(other.value); }
}
