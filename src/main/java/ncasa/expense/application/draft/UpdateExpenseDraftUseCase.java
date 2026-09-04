package ncasa.expense.application.draft;

import java.time.Clock;
import ncasa.expense.application.*;
import ncasa.expense.application.create.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class UpdateExpenseDraftUseCase {
    private final ExpenseDraftRepository drafts;private final HouseholdExpenseAccessPort household;private final ExpenseCategoryRepository categories;private final Clock clock;
    public UpdateExpenseDraftUseCase(ExpenseDraftRepository drafts,HouseholdExpenseAccessPort household,ExpenseCategoryRepository categories,Clock clock){this.drafts=drafts;this.household=household;this.categories=categories;this.clock=clock;}
    public ExpenseDraftView execute(UpdateExpenseDraftCommand command){
        var scope=new HouseholdRef(command.householdId());var context=household.getContext(scope,command.actorAccountId());
        var draft=drafts.findByIdAndHousehold(new ExpenseDraftId(command.draftId()),scope).orElseThrow(()->new DraftNotFoundException("Expense draft not found"));
        if(!draft.createdByMemberId().equals(context.actorMemberId()))throw new ExpenseAccessDeniedException("Expense drafts are private");
        if(draft.version()!=command.version())throw new DraftConflictException("Expense draft version is stale");
        if((command.amount()==null)!=(command.currency()==null))throw new ExpenseRuleViolationException("Draft amount and currency must be provided together");
        var total=command.amount()==null?null:new Money(command.amount(),command.currency());
        var category=command.categoryId()==null?null:categories.findByIdAndHousehold(new ExpenseCategoryId(command.categoryId()),scope)
                .orElseThrow(()->new CategoryNotFoundException("Expense category not found"));
        if(category!=null)category.requireActive();
        if(command.payerMemberId()!=null)context.requireActive(new MemberRef(command.payerMemberId()));
        DraftSplit split=toDraftSplit(command.split(),total,context);
        draft.update(new ExpenseDraftContent(command.description()==null||command.description().isBlank()?null:new ExpenseDescription(command.description()),
                command.payerMemberId()==null?null:new MemberRef(command.payerMemberId()),total,command.expenseDate(),
                category==null?null:category.id(),split),clock.instant());
        return ExpenseDraftView.from(drafts.save(draft));
    }
    private DraftSplit toDraftSplit(ExpenseSplitCommand command,Money total,ExpenseHouseholdContext context){
        if(command==null)return null;
        if(command instanceof EqualSplitCommand equal){var members=equal.memberIds().stream().map(MemberRef::new).toList();members.forEach(context::requireActive);return new EqualDraftSplit(members);}
        if(command instanceof ExactSplitCommand exact){if(total==null)throw new ExpenseRuleViolationException("Draft total is required for exact allocations");return new ExactDraftSplit(exact.allocations().stream().map(a->{var member=new MemberRef(a.memberId());context.requireActive(member);return new ExpenseAllocation(member,new Money(a.amount(),total.currency()));}).toList());}
        var percentage=(PercentageSplitCommand)command;return new PercentageDraftSplit(percentage.allocations().stream().map(a->{var member=new MemberRef(a.memberId());context.requireActive(member);return new PercentageAllocation(member,Percentage.of(a.percentage()));}).toList());
    }
}
