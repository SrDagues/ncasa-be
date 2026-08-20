package ncasa.household.domain;

import java.util.UUID;

public record HouseholdId(UUID value) {
    public HouseholdId {
        if (value == null) throw new IllegalArgumentException("Household id is required");
    }
}
