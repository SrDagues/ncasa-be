package ncasa.expense.application.port.out;
import java.util.Set;import ncasa.expense.domain.*;
public interface ExpensePlanHouseholdStatePort {
    State get(HouseholdRef householdId);
    record State(Set<MemberRef> activeMembers,Set<ExpenseCategoryId> activeCategories){public State{activeMembers=Set.copyOf(activeMembers);activeCategories=Set.copyOf(activeCategories);}}
}
