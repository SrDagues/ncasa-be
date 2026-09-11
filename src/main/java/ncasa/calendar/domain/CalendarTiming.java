package ncasa.calendar.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record CalendarTiming(boolean allDay, LocalDate startDate, LocalTime startTime,
        LocalDate endDate, LocalTime endTime) {
    public CalendarTiming {
        Objects.requireNonNull(startDate, "Start date is required");
        if (allDay) {
            if (startTime != null || endTime != null) throw new CalendarRuleViolationException("All-day entries cannot contain times");
            if (endDate != null && endDate.isBefore(startDate)) throw new CalendarRuleViolationException("End cannot precede start");
        } else {
            if (startTime == null) throw new CalendarRuleViolationException("Start time is required");
            if ((endDate == null) != (endTime == null)) throw new CalendarRuleViolationException("End date and time must be provided together");
            if (endDate != null && LocalDateTime.of(endDate, endTime).isBefore(LocalDateTime.of(startDate, startTime)))
                throw new CalendarRuleViolationException("End cannot precede start");
        }
    }

    public static CalendarTiming allDay(LocalDate start, LocalDate end) {
        return new CalendarTiming(true, start, null, end, null);
    }

    public static CalendarTiming timed(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        return new CalendarTiming(false, startDate, startTime, endDate, endTime);
    }

    public CalendarTiming shiftedTo(LocalDate date) {
        long span = endDate == null ? 0 : ChronoUnit.DAYS.between(startDate, endDate);
        return new CalendarTiming(allDay, date, startTime, endDate == null ? null : date.plusDays(span), endTime);
    }
}
