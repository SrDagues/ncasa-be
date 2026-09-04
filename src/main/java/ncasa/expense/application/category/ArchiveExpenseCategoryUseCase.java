package ncasa.expense.application.category;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class ArchiveExpenseCategoryUseCase {
    private final ExpenseCategoryRepository categories; private final HouseholdExpenseAccessPort household; private final Clock clock;
    public ArchiveExpenseCategoryUseCase(ExpenseCategoryRepository categories, HouseholdExpenseAccessPort household, Clock clock) {
        this.categories = categories; this.household = household; this.clock = clock;
    }
    public ExpenseCategoryView execute(Long actorAccountId, UUID householdId, UUID categoryId) {
        var scope = new HouseholdRef(householdId); var context = household.getContext(scope, actorAccountId);
        if (!context.administrator()) throw new ExpenseAccessDeniedException("Only administrators can archive categories");
        var category = categories.findByIdAndHousehold(new ExpenseCategoryId(categoryId), scope)
                .orElseThrow(() -> new CategoryNotFoundException("Expense category not found"));
        category.archive(clock.instant()); return ExpenseCategoryView.from(categories.save(category));
    }
}
