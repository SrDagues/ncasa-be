package ncasa.expense.application;

import java.util.Set;
import ncasa.expense.domain.HouseholdRef;
import ncasa.expense.domain.MemberRef;

public record ExpenseHouseholdContext(HouseholdRef householdId, MemberRef actorMemberId,
        boolean administrator, Set<MemberRef> activeMemberIds) {
    public ExpenseHouseholdContext {
        activeMemberIds = Set.copyOf(activeMemberIds);
        if (!activeMemberIds.contains(actorMemberId)) {
            throw new IllegalArgumentException("Actor must be an active household member");
        }
    }

    public void requireActive(MemberRef memberId) {
        if (!activeMemberIds.contains(memberId)) {
            throw new ExpenseAccessDeniedException("Expense member is not active in the household");
        }
    }
}
