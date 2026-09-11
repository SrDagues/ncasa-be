package ncasa.calendar.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public record RecurrenceRule(RecurrenceFrequency frequency, RecurrenceEndType endType,
        LocalDate untilDate, Integer totalOccurrences) {
    public RecurrenceRule {
        Objects.requireNonNull(frequency, "Frequency is required");
        Objects.requireNonNull(endType, "End type is required");
        if (endType == RecurrenceEndType.UNTIL_DATE && untilDate == null)
            throw new CalendarRuleViolationException("Until date is required");
        if (endType != RecurrenceEndType.UNTIL_DATE && untilDate != null)
            throw new CalendarRuleViolationException("Until date is not allowed");
        if (endType == RecurrenceEndType.AFTER_OCCURRENCES && (totalOccurrences == null || totalOccurrences < 1))
            throw new CalendarRuleViolationException("Total occurrences must be positive");
        if (endType != RecurrenceEndType.AFTER_OCCURRENCES && totalOccurrences != null)
            throw new CalendarRuleViolationException("Occurrence count is not allowed");
    }

    public static RecurrenceRule never(RecurrenceFrequency frequency) {
        return new RecurrenceRule(frequency, RecurrenceEndType.NEVER, null, null);
    }
    public static RecurrenceRule until(RecurrenceFrequency frequency, LocalDate date) {
        return new RecurrenceRule(frequency, RecurrenceEndType.UNTIL_DATE, date, null);
    }
    public static RecurrenceRule afterOccurrences(RecurrenceFrequency frequency, int count) {
        return new RecurrenceRule(frequency, RecurrenceEndType.AFTER_OCCURRENCES, null, count);
    }

    public boolean allows(LocalDate date, long index) {
        return switch (endType) {
            case NEVER -> true;
            case UNTIL_DATE -> !date.isAfter(untilDate);
            case AFTER_OCCURRENCES -> index < totalOccurrences;
        };
    }

    public LocalDate occurrence(LocalDate anchor, long index) {
        if (index < 0) throw new CalendarRuleViolationException("Occurrence index cannot be negative");
        return switch (frequency) {
            case WEEKLY -> anchor.plusWeeks(index);
            case MONTHLY -> {
                var month = YearMonth.from(anchor).plusMonths(index);
                yield month.atDay(Math.min(anchor.getDayOfMonth(), month.lengthOfMonth()));
            }
            case YEARLY -> {
                int year = Math.addExact(anchor.getYear(), Math.toIntExact(index));
                var month = YearMonth.of(year, anchor.getMonth());
                yield month.atDay(Math.min(anchor.getDayOfMonth(), month.lengthOfMonth()));
            }
        };
    }
}
