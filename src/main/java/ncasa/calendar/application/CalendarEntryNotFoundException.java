package ncasa.calendar.application;

public final class CalendarEntryNotFoundException extends RuntimeException {
    public CalendarEntryNotFoundException() { super("Calendar entry not found"); }
}
