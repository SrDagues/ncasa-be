package ncasa.household.application.invite;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.SentInvitationView;
import ncasa.household.application.port.out.HouseholdInvitationRepository;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdId;

public final class GetHouseholdPendingInvitationsUseCase {
    private final HouseholdRepository households;
    private final HouseholdInvitationRepository invitations;
    private final Clock clock;

    public GetHouseholdPendingInvitationsUseCase(HouseholdRepository households,
            HouseholdInvitationRepository invitations, Clock clock) {
        this.households = households;
        this.invitations = invitations;
        this.clock = clock;
    }

    public List<SentInvitationView> execute(HouseholdId householdId, AccountId actor) {
        var household = HouseholdLoader.load(households, householdId);
        household.authorizeInvitationManagement(household.activeMemberFor(actor).id());
        var result = new ArrayList<SentInvitationView>();
        for (var invitation : invitations.findPendingByHousehold(householdId)) {
            if (invitation.expireIfNeeded(clock.instant())) {
                invitations.save(invitation);
            } else {
                result.add(SentInvitationView.from(invitation));
            }
        }
        return List.copyOf(result);
    }
}
