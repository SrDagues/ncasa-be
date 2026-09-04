package ncasa.expense.application.port.out;

import java.util.List;
import java.util.Optional;
import ncasa.expense.domain.*;

public interface ExpenseCategoryRepository {
    ExpenseCategory save(ExpenseCategory category);
    Optional<ExpenseCategory> findByIdAndHousehold(ExpenseCategoryId id, HouseholdRef householdId);
    boolean existsActiveByName(HouseholdRef householdId, CategoryName name, ExpenseCategoryId excludedId);
    List<ExpenseCategory> findAll(HouseholdRef householdId, boolean includeArchived);
}
