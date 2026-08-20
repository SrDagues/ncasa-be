package ncasa.expense.infrastructure.web;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ncasa.expense.application.ExpenseView;
import ncasa.expense.domain.ExpenseSource;
import ncasa.expense.domain.ExpenseSplitType;
import ncasa.expense.domain.ExpenseStatus;

public record ExpenseResponse(UUID id, UUID householdId, UUID createdByMemberId, UUID payerMemberId,
        String amount, String currency, String description, LocalDate expenseDate,
        ExpenseSplitType splitType, List<AllocationResponse> allocations, ExpenseStatus status,
        ExpenseSource source, String voidReason, Instant createdAt, Instant updatedAt, Instant voidedAt,
        long version) {
    public record AllocationResponse(UUID memberId, String amount) {}

    static ExpenseResponse from(ExpenseView view) {
        var allocations = view.allocations().stream()
                .map(item -> new AllocationResponse(item.memberId(), item.amount().toPlainString())).toList();
        return new ExpenseResponse(view.id(), view.householdId(), view.createdByMemberId(), view.payerMemberId(),
                view.amount().toPlainString(), view.currency(), view.description(), view.expenseDate(),
                view.splitType(), allocations, view.status(), view.source(), view.voidReason(), view.createdAt(),
                view.updatedAt(), view.voidedAt(), view.version());
    }
}
