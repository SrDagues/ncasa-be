package ncasa.expense.application.category;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class CreateExpenseCategoryUseCase {
    private final ExpenseCategoryRepository categories; private final HouseholdExpenseAccessPort household; private final Clock clock;
    public CreateExpenseCategoryUseCase(ExpenseCategoryRepository categories, HouseholdExpenseAccessPort household, Clock clock) {
        this.categories = categories; this.household = household; this.clock = clock;
    }
    public ExpenseCategoryView execute(Long actorAccountId, UUID householdId, String rawName) {
        var scope = new HouseholdRef(householdId); var context = household.getContext(scope, actorAccountId);
        if (!context.administrator()) throw new ExpenseAccessDeniedException("Only administrators can create categories");
        var name = new CategoryName(rawName);
        if (categories.existsActiveByName(scope, name, null)) throw new CategoryConflictException("An active category with this name already exists");
        return ExpenseCategoryView.from(categories.save(ExpenseCategory.create(new ExpenseCategoryId(UUID.randomUUID()),
                scope, context.actorMemberId(), name, clock.instant())));
    }
}
