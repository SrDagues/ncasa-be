package ncasa.expense.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import org.junit.jupiter.api.Test;

class ExpensePlanScheduleTest {
    @Test void monthlyScheduleKeepsItsAnchorAfterShortMonths() {
        Schedule schedule = Schedule.monthly(LocalDate.of(2026, 1, 31));
        assertEquals(LocalDate.of(2026, 2, 28), schedule.occurrence(1));
        assertEquals(LocalDate.of(2026, 3, 31), schedule.occurrence(2));
    }

    @Test void yearlyScheduleRestoresLeapDay() {
        Schedule schedule = Schedule.yearly(LocalDate.of(2024, 2, 29));
        assertEquals(LocalDate.of(2025, 2, 28), schedule.occurrence(1));
        assertEquals(LocalDate.of(2028, 2, 29), schedule.occurrence(4));
    }

    @Test void endDateIsInclusive() {
        var end = EndCondition.until(LocalDate.of(2026, 1, 15));
        assertTrue(end.allows(LocalDate.of(2026, 1, 15), 3));
        assertFalse(end.allows(LocalDate.of(2026, 1, 16), 4));
    }
}
