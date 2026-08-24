package ncasa.expense.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.expense.domain.*;

public record ExpenseCategoryView(UUID id, UUID householdId, UUID createdByMemberId, String name,
        ExpenseCategoryStatus status, Instant createdAt, Instant updatedAt, Instant archivedAt, long version) {
    public static ExpenseCategoryView from(ExpenseCategory category) {
        return new ExpenseCategoryView(category.id().value(), category.householdId().value(),
                category.createdByMemberId().value(), category.name().value(), category.status(),
                category.createdAt(), category.updatedAt(), category.archivedAt(), category.version());
    }
}
