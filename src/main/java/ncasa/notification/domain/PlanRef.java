package ncasa.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record PlanRef(UUID value) {
    public PlanRef { Objects.requireNonNull(value, "Plan id is required"); }
}
