package ncasa.expense.application.draft;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.ExpenseDraftView;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class CreateExpenseDraftUseCase {
    private final ExpenseDraftRepository drafts;private final HouseholdExpenseAccessPort household;private final Clock clock;
    public CreateExpenseDraftUseCase(ExpenseDraftRepository drafts,HouseholdExpenseAccessPort household,Clock clock){this.drafts=drafts;this.household=household;this.clock=clock;}
    public ExpenseDraftView execute(Long actorAccountId,UUID householdId){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        return ExpenseDraftView.from(drafts.save(ExpenseDraft.create(new ExpenseDraftId(UUID.randomUUID()),scope,context.actorMemberId(),clock.instant())));
    }
}
