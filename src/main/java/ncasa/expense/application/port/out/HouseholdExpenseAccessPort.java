package ncasa.expense.application.port.out;

import ncasa.expense.application.ExpenseHouseholdContext;
import ncasa.expense.domain.HouseholdRef;

public interface HouseholdExpenseAccessPort {
    ExpenseHouseholdContext getContext(HouseholdRef householdId, Long actorAccountId);
}
