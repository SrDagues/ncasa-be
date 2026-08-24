package ncasa.expense.application.category;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class RenameExpenseCategoryUseCase {
    private final ExpenseCategoryRepository categories; private final HouseholdExpenseAccessPort household; private final Clock clock;
    public RenameExpenseCategoryUseCase(ExpenseCategoryRepository categories, HouseholdExpenseAccessPort household, Clock clock) {
        this.categories = categories; this.household = household; this.clock = clock;
    }
    public ExpenseCategoryView execute(Long actorAccountId, UUID householdId, UUID categoryId, String rawName) {
        var scope = new HouseholdRef(householdId); var context = household.getContext(scope, actorAccountId);
        if (!context.administrator()) throw new ExpenseAccessDeniedException("Only administrators can rename categories");
        var id = new ExpenseCategoryId(categoryId); var category = categories.findByIdAndHousehold(id, scope)
                .orElseThrow(() -> new CategoryNotFoundException("Expense category not found"));
        var name = new CategoryName(rawName);
        if (categories.existsActiveByName(scope, name, id)) throw new CategoryConflictException("An active category with this name already exists");
        category.rename(name, clock.instant()); return ExpenseCategoryView.from(categories.save(category));
    }
}
