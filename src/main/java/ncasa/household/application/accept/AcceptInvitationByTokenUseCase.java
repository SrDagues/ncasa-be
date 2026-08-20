package ncasa.household.application.accept;

import ncasa.household.application.*;
import ncasa.household.application.port.out.*;
import ncasa.household.domain.*;

public final class AcceptInvitationByTokenUseCase {
    private final HouseholdInvitationRepository invitations; private final InvitationTokenHasher hasher;
    private final InvitationAcceptanceService acceptance;
    private final HouseholdViewAssembler views;
    public AcceptInvitationByTokenUseCase(HouseholdInvitationRepository invitations, InvitationTokenHasher hasher,
            InvitationAcceptanceService acceptance, HouseholdViewAssembler views) {
        this.invitations = invitations; this.hasher = hasher; this.acceptance = acceptance; this.views = views;
    }
    public HouseholdView execute(String rawToken, AccountId account, String email) {
        var invitation = invitations.findByTokenHash(hasher.hash(rawToken)).orElseThrow(InvitationNotFoundException::new);
        var authenticatedEmail = InvitationEmail.of(email);
        if (!invitation.email().equals(authenticatedEmail)) throw new InvitationNotFoundException();
        return views.assemble(acceptance.accept(invitation, account, authenticatedEmail));
    }
}
