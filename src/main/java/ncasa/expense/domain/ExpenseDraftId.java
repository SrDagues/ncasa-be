package ncasa.expense.domain;

import java.util.Objects;
import java.util.UUID;

public record ExpenseDraftId(UUID value) {
    public ExpenseDraftId { Objects.requireNonNull(value, "Draft id is required"); }
}
