package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PercentageSplitTest {
    private static final MemberRef A = new MemberRef(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final MemberRef B = new MemberRef(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final MemberRef C = new MemberRef(UUID.fromString("00000000-0000-0000-0000-000000000003"));

    @Test void shouldMaterializePercentageSplitExactly() {
        var split = ExpenseSplit.percentage(Money.of("10.00", "EUR"), List.of(
                allocation(A, "33.33"), allocation(B, "33.33"), allocation(C, "33.34")));
        assertThat(split.type()).isEqualTo(ExpenseSplitType.PERCENTAGE);
        assertThat(split.allocations()).extracting(item -> item.amount().amount())
                .containsExactly(new BigDecimal("3.33"), new BigDecimal("3.33"), new BigDecimal("3.34"));
    }

    @Test void shouldAssignEqualRemaindersByMemberReference() {
        var split = ExpenseSplit.percentage(Money.of("0.05", "EUR"), List.of(
                allocation(C, "20.00"), allocation(B, "40.00"), allocation(A, "40.00")));
        assertThat(split.allocations()).extracting(item -> item.amount().amount())
                .containsExactly(new BigDecimal("0.02"), new BigDecimal("0.02"), new BigDecimal("0.01"));
    }

    @Test void shouldRejectPercentageTotalDifferentFromOneHundred() {
        assertThatThrownBy(() -> ExpenseSplit.percentage(Money.of("10", "EUR"),
                List.of(allocation(A, "50"), allocation(B, "40"))))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    @Test void shouldRejectDuplicateMembersAndZeroMaterializedAmounts() {
        assertThatThrownBy(() -> ExpenseSplit.percentage(Money.of("10", "EUR"),
                List.of(allocation(A, "50"), allocation(A, "50"))))
                .isInstanceOf(ExpenseRuleViolationException.class);
        assertThatThrownBy(() -> ExpenseSplit.percentage(Money.of("0.01", "EUR"),
                List.of(allocation(A, "50"), allocation(B, "50"))))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    private PercentageAllocation allocation(MemberRef member, String percentage) {
        return new PercentageAllocation(member, Percentage.of(new BigDecimal(percentage)));
    }
}
