package ncasa.calendar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalendarEntryTest {
    private static final UUID HOUSEHOLD = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();

    @Test
    void shouldProjectWeeklyOccurrencesUntilTheConfiguredCount() {
        var entry = entry(CalendarTiming.allDay(LocalDate.of(2026, 9, 7), null),
                RecurrenceRule.afterOccurrences(RecurrenceFrequency.WEEKLY, 3));

        assertThat(entry.occurrencesBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1)))
                .extracting(CalendarOccurrence::date)
                .containsExactly(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 21));
    }

    @Test
    void shouldKeepMonthlyAnchorAndRestoreLeapDay() {
        var monthly = entry(CalendarTiming.allDay(LocalDate.of(2026, 1, 31), null),
                RecurrenceRule.afterOccurrences(RecurrenceFrequency.MONTHLY, 3));
        var yearly = entry(CalendarTiming.allDay(LocalDate.of(2024, 2, 29), null),
                RecurrenceRule.afterOccurrences(RecurrenceFrequency.YEARLY, 5));

        assertThat(monthly.occurrencesBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1)))
                .extracting(CalendarOccurrence::date)
                .containsExactly(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31));
        assertThat(yearly.occurrencesBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2029, 1, 1)))
                .extracting(CalendarOccurrence::date)
                .containsExactly(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28),
                        LocalDate.of(2026, 2, 28), LocalDate.of(2027, 2, 28), LocalDate.of(2028, 2, 29));
    }

    @Test
    void shouldCompleteOnlyOneOccurrence() {
        var entry = entry(CalendarTiming.allDay(LocalDate.of(2026, 9, 7), null),
                RecurrenceRule.never(RecurrenceFrequency.WEEKLY));

        entry.complete("2026-09-14");

        var occurrences = entry.occurrencesBetween(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 21));
        assertThat(occurrences).extracting(CalendarOccurrence::status)
                .containsExactly(CalendarEntryStatus.PENDING, CalendarEntryStatus.COMPLETED,
                        CalendarEntryStatus.PENDING);
    }

    @Test
    void shouldRejectRecurrenceForEventsAndInvalidEndTime() {
        assertThatThrownBy(() -> CalendarTiming.timed(LocalDate.of(2026, 9, 7), LocalTime.NOON,
                LocalDate.of(2026, 9, 7), LocalTime.of(11, 0))).isInstanceOf(CalendarRuleViolationException.class);
        assertThatThrownBy(() -> CalendarEntry.create(UUID.randomUUID(), HOUSEHOLD, CalendarEntryKind.EVENT,
                "Cena", CalendarTiming.allDay(LocalDate.of(2026, 9, 7), null), "#123456", null, null, null,
                Set.of(), RecurrenceRule.never(RecurrenceFrequency.WEEKLY), List.of(), Set.of(), null, null, ACTOR))
                .isInstanceOf(CalendarRuleViolationException.class);
    }

    @Test
    void shouldRequireSpecialDateTypeOnlyForSpecialDates() {
        assertThatThrownBy(() -> CalendarEntry.create(UUID.randomUUID(), HOUSEHOLD,
                CalendarEntryKind.SPECIAL_DATE, "Fecha", CalendarTiming.allDay(LocalDate.now(), null), "#abcdef",
                null, null, null, Set.of(), null, List.of(), Set.of(), null, null, ACTOR))
                .isInstanceOf(CalendarRuleViolationException.class);
    }

    @Test
    void shouldRepeatBirthdaysYearlyForeverWhenNoRecurrenceWasExplicitlyConfigured() {
        var birthday = CalendarEntry.create(UUID.randomUUID(), HOUSEHOLD, CalendarEntryKind.SPECIAL_DATE,
                "Cumpleaños", CalendarTiming.allDay(LocalDate.of(2026, 5, 12), null), "#abcdef",
                null, null, null, Set.of(), null, List.of(), Set.of(), SpecialDateType.BIRTHDAY, null, ACTOR);

        assertThat(birthday.occurrencesBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2029, 12, 31)))
                .extracting(CalendarOccurrence::date)
                .containsExactly(LocalDate.of(2026, 5, 12), LocalDate.of(2027, 5, 12),
                        LocalDate.of(2028, 5, 12), LocalDate.of(2029, 5, 12));
    }

    private CalendarEntry entry(CalendarTiming timing, RecurrenceRule recurrence) {
        return CalendarEntry.create(UUID.randomUUID(), HOUSEHOLD, CalendarEntryKind.TASK, "Tarea", timing,
                "#123456", null, null, null, Set.of(), recurrence, List.of(), Set.of(), null, null, ACTOR);
    }
}
