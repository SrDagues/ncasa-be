package ncasa.expense.application.classification;

import java.time.Clock;
import java.util.*;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class ReclassifyExpenseUseCase {
    private final ExpenseRepository expenses;private final ExpenseCategoryRepository categories;
    private final ExpenseClassificationAuditRepository audit;private final HouseholdExpenseAccessPort household;private final Clock clock;
    public ReclassifyExpenseUseCase(ExpenseRepository expenses,ExpenseCategoryRepository categories,
            ExpenseClassificationAuditRepository audit,HouseholdExpenseAccessPort household,Clock clock){
        this.expenses=expenses;this.categories=categories;this.audit=audit;this.household=household;this.clock=clock;
    }
    public ExpenseView execute(Long actorAccountId,UUID householdId,UUID expenseId,UUID rawCategoryId,String reason){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        var expense=expenses.findByIdAndHousehold(new ExpenseId(expenseId),scope).orElseThrow(ExpenseNotFoundException::new);
        if(!context.administrator()&&!expense.createdByMemberId().equals(context.actorMemberId()))throw new ExpenseAccessDeniedException("Only the creator or an administrator can reclassify an expense");
        ExpenseCategoryId categoryId=null;if(rawCategoryId!=null){var category=categories.findByIdAndHousehold(new ExpenseCategoryId(rawCategoryId),scope)
                .orElseThrow(()->new CategoryNotFoundException("Expense category not found"));category.requireActive();categoryId=category.id();}
        var changedAt=clock.instant();var result=expense.reclassify(categoryId,changedAt);
        if(Objects.equals(result.previousCategoryId(),result.newCategoryId()))return ExpenseView.from(expense);
        var saved=expenses.save(expense);audit.save(new ExpenseClassificationChange(UUID.randomUUID(),saved.id(),scope,context.actorMemberId(),
                result.previousCategoryId(),result.newCategoryId(),reason,changedAt));return ExpenseView.from(saved);
    }
}
