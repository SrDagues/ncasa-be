package ncasa.expense.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ExpensePlanTest {
    private static final Instant NOW = Instant.parse("2026-08-25T10:00:00Z");

    @Test void countLimitedPlanCompletesOnlyAfterAllMaterializedOccurrences() {
        var plan = plan(3);
        plan.occurrenceMaterialized(LocalDate.of(2026, 9, 1), NOW);
        plan.occurrenceMaterialized(LocalDate.of(2026, 10, 1), NOW.plusSeconds(1));
        plan.occurrenceMaterialized(LocalDate.of(2026, 11, 1), NOW.plusSeconds(2));
        assertEquals(ExpensePlanStatus.COMPLETED, plan.status());
        assertEquals(3, plan.materializedOccurrences());
    }

    @Test void reactivationSkipsPastDatesWithoutConsumingInstallments() {
        var plan = plan(3);
        plan.pause("holiday", NOW);
        plan.reactivate(LocalDate.of(2026, 11, 10), NOW.plusSeconds(1));
        assertEquals(LocalDate.of(2026, 12, 1), plan.nextOccurrence());
        assertEquals(0, plan.materializedOccurrences());
    }

    @Test void cancellationIsTerminal() {
        var plan = plan(3);
        plan.cancel("contract ended", NOW);
        assertThrows(ExpensePlanStateException.class,
                () -> plan.reactivate(LocalDate.of(2026, 9, 1), NOW));
    }

    private ExpensePlan plan(int occurrences) {
        UUID member = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var template = new ExpenseTemplate(new ExpenseDescription("Rent"), Money.of("900", "EUR"),
                new MemberRef(member), null, new EqualTemplateSplit(List.of(new MemberRef(member))));
        return ExpensePlan.create(new ExpensePlanId(UUID.randomUUID()), new HouseholdRef(UUID.randomUUID()),
                new MemberRef(member), template, Schedule.monthly(LocalDate.of(2026, 9, 1)),
                EndCondition.afterOccurrences(occurrences), ZoneId.of("Europe/Madrid"), 1, NOW);
    }
}
