package ncasa.calendar.application;

import java.util.List;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.domain.CalendarEntry;

public final class ListTrashedCalendarEntriesUseCase {
    private final CalendarEntryRepository entries;private final CalendarHouseholdAccessPort households;
    public ListTrashedCalendarEntriesUseCase(CalendarEntryRepository e,CalendarHouseholdAccessPort h){entries=e;households=h;}
    public List<CalendarEntry> execute(Long actor,UUID householdId){households.getContext(householdId,actor);return entries.findTrashed(householdId);}
}
