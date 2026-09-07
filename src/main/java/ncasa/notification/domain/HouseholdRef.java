package ncasa.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record HouseholdRef(UUID value) {
    public HouseholdRef { Objects.requireNonNull(value, "Household id is required"); }
}
