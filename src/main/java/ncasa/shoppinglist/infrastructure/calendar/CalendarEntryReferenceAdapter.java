package ncasa.shoppinglist.infrastructure.calendar;
import java.util.UUID;import ncasa.calendar.application.CalendarSeriesReferenceUseCase;import ncasa.shoppinglist.application.port.out.CalendarEntryReferencePort;
public final class CalendarEntryReferenceAdapter implements CalendarEntryReferencePort{private final CalendarSeriesReferenceUseCase calendar;public CalendarEntryReferenceAdapter(CalendarSeriesReferenceUseCase c){calendar=c;}public boolean activeSeriesExists(UUID household,UUID series){return calendar.exists(household,series);}}
