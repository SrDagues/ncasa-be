package ncasa.calendar.domain;

public record ReminderRule(int daysBefore, boolean enabled) {
    public ReminderRule {
        if (daysBefore < 0 || daysBefore > 366) throw new CalendarRuleViolationException("Reminder days must be between 0 and 366");
    }
}
