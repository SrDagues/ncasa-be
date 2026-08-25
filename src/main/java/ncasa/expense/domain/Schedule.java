package ncasa.expense.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public record Schedule(ExpensePlanFrequency frequency, LocalDate startDate) {
    public Schedule { Objects.requireNonNull(frequency); Objects.requireNonNull(startDate); }
    public static Schedule once(LocalDate start) { return new Schedule(ExpensePlanFrequency.ONCE, start); }
    public static Schedule weekly(LocalDate start) { return new Schedule(ExpensePlanFrequency.WEEKLY, start); }
    public static Schedule monthly(LocalDate start) { return new Schedule(ExpensePlanFrequency.MONTHLY, start); }
    public static Schedule yearly(LocalDate start) { return new Schedule(ExpensePlanFrequency.YEARLY, start); }

    public LocalDate occurrence(long index) {
        if (index < 0) throw new IllegalArgumentException("Occurrence index cannot be negative");
        return switch (frequency) {
            case ONCE -> { if (index > 0) throw new IllegalArgumentException("One-time schedule has one occurrence"); yield startDate; }
            case WEEKLY -> startDate.plusWeeks(index);
            case MONTHLY -> {
                YearMonth month = YearMonth.from(startDate).plusMonths(index);
                yield month.atDay(Math.min(startDate.getDayOfMonth(), month.lengthOfMonth()));
            }
            case YEARLY -> {
                int year = Math.addExact(startDate.getYear(), Math.toIntExact(index));
                YearMonth month = YearMonth.of(year, startDate.getMonth());
                yield month.atDay(Math.min(startDate.getDayOfMonth(), month.lengthOfMonth()));
            }
        };
    }
}
