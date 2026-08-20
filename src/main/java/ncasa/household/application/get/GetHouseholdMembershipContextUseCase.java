package ncasa.household.application.get;

import java.util.stream.Collectors;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdAccessDeniedException;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdStatus;
import ncasa.household.domain.HouseholdMember;

public final class GetHouseholdMembershipContextUseCase {
    private final HouseholdRepository households;

    public GetHouseholdMembershipContextUseCase(HouseholdRepository households) {
        this.households = households;
    }

    public HouseholdMembershipContext execute(HouseholdId id, AccountId actor) {
        var household = HouseholdLoader.load(households, id);
        if (household.status() != HouseholdStatus.ACTIVE) {
            throw new HouseholdAccessDeniedException("Household is archived");
        }
        var actorMember = household.activeMemberFor(actor);
        var activeMemberIds = household.members().stream()
                .filter(HouseholdMember::isActive)
                .map(member -> member.id().value())
                .collect(Collectors.toUnmodifiableSet());
        return new HouseholdMembershipContext(household.id().value(), actorMember.id().value(),
                actorMember.role(), activeMemberIds);
    }
}
