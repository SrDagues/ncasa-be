package ncasa.calendar.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.domain.CalendarOccurrence;

public final class ListCalendarOccurrencesUseCase {
    private final CalendarEntryRepository entries; private final CalendarHouseholdAccessPort households;
    public ListCalendarOccurrencesUseCase(CalendarEntryRepository entries,CalendarHouseholdAccessPort households){this.entries=entries;this.households=households;}
    public List<CalendarOccurrence> execute(Long actor,UUID householdId,LocalDate from,LocalDate to){
        households.getContext(householdId,actor);
        if(from==null||to==null||from.isAfter(to)||to.isAfter(from.plusMonths(2))) throw new IllegalArgumentException("Calendar range must be at most two months");
        return entries.findActive(householdId).stream().flatMap(e->e.occurrencesBetween(from,to).stream())
                .sorted(Comparator.comparing(CalendarOccurrence::date).thenComparing(CalendarOccurrence::title)).toList();
    }
}
