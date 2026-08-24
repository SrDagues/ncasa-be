package ncasa.expense.application.draft;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class ConfirmExpenseDraftUseCase {
    private final ExpenseDraftRepository drafts;private final ExpenseRepository expenses;
    private final ExpenseCategoryRepository categories;private final HouseholdExpenseAccessPort household;private final Clock clock;
    public ConfirmExpenseDraftUseCase(ExpenseDraftRepository drafts,ExpenseRepository expenses,
            ExpenseCategoryRepository categories,HouseholdExpenseAccessPort household,Clock clock){
        this.drafts=drafts;this.expenses=expenses;this.categories=categories;this.household=household;this.clock=clock;
    }
    public ExpenseView execute(Long actorAccountId,UUID householdId,UUID draftId,long version){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        var draft=drafts.findByIdAndHousehold(new ExpenseDraftId(draftId),scope).orElseThrow(()->new DraftNotFoundException("Expense draft not found"));
        if(!draft.createdByMemberId().equals(context.actorMemberId()))throw new ExpenseAccessDeniedException("Expense drafts are private");
        if(draft.status()==ExpenseDraftStatus.CONFIRMED){
            return ExpenseView.from(expenses.findByIdAndHousehold(draft.confirmedExpenseId(),scope)
                    .orElseThrow(ExpenseNotFoundException::new));
        }
        if(draft.status()!=ExpenseDraftStatus.OPEN)throw new DraftConflictException("Discarded draft cannot be confirmed");
        if(draft.version()!=version)throw new DraftConflictException("Expense draft version is stale");
        var content=draft.content();requireComplete(content);
        context.requireActive(content.payerMemberId());
        ExpenseCategoryId categoryId=content.categoryId();
        if(categoryId!=null){var category=categories.findByIdAndHousehold(categoryId,scope)
                .orElseThrow(()->new CategoryNotFoundException("Expense category not found"));category.requireActive();}
        var split=materialize(content,context);var now=clock.instant();var expenseId=new ExpenseId(UUID.randomUUID());
        var expense=Expense.confirmedManual(expenseId,scope,context.actorMemberId(),content.payerMemberId(),content.total(),
                content.description(),content.expenseDate(),split,categoryId,now);
        var saved=expenses.save(expense);draft.confirm(saved.id(),now);drafts.save(draft);return ExpenseView.from(saved);
    }
    private void requireComplete(ExpenseDraftContent c){
        if(c.description()==null)throw new ExpenseRuleViolationException("Draft description is required");
        if(c.payerMemberId()==null)throw new ExpenseRuleViolationException("Draft payer is required");
        if(c.total()==null)throw new ExpenseRuleViolationException("Draft amount and currency are required");
        if(c.expenseDate()==null)throw new ExpenseRuleViolationException("Draft expense date is required");
        if(c.split()==null)throw new ExpenseRuleViolationException("Draft split is required");
    }
    private ExpenseSplit materialize(ExpenseDraftContent c,ExpenseHouseholdContext context){
        if(c.split() instanceof EqualDraftSplit equal){equal.members().forEach(context::requireActive);return ExpenseSplit.equal(c.total(),equal.members());}
        if(c.split() instanceof ExactDraftSplit exact){exact.allocations().forEach(a->context.requireActive(a.memberId()));return ExpenseSplit.exact(c.total(),exact.allocations());}
        var percentage=(PercentageDraftSplit)c.split();percentage.allocations().forEach(a->context.requireActive(a.memberId()));return ExpenseSplit.percentage(c.total(),percentage.allocations());
    }
}
