package ncasa.household.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.domain.HouseholdRole;

public record InvitationView(UUID id, UUID householdId, String householdName, HouseholdRole role,
        UUID invitedBy, String invitedByEmail, Instant createdAt, Instant expiresAt) {
    public static InvitationView from(HouseholdInvitation invitation, String householdName, String invitedByEmail) {
        return new InvitationView(invitation.id().value(), invitation.householdId().value(), householdName,
                invitation.invitedRole(), invitation.invitedBy().value(), invitedByEmail, invitation.createdAt(),
                invitation.expiry().value());
    }
}
