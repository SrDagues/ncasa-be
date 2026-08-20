package ncasa.household.application.invite;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.InvitationResult;
import ncasa.household.application.port.out.*;
import ncasa.household.domain.*;

public final class InviteHouseholdMemberUseCase {
    private final HouseholdRepository households;
    private final HouseholdInvitationRepository invitations;
    private final InvitationTokenGenerator tokens;
    private final InvitationTokenHasher hasher;
    private final InvitationDeliveryPort delivery;
    private final Clock clock;
    private final Duration duration;

    public InviteHouseholdMemberUseCase(HouseholdRepository households, HouseholdInvitationRepository invitations,
            InvitationTokenGenerator tokens, InvitationTokenHasher hasher, InvitationDeliveryPort delivery,
            Clock clock, Duration duration) {
        this.households = households; this.invitations = invitations; this.tokens = tokens; this.hasher = hasher;
        this.delivery = delivery; this.clock = clock; this.duration = duration;
    }

    public InvitationResult execute(HouseholdId householdId, AccountId actor, String rawEmail, HouseholdRole role) {
        var household = HouseholdLoader.load(households, householdId);
        var actorMember = household.activeMemberFor(actor);
        household.authorizeInvitation(actorMember.id(), role);
        var email = InvitationEmail.of(rawEmail);
        var now = clock.instant();
        invitations.findPending(householdId, email).ifPresent(existing -> {
            existing.cancel(now);
            invitations.save(existing);
        });
        String rawToken = tokens.generate();
        var invitation = HouseholdInvitation.create(new InvitationId(UUID.randomUUID()), householdId, email, role,
                actorMember.id(), hasher.hash(rawToken), now, InvitationExpiry.after(now, duration));
        invitations.save(invitation);
        boolean delivered = true;
        try { delivery.deliver(invitation, rawToken); } catch (RuntimeException failure) { delivered = false; }
        return InvitationResult.from(invitation, delivered);
    }
}
