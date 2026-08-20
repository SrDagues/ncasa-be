package ncasa.household.application.port.out;

import java.util.List;
import java.util.Optional;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.domain.InvitationEmail;
import ncasa.household.domain.InvitationId;
import ncasa.household.domain.InvitationTokenHash;

public interface HouseholdInvitationRepository {
    Optional<HouseholdInvitation> findById(InvitationId id);
    Optional<HouseholdInvitation> findByTokenHash(InvitationTokenHash hash);
    Optional<HouseholdInvitation> findPending(HouseholdId householdId, InvitationEmail email);
    List<HouseholdInvitation> findPendingByEmail(InvitationEmail email);
    List<HouseholdInvitation> findPendingByHousehold(HouseholdId householdId);
    HouseholdInvitation save(HouseholdInvitation invitation);
}
