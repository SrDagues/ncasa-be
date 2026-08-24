package ncasa.expense.application.port.out;

import java.util.List;
import java.util.Optional;
import ncasa.expense.domain.*;

public interface ExpenseDraftRepository {
    ExpenseDraft save(ExpenseDraft draft);
    Optional<ExpenseDraft> findByIdAndHousehold(ExpenseDraftId id,HouseholdRef householdId);
    List<ExpenseDraft> findByCreator(HouseholdRef householdId,MemberRef creator,ExpenseDraftStatus status);
}
