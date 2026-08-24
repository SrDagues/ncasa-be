package ncasa.expense.application.port.out;

import java.util.List;
import ncasa.expense.domain.*;

public interface ExpenseClassificationAuditRepository {
    ExpenseClassificationChange save(ExpenseClassificationChange change);
    List<ExpenseClassificationChange> findByExpense(ExpenseId expenseId,HouseholdRef householdId);
}
