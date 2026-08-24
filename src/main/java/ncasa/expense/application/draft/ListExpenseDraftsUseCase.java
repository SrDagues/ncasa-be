package ncasa.expense.application.draft;

import java.util.List;
import java.util.UUID;
import ncasa.expense.application.ExpenseDraftView;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class ListExpenseDraftsUseCase {
    private final ExpenseDraftRepository drafts;private final HouseholdExpenseAccessPort household;
    public ListExpenseDraftsUseCase(ExpenseDraftRepository drafts,HouseholdExpenseAccessPort household){this.drafts=drafts;this.household=household;}
    public List<ExpenseDraftView> execute(Long actorAccountId,UUID householdId,ExpenseDraftStatus status){
        var scope=new HouseholdRef(householdId);var context=household.getContext(scope,actorAccountId);
        return drafts.findByCreator(scope,context.actorMemberId(),status).stream().map(ExpenseDraftView::from).toList();
    }
}
