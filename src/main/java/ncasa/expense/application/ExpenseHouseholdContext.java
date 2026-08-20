package ncasa.expense.application;

import java.util.Set;
import ncasa.expense.domain.HouseholdRef;
import ncasa.expense.domain.MemberRef;

public record ExpenseHouseholdContext(HouseholdRef householdId, MemberRef actorMemberId,
        boolean administrator, Set<MemberRef> activeMemberIds, Set<MemberRef> allMemberIds) {
    public ExpenseHouseholdContext(HouseholdRef householdId, MemberRef actorMemberId,
            boolean administrator, Set<MemberRef> activeMemberIds) {
        this(householdId, actorMemberId, administrator, activeMemberIds, activeMemberIds);
    }
    public ExpenseHouseholdContext {
        activeMemberIds = Set.copyOf(activeMemberIds);
        allMemberIds = Set.copyOf(allMemberIds);
        if (!activeMemberIds.contains(actorMemberId)) {
            throw new IllegalArgumentException("Actor must be an active household member");
        }
    }

    public void requireActive(MemberRef memberId) {
        if (!activeMemberIds.contains(memberId)) {
            throw new ExpenseAccessDeniedException("Expense member is not active in the household");
        }
    }


    public void requireMember(MemberRef memberId) {
        if (!allMemberIds.contains(memberId)) {
            throw new ExpenseAccessDeniedException("Member does not belong to the household");
        }
    }
}
