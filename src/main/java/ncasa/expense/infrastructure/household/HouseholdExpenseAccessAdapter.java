package ncasa.expense.infrastructure.household;

import java.util.stream.Collectors;
import ncasa.expense.application.ExpenseHouseholdContext;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.HouseholdRef;
import ncasa.expense.domain.MemberRef;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdRole;

public final class HouseholdExpenseAccessAdapter implements HouseholdExpenseAccessPort {
    private final GetHouseholdMembershipContextUseCase memberships;

    public HouseholdExpenseAccessAdapter(GetHouseholdMembershipContextUseCase memberships) {
        this.memberships = memberships;
    }

    @Override
    public ExpenseHouseholdContext getContext(HouseholdRef householdId, Long actorAccountId) {
        var context = memberships.execute(new HouseholdId(householdId.value()), new AccountId(actorAccountId));
        var members = context.activeMemberIds().stream().map(MemberRef::new).collect(Collectors.toUnmodifiableSet());
        var allMembers = context.allMemberIds().stream().map(MemberRef::new).collect(Collectors.toUnmodifiableSet());
        return new ExpenseHouseholdContext(householdId, new MemberRef(context.actorMemberId()),
                context.actorRole() == HouseholdRole.ADMIN, members, allMembers);
    }
}
