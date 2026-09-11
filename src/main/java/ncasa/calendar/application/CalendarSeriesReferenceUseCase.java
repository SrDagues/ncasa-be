package ncasa.calendar.application;
import java.util.UUID;import ncasa.calendar.application.port.out.CalendarEntryRepository;
public final class CalendarSeriesReferenceUseCase{private final CalendarEntryRepository entries;public CalendarSeriesReferenceUseCase(CalendarEntryRepository e){entries=e;}public boolean exists(UUID householdId,UUID seriesId){return entries.activeSeriesExists(householdId,seriesId);}}
