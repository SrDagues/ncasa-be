package ncasa.household.application.accept;

import java.time.Clock;
import java.util.UUID;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.port.out.*;
import ncasa.household.domain.*;

public final class InvitationAcceptanceService {
    private final HouseholdRepository households; private final HouseholdInvitationRepository invitations; private final Clock clock;
    public InvitationAcceptanceService(HouseholdRepository households, HouseholdInvitationRepository invitations, Clock clock) {
        this.households = households; this.invitations = invitations; this.clock = clock;
    }
    public Household accept(HouseholdInvitation invitation, AccountId accountId, InvitationEmail email) {
        var now = clock.instant();
        invitation.accept(email, now);
        var household = HouseholdLoader.load(households, invitation.householdId());
        household.addOrReactivateMember(new MemberId(UUID.randomUUID()), accountId, invitation.invitedRole(), now);
        households.save(household);
        invitations.save(invitation);
        return household;
    }
}
