package ncasa.expense.application.draft;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class DiscardExpenseDraftUseCase {
    private final ExpenseDraftRepository drafts;private final HouseholdExpenseAccessPort household;private final Clock clock;
    public DiscardExpenseDraftUseCase(ExpenseDraftRepository drafts,HouseholdExpenseAccessPort household,Clock clock){this.drafts=drafts;this.household=household;this.clock=clock;}
    public ExpenseDraftView execute(Long actorAccountId,UUID householdId,UUID draftId,long version){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        var draft=drafts.findByIdAndHousehold(new ExpenseDraftId(draftId),scope).orElseThrow(()->new DraftNotFoundException("Expense draft not found"));
        if(!draft.createdByMemberId().equals(context.actorMemberId()))throw new ExpenseAccessDeniedException("Expense drafts are private");
        if(draft.version()!=version)throw new DraftConflictException("Expense draft version is stale");
        draft.discard(clock.instant());return ExpenseDraftView.from(drafts.save(draft));
    }
}
