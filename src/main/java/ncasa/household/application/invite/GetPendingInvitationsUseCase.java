package ncasa.household.application.invite;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ncasa.household.application.*;
import ncasa.household.application.port.out.*;
import ncasa.household.domain.*;

public final class GetPendingInvitationsUseCase {
    private final HouseholdRepository households; private final HouseholdInvitationRepository invitations;
    private final AccountDirectoryPort accounts; private final Clock clock;
    public GetPendingInvitationsUseCase(HouseholdRepository households, HouseholdInvitationRepository invitations,
            AccountDirectoryPort accounts, Clock clock) {
        this.households = households; this.invitations = invitations; this.accounts = accounts; this.clock = clock;
    }
    public List<InvitationView> execute(String authenticatedEmail) {
        var result = new ArrayList<InvitationView>();
        for (var invitation : invitations.findPendingByEmail(InvitationEmail.of(authenticatedEmail))) {
            if (invitation.expireIfNeeded(clock.instant())) { invitations.save(invitation); continue; }
            var household = households.findById(invitation.householdId()).orElse(null);
            if (household != null && household.status() == HouseholdStatus.ACTIVE) {
                var inviter = household.member(invitation.invitedBy());
                String email = accounts.findEmails(Set.of(inviter.accountId())).get(inviter.accountId());
                result.add(InvitationView.from(invitation, household.name().value(), email));
            }
        }
        return List.copyOf(result);
    }
}
