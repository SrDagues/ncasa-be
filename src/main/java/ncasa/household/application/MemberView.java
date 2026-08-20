package ncasa.household.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.household.domain.HouseholdMember;
import ncasa.household.domain.HouseholdRole;
import ncasa.household.domain.MembershipStatus;
import ncasa.household.domain.MemberId;

public record MemberView(UUID id, Long accountId, String email, HouseholdRole role, MembershipStatus status, boolean owner,
        Instant joinedAt, Instant statusChangedAt) {
    static MemberView from(HouseholdMember member, MemberId ownerId, String email) {
        return new MemberView(member.id().value(), member.accountId().value(), email, member.role(), member.status(),
                member.id().equals(ownerId), member.joinedAt(), member.statusChangedAt());
    }
}
