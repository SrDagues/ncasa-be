package ncasa.expense.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import ncasa.expense.domain.Expense;
import ncasa.expense.domain.ExpenseId;
import ncasa.expense.domain.ExpenseStatus;
import ncasa.expense.domain.ExpenseCategoryId;
import ncasa.expense.domain.ExpenseSplitType;
import ncasa.expense.domain.ExpenseSource;
import ncasa.expense.domain.ExpensePlanId;
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
    default ExpensePageSlice findPage(HouseholdRef householdId,LocalDate from,LocalDate to,ExpenseStatus status,
            MemberRef payer,MemberRef participant,ExpenseCategoryId category,boolean uncategorized,
            ExpenseSplitType splitType,int page,int size){
        if(category!=null||uncategorized||splitType!=null)throw new UnsupportedOperationException("Classification filters are not supported");
        return findPage(householdId,from,to,status,payer,participant,page,size);
    }
    default ExpensePageSlice findPage(HouseholdRef householdId,LocalDate from,LocalDate to,ExpenseStatus status,
            MemberRef payer,MemberRef participant,ExpenseCategoryId category,boolean uncategorized,
            ExpenseSplitType splitType,ExpenseSource source,ExpensePlanId planId,int page,int size){
        if(source!=null||planId!=null)throw new UnsupportedOperationException("Plan filters are not supported");
        return findPage(householdId,from,to,status,payer,participant,category,uncategorized,splitType,page,size);
    }
}
