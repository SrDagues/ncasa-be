package ncasa.household.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdStatus;

public record HouseholdView(UUID id, String name, HouseholdStatus status, UUID ownerMemberId, Long createdBy,
        Instant createdAt, List<MemberView> members) {
    public static HouseholdView from(Household household, Map<AccountId, String> emails) {
        return new HouseholdView(household.id().value(), household.name().value(), household.status(),
                household.ownerMemberId().value(), household.createdBy().value(), household.createdAt(),
                household.members().stream().map(member -> MemberView.from(member, household.ownerMemberId(),
                        emails.get(member.accountId()))).toList());
    }
}
