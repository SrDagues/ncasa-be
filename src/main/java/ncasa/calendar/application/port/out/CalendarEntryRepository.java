package ncasa.calendar.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ncasa.calendar.domain.CalendarEntry;

public interface CalendarEntryRepository {
    CalendarEntry save(CalendarEntry entry);
    Optional<CalendarEntry> find(UUID id, UUID householdId);
    List<CalendarEntry> findActive(UUID householdId);
    List<CalendarEntry> findTrashed(UUID householdId);
    void delete(CalendarEntry entry);
}
