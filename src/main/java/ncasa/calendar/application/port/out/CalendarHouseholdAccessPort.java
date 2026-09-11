package ncasa.calendar.application.port.out;

import java.util.UUID;
import ncasa.calendar.application.CalendarHouseholdContext;

public interface CalendarHouseholdAccessPort {
    CalendarHouseholdContext getContext(UUID householdId, Long actorAccountId);
}
