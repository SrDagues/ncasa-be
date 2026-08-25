package ncasa.expense.domain;

import java.time.LocalDate;
import java.util.Objects;

public sealed interface EndCondition permits UntilDate, AfterOccurrences {
    boolean allows(LocalDate date, long zeroBasedIndex);
    String type();
    default LocalDate endDate() { return null; }
    default Integer occurrenceLimit() { return null; }
    static EndCondition until(LocalDate date) { return new UntilDate(date); }
    static EndCondition afterOccurrences(int count) { return new AfterOccurrences(count); }
}

record UntilDate(LocalDate endDate) implements EndCondition {
    UntilDate { Objects.requireNonNull(endDate); }
    @Override public boolean allows(LocalDate date, long zeroBasedIndex) { return !date.isAfter(endDate); }
    @Override public String type() { return "UNTIL_DATE"; }
}

record AfterOccurrences(int totalOccurrences) implements EndCondition {
    AfterOccurrences { if (totalOccurrences < 1) throw new ExpenseRuleViolationException("Total occurrences must be positive"); }
    @Override public boolean allows(LocalDate date, long zeroBasedIndex) { return zeroBasedIndex < totalOccurrences; }
    @Override public String type() { return "AFTER_OCCURRENCES"; }
    @Override public Integer occurrenceLimit() { return totalOccurrences; }
}
