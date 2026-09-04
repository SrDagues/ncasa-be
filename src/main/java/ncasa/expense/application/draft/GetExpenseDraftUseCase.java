package ncasa.expense.application.draft;

import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class GetExpenseDraftUseCase {
    private final ExpenseDraftRepository drafts;private final HouseholdExpenseAccessPort household;
    public GetExpenseDraftUseCase(ExpenseDraftRepository drafts,HouseholdExpenseAccessPort household){this.drafts=drafts;this.household=household;}
    public ExpenseDraftView execute(Long actorAccountId,UUID householdId,UUID draftId){return ExpenseDraftView.from(loadOwned(actorAccountId,householdId,draftId));}
    ExpenseDraft loadOwned(Long actorAccountId,UUID householdId,UUID draftId){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        var draft=drafts.findByIdAndHousehold(new ExpenseDraftId(draftId),scope).orElseThrow(()->new DraftNotFoundException("Expense draft not found"));
        if(!draft.createdByMemberId().equals(context.actorMemberId()))throw new ExpenseAccessDeniedException("Expense drafts are private");
        return draft;
    }
}
