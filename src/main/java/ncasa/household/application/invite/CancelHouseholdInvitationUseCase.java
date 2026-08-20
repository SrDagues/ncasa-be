package ncasa.household.application.invite;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.*;
import ncasa.household.domain.*;

public final class CancelHouseholdInvitationUseCase {
    private final HouseholdRepository households; private final HouseholdInvitationRepository invitations; private final Clock clock;
    public CancelHouseholdInvitationUseCase(HouseholdRepository households, HouseholdInvitationRepository invitations, Clock clock) {
        this.households = households; this.invitations = invitations; this.clock = clock;
    }
    public void execute(HouseholdId householdId, InvitationId invitationId, AccountId actor) {
        var household = HouseholdLoader.load(households, householdId);
        var invitation = invitations.findById(invitationId).orElseThrow(InvitationNotFoundException::new);
        if (!invitation.householdId().equals(householdId)) throw new InvitationNotFoundException();
        household.authorizeInvitation(household.activeMemberFor(actor).id(), invitation.invitedRole());
        invitation.cancel(clock.instant());
        invitations.save(invitation);
    }
}
