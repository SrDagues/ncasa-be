package ncasa.expense.application.get;

import java.util.UUID;
import ncasa.expense.application.ExpenseNotFoundException;
import ncasa.expense.application.ExpenseView;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.ExpenseId;
import ncasa.expense.domain.HouseholdRef;

public final class GetExpenseUseCase {
    private final ExpenseRepository expenses;
    private final HouseholdExpenseAccessPort householdAccess;
    public GetExpenseUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess) {
        this.expenses = expenses; this.householdAccess = householdAccess;
    }
    public ExpenseView execute(Long actorAccountId, UUID householdId, UUID expenseId) {
        var household = new HouseholdRef(householdId);
        householdAccess.getContext(household, actorAccountId);
        return expenses.findByIdAndHousehold(new ExpenseId(expenseId), household)
                .map(ExpenseView::from).orElseThrow(ExpenseNotFoundException::new);
    }
}
