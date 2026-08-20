package ncasa.household.application.get;

import java.util.Set;
import java.util.UUID;
import ncasa.household.domain.HouseholdRole;

public record HouseholdMembershipContext(UUID householdId, UUID actorMemberId, HouseholdRole actorRole,
        Set<UUID> activeMemberIds) {
    public HouseholdMembershipContext {
        activeMemberIds = Set.copyOf(activeMemberIds);
    }
}
