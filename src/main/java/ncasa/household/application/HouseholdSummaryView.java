package ncasa.household.application;

import java.util.UUID;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdMember;
import ncasa.household.domain.HouseholdRole;
import ncasa.household.domain.HouseholdStatus;

public record HouseholdSummaryView(UUID id, String name, HouseholdStatus status, UUID currentMemberId,
        HouseholdRole currentRole, boolean owner) {
    public static HouseholdSummaryView from(Household household, HouseholdMember currentMember) {
        return new HouseholdSummaryView(household.id().value(), household.name().value(), household.status(),
                currentMember.id().value(), currentMember.role(),
                household.ownerMemberId().equals(currentMember.id()));
    }
}
