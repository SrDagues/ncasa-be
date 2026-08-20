package ncasa.household.application.accept;

import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdInvitationRepository;
import ncasa.household.domain.*;

public final class AcceptInvitationByIdUseCase {
    private final HouseholdInvitationRepository invitations; private final InvitationAcceptanceService acceptance;
    private final HouseholdViewAssembler views;
    public AcceptInvitationByIdUseCase(HouseholdInvitationRepository invitations, InvitationAcceptanceService acceptance,
            HouseholdViewAssembler views) {
        this.invitations = invitations; this.acceptance = acceptance; this.views = views;
    }
    public HouseholdView execute(InvitationId id, AccountId account, String email) {
        var invitation = invitations.findById(id).orElseThrow(InvitationNotFoundException::new);
        var authenticatedEmail = InvitationEmail.of(email);
        if (!invitation.email().equals(authenticatedEmail)) throw new InvitationNotFoundException();
        return views.assemble(acceptance.accept(invitation, account, authenticatedEmail));
    }
}
