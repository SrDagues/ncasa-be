package ncasa.household.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.domain.HouseholdRole;
import ncasa.household.domain.InvitationStatus;

public record SentInvitationView(UUID id, UUID householdId, String email, HouseholdRole role,
        InvitationStatus status, UUID invitedBy, Instant createdAt, Instant expiresAt) {
    public static SentInvitationView from(HouseholdInvitation invitation) {
        return new SentInvitationView(invitation.id().value(), invitation.householdId().value(),
                invitation.email().value(), invitation.invitedRole(), invitation.status(),
                invitation.invitedBy().value(), invitation.createdAt(), invitation.expiry().value());
    }
}
