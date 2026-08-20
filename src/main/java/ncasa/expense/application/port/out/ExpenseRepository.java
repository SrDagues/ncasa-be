package ncasa.expense.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import ncasa.expense.domain.Expense;
import ncasa.expense.domain.ExpenseId;
import ncasa.expense.domain.ExpenseStatus;
import ncasa.expense.domain.HouseholdRef;

public interface ExpenseRepository {
    Expense save(Expense expense);
    Optional<Expense> findByIdAndHousehold(ExpenseId id, HouseholdRef householdId);
    ExpensePageSlice findPage(HouseholdRef householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, int page, int size);
}
