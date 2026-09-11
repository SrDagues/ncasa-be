package ncasa.notification.domain;

import java.util.Objects;
import java.util.UUID;

public record CalendarEntryRef(UUID value) {
    public CalendarEntryRef { Objects.requireNonNull(value, "Calendar entry id is required"); }
}
