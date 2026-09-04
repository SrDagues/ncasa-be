package ncasa.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record IntegrationEventId(UUID value) {
    public IntegrationEventId { Objects.requireNonNull(value, "Event id is required"); }
}
