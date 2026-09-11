package ncasa.calendar.application;

import java.util.UUID;
import ncasa.calendar.application.port.out.*;
import ncasa.calendar.domain.CalendarEntry;

public final class GetCalendarEntryUseCase {
    private final CalendarEntryRepository entries;private final CalendarHouseholdAccessPort households;
    public GetCalendarEntryUseCase(CalendarEntryRepository e,CalendarHouseholdAccessPort h){entries=e;households=h;}
    public CalendarEntry execute(Long actor,UUID householdId,UUID id){households.getContext(householdId,actor);return entries.find(id,householdId).orElseThrow(CalendarEntryNotFoundException::new);}
}
