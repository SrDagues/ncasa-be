package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseSplitTest {
    private static final MemberRef FIRST = new MemberRef(new UUID(0, 1));
    private static final MemberRef SECOND = new MemberRef(new UUID(0, 2));
    private static final MemberRef THIRD = new MemberRef(new UUID(0, 3));

    @Test
    void shouldSplitRemainderDeterministically() {
        ExpenseSplit split = ExpenseSplit.equal(Money.of("10.00", "EUR"), List.of(THIRD, FIRST, SECOND));

        assertThat(split.allocations()).extracting(a -> a.memberId().value())
                .containsExactly(FIRST.value(), SECOND.value(), THIRD.value());
        assertThat(split.allocations()).extracting(a -> a.amount().amount().toPlainString())
                .containsExactly("3.34", "3.33", "3.33");
    }

    @Test
    void shouldAcceptExactAllocationsThatMatchTotal() {
        ExpenseSplit split = ExpenseSplit.exact(Money.of("100", "EUR"), List.of(
                new ExpenseAllocation(FIRST, Money.of("40", "EUR")),
                new ExpenseAllocation(SECOND, Money.of("60", "EUR"))));
        assertThat(split.type()).isEqualTo(ExpenseSplitType.EXACT);
    }

    @Test
    void shouldRejectAllocationsThatDoNotMatchTotal() {
        assertThatThrownBy(() -> ExpenseSplit.exact(Money.of("100", "EUR"), List.of(
                new ExpenseAllocation(FIRST, Money.of("40", "EUR")))))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        assertThatThrownBy(() -> ExpenseSplit.equal(Money.of("10", "EUR"), List.of(FIRST, FIRST)))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }
}
