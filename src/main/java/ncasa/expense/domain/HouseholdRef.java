package ncasa.expense.domain;

import java.util.UUID;

public record HouseholdRef(UUID value) {
    public HouseholdRef {
        if (value == null) throw new IllegalArgumentException("Household reference is required");
    }
}
