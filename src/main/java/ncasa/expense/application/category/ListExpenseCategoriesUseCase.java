package ncasa.expense.application.category;

import java.util.List;
import java.util.UUID;
import ncasa.expense.application.ExpenseCategoryView;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.HouseholdRef;

public final class ListExpenseCategoriesUseCase {
    private final ExpenseCategoryRepository categories; private final HouseholdExpenseAccessPort household;
    public ListExpenseCategoriesUseCase(ExpenseCategoryRepository categories, HouseholdExpenseAccessPort household) {
        this.categories = categories; this.household = household;
    }
    public List<ExpenseCategoryView> execute(Long actorAccountId, UUID householdId, boolean includeArchived) {
        var scope = new HouseholdRef(householdId); household.getContext(scope, actorAccountId);
        return categories.findAll(scope, includeArchived).stream().map(ExpenseCategoryView::from).toList();
    }
}
