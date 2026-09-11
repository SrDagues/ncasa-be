package ncasa.calendar.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.domain.CalendarEntryKind;

public final class ListCalendarSeriesOptionsUseCase {
    private final CalendarEntryRepository entries;
    private final CalendarHouseholdAccessPort access;

    public ListCalendarSeriesOptionsUseCase(CalendarEntryRepository entries, CalendarHouseholdAccessPort access) {
        this.entries = entries;
        this.access = access;
    }

    public List<CalendarSeriesOption> execute(Long actorId, UUID householdId) {
        access.getContext(householdId, actorId);
        var bySeries = new LinkedHashMap<UUID, CalendarSeriesOption>();
        entries.findActive(householdId).stream()
                .sorted(Comparator.comparing(entry -> entry.timing().startDate()))
                .forEach(entry -> bySeries.put(entry.seriesId(), new CalendarSeriesOption(
                        entry.seriesId(), entry.title(), entry.kind(), entry.timing().startDate())));
        return bySeries.values().stream()
                .sorted(Comparator.comparing(CalendarSeriesOption::title, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(CalendarSeriesOption::seriesId))
                .toList();
    }

    public record CalendarSeriesOption(UUID seriesId, String title, CalendarEntryKind kind, LocalDate startDate) {}
}
