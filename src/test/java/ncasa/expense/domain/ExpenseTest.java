package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseTest {
    private static final Instant NOW = Instant.parse("2026-08-20T10:00:00Z");

    @Test
    void shouldCreateConfirmedManualExpense() {
        Expense expense = expense();
        assertThat(expense.status()).isEqualTo(ExpenseStatus.CONFIRMED);
        assertThat(expense.source()).isEqualTo(ExpenseSource.MANUAL);
        assertThat(expense.split().allocations()).hasSize(2);
    }

    @Test
    void shouldVoidExpenseWithoutLosingAccountingData() {
        Expense expense = expense();
        ExpenseSplit originalSplit = expense.split();

        expense.voidExpense(new VoidReason("Duplicated"), NOW.plusSeconds(60));

        assertThat(expense.status()).isEqualTo(ExpenseStatus.VOIDED);
        assertThat(expense.voidReason().value()).isEqualTo("Duplicated");
        assertThat(expense.split()).isSameAs(originalSplit);
    }

    @Test
    void shouldRejectSecondVoiding() {
        Expense expense = expense();
        expense.voidExpense(new VoidReason("Duplicated"), NOW.plusSeconds(60));
        assertThatThrownBy(() -> expense.voidExpense(new VoidReason("Again"), NOW.plusSeconds(120)))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    @Test
    void shouldRejectSplitBuiltForDifferentTotal() {
        var member = new MemberRef(UUID.randomUUID());
        Money splitTotal = Money.of("10", "EUR");
        assertThatThrownBy(() -> Expense.confirmedManual(new ExpenseId(UUID.randomUUID()),
                new HouseholdRef(UUID.randomUUID()), member, member, Money.of("20", "EUR"),
                new ExpenseDescription("Groceries"), LocalDate.of(2026, 8, 20),
                ExpenseSplit.equal(splitTotal, List.of(member)), NOW))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    private Expense expense() {
        var first = new MemberRef(UUID.randomUUID());
        var second = new MemberRef(UUID.randomUUID());
        Money total = Money.of("100", "EUR");
        return Expense.confirmedManual(new ExpenseId(UUID.randomUUID()), new HouseholdRef(UUID.randomUUID()),
                first, first, total, new ExpenseDescription("Groceries"), LocalDate.of(2026, 8, 20),
                ExpenseSplit.equal(total, List.of(first, second)), NOW);
    }
}
