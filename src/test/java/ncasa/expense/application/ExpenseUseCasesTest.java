package ncasa.expense.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ncasa.expense.application.create.*;
import ncasa.expense.application.get.GetExpenseUseCase;
import ncasa.expense.application.list.ListExpensesQuery;
import ncasa.expense.application.list.ListExpensesUseCase;
import ncasa.expense.application.port.out.*;
import ncasa.expense.application.voidexpense.VoidExpenseUseCase;
import ncasa.expense.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseUseCasesTest {
    private static final Instant NOW = Instant.parse("2026-08-20T10:00:00Z");
    private static final UUID HOUSEHOLD = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private InMemoryExpenses expenses;
    private HouseholdExpenseAccessPort access;

    @BeforeEach
    void setUp() {
        expenses = new InMemoryExpenses();
        access = (householdId, accountId) -> new ExpenseHouseholdContext(householdId,
                new MemberRef(ACTOR), accountId == 99L, Set.of(new MemberRef(ACTOR), new MemberRef(OTHER)));
    }

    @Test
    void shouldCreateEqualExpenseForActiveMembers() {
        var command = new CreateExpenseCommand(1L, HOUSEHOLD, "Groceries", new BigDecimal("10.00"),
                "EUR", LocalDate.of(2026, 8, 20), ACTOR, new EqualSplitCommand(List.of(ACTOR, OTHER)));

        ExpenseView result = new CreateExpenseUseCase(expenses, access, clock).execute(command);

        assertThat(result.status()).isEqualTo(ExpenseStatus.CONFIRMED);
        assertThat(result.createdByMemberId()).isEqualTo(ACTOR);
        assertThat(result.allocations()).extracting(ExpenseView.AllocationView::amount)
                .containsExactly(new BigDecimal("5.00"), new BigDecimal("5.00"));
        assertThat(expenses.values).hasSize(1);
    }

    @Test
    void shouldCreateExactExpense() {
        var split = new ExactSplitCommand(List.of(new ExactAllocationCommand(ACTOR, new BigDecimal("40")),
                new ExactAllocationCommand(OTHER, new BigDecimal("60"))));
        var command = new CreateExpenseCommand(1L, HOUSEHOLD, "Groceries", new BigDecimal("100"),
                "EUR", LocalDate.of(2026, 8, 20), ACTOR, split);

        ExpenseView result = new CreateExpenseUseCase(expenses, access, clock).execute(command);
        assertThat(result.splitType()).isEqualTo(ExpenseSplitType.EXACT);
    }

    @Test
    void shouldRejectParticipantOutsideHousehold() {
        UUID outsider = UUID.randomUUID();
        var command = new CreateExpenseCommand(1L, HOUSEHOLD, "Groceries", new BigDecimal("10"),
                "EUR", LocalDate.now(), ACTOR, new EqualSplitCommand(List.of(ACTOR, outsider)));
        assertThatThrownBy(() -> new CreateExpenseUseCase(expenses, access, clock).execute(command))
                .isInstanceOf(ExpenseAccessDeniedException.class);
    }

    @Test
    void shouldGetAndListExpenseForMember() {
        ExpenseView created = createExpense();
        ExpenseView found = new GetExpenseUseCase(expenses, access).execute(1L, HOUSEHOLD, created.id());
        ExpensePage page = new ListExpensesUseCase(expenses, access).execute(new ListExpensesQuery(
                1L, HOUSEHOLD, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                ExpenseStatus.CONFIRMED, 0, 20));
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(page.items()).singleElement().extracting(ExpenseView::id).isEqualTo(created.id());
    }

    @Test
    void shouldRejectInvalidHistoryDateRange() {
        var query = new ListExpensesQuery(1L, HOUSEHOLD, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 8, 1), ExpenseStatus.CONFIRMED, 0, 20);
        assertThatThrownBy(() -> new ListExpensesUseCase(expenses, access).execute(query))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHideUnknownExpense() {
        assertThatThrownBy(() -> new GetExpenseUseCase(expenses, access)
                .execute(1L, HOUSEHOLD, UUID.randomUUID()))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void shouldAllowCreatorAndAdministratorToVoidExpense() {
        ExpenseView created = createExpense();
        ExpenseView voided = new VoidExpenseUseCase(expenses, access, clock)
                .execute(1L, HOUSEHOLD, created.id(), "Duplicated");
        assertThat(voided.status()).isEqualTo(ExpenseStatus.VOIDED);
    }

    @Test
    void shouldRejectNonCreatorMemberVoidingExpense() {
        ExpenseView created = createExpense();
        HouseholdExpenseAccessPort otherAccess = (householdId, accountId) -> new ExpenseHouseholdContext(
                householdId, new MemberRef(OTHER), false, Set.of(new MemberRef(ACTOR), new MemberRef(OTHER)));
        assertThatThrownBy(() -> new VoidExpenseUseCase(expenses, otherAccess, clock)
                .execute(2L, HOUSEHOLD, created.id(), "No permission"))
                .isInstanceOf(ExpenseAccessDeniedException.class);
    }

    private ExpenseView createExpense() {
        return new CreateExpenseUseCase(expenses, access, clock).execute(new CreateExpenseCommand(1L, HOUSEHOLD,
                "Groceries", new BigDecimal("10"), "EUR", LocalDate.of(2026, 8, 20), ACTOR,
                new EqualSplitCommand(List.of(ACTOR, OTHER))));
    }

    private static final class InMemoryExpenses implements ExpenseRepository {
        private final Map<ExpenseId, Expense> values = new LinkedHashMap<>();
        public Expense save(Expense expense) { values.put(expense.id(), expense); return expense; }
        public Optional<Expense> findByIdAndHousehold(ExpenseId id, HouseholdRef householdId) {
            return Optional.ofNullable(values.get(id)).filter(value -> value.householdId().equals(householdId));
        }
        public ExpensePageSlice findPage(HouseholdRef householdId, LocalDate from, LocalDate to,
                ExpenseStatus status, int page, int size) {
            List<Expense> matches = new ArrayList<>(values.values().stream()
                    .filter(e -> e.householdId().equals(householdId))
                    .filter(e -> from == null || !e.expenseDate().isBefore(from))
                    .filter(e -> to == null || !e.expenseDate().isAfter(to))
                    .filter(e -> status == null || e.status() == status).toList());
            return new ExpensePageSlice(matches, matches.size());
        }
    }
}
