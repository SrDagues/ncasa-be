package ncasa.expense.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ncasa.expense.domain.Expense;
import ncasa.expense.domain.ExpenseSource;
import ncasa.expense.domain.ExpenseSplitType;
import ncasa.expense.domain.ExpenseStatus;

public record ExpenseView(UUID id, UUID householdId, UUID createdByMemberId, UUID payerMemberId,
        BigDecimal amount, String currency, String description, LocalDate expenseDate,
        ExpenseSplitType splitType, List<AllocationView> allocations, ExpenseStatus status,
        ExpenseSource source, String voidReason, Instant createdAt, Instant updatedAt, Instant voidedAt,
        long version) {
    public record AllocationView(UUID memberId, BigDecimal amount) {}

    public static ExpenseView from(Expense expense) {
        var allocations = expense.split().allocations().stream()
                .map(allocation -> new AllocationView(allocation.memberId().value(), allocation.amount().amount()))
                .toList();
        return new ExpenseView(expense.id().value(), expense.householdId().value(),
                expense.createdByMemberId().value(), expense.payerMemberId().value(),
                expense.total().amount(), expense.total().currency(), expense.description().value(),
                expense.expenseDate(), expense.split().type(), allocations, expense.status(), expense.source(),
                expense.voidReason() == null ? null : expense.voidReason().value(), expense.createdAt(),
                expense.updatedAt(), expense.voidedAt(), expense.version());
    }
}
