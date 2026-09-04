package ncasa.expense.domain;

import java.util.Objects;
import java.util.UUID;

public record ExpensePlanId(UUID value) implements Comparable<ExpensePlanId> {
    public ExpensePlanId { Objects.requireNonNull(value); }
    @Override public int compareTo(ExpensePlanId other) { return value.compareTo(other.value); }
}
