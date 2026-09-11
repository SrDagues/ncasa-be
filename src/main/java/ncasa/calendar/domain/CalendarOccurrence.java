package ncasa.calendar.domain;

import java.time.LocalDate;
import java.util.UUID;

public record CalendarOccurrence(UUID itemId, String occurrenceKey, CalendarEntryKind kind,
        String title, CalendarTiming timing, String color, CalendarEntryStatus status, boolean recurring) {
    public LocalDate date() { return timing.startDate(); }
}
