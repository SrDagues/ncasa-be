package ncasa.household.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.domain.HouseholdRole;
import ncasa.household.domain.InvitationStatus;

public record InvitationResult(UUID id, UUID householdId, String email, HouseholdRole role,
        InvitationStatus status, Instant expiresAt, boolean deliverySucceeded) {
    public static InvitationResult from(HouseholdInvitation invitation, boolean delivered) {
        return new InvitationResult(invitation.id().value(), invitation.householdId().value(),
                invitation.email().value(), invitation.invitedRole(), invitation.status(),
                invitation.expiry().value(), delivered);
    }
}
