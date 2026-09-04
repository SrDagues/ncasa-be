package ncasa.expense.application.classification;

import java.util.*;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class ListExpenseClassificationHistoryUseCase {
    private final ExpenseRepository expenses;private final ExpenseClassificationAuditRepository audit;private final HouseholdExpenseAccessPort household;
    public ListExpenseClassificationHistoryUseCase(ExpenseRepository expenses,ExpenseClassificationAuditRepository audit,HouseholdExpenseAccessPort household){this.expenses=expenses;this.audit=audit;this.household=household;}
    public List<ExpenseClassificationView> execute(Long actorAccountId,UUID householdId,UUID expenseId){
        var scope=new HouseholdRef(householdId);household.getContext(scope,actorAccountId);var id=new ExpenseId(expenseId);
        if(expenses.findByIdAndHousehold(id,scope).isEmpty())throw new ExpenseNotFoundException();
        return audit.findByExpense(id,scope).stream().map(ExpenseClassificationView::from).toList();
    }
}
