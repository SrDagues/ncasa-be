package ncasa.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {
    public NotificationId { Objects.requireNonNull(value, "Notification id is required"); }
}
