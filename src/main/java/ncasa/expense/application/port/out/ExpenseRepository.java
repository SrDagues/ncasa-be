package ncasa.expense.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import ncasa.expense.domain.Expense;
import ncasa.expense.domain.ExpenseId;
import ncasa.expense.domain.ExpenseStatus;
import ncasa.expense.domain.HouseholdRef;
import ncasa.expense.domain.MemberRef;

public interface ExpenseRepository {
    Expense save(Expense expense);
    Optional<Expense> findByIdAndHousehold(ExpenseId id, HouseholdRef householdId);
    ExpensePageSlice findPage(HouseholdRef householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, int page, int size);
    default ExpensePageSlice findPage(HouseholdRef householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, MemberRef payer, MemberRef participant, int page, int size) {
        if (payer != null || participant != null) throw new UnsupportedOperationException("Member filters are not supported");
        return findPage(householdId, from, to, status, page, size);
    }
}
