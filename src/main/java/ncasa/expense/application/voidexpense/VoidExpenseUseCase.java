package ncasa.expense.application.voidexpense;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.ExpenseAccessDeniedException;
import ncasa.expense.application.ExpenseNotFoundException;
import ncasa.expense.application.ExpenseView;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.*;

public final class VoidExpenseUseCase {
    private final ExpenseRepository expenses;
    private final HouseholdExpenseAccessPort householdAccess;
    private final Clock clock;
    public VoidExpenseUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess, Clock clock) {
        this.expenses = expenses; this.householdAccess = householdAccess; this.clock = clock;
    }
    public ExpenseView execute(Long actorAccountId, UUID householdId, UUID expenseId, String reason) {
        var household = new HouseholdRef(householdId);
        var context = householdAccess.getContext(household, actorAccountId);
        var expense = expenses.findByIdAndHousehold(new ExpenseId(expenseId), household)
                .orElseThrow(ExpenseNotFoundException::new);
        if (!context.administrator() && !context.actorMemberId().equals(expense.createdByMemberId())) {
            throw new ExpenseAccessDeniedException("Only the creator or an administrator can void this expense");
        }
        expense.voidExpense(new VoidReason(reason), clock.instant());
        return ExpenseView.from(expenses.save(expense));
    }
}
