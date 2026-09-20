package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.application.port.out.InactiveMemberCleanupPort;
import ncasa.household.domain.*;

public final class RemoveMemberUseCase {
    private final HouseholdRepository households; private final Clock clock;private final InactiveMemberCleanupPort cleanup;
    public RemoveMemberUseCase(HouseholdRepository households, Clock clock) { this(households,clock,InactiveMemberCleanupPort.NONE); }
    public RemoveMemberUseCase(HouseholdRepository households, Clock clock,InactiveMemberCleanupPort cleanup) { this.households = households; this.clock = clock;this.cleanup=cleanup; }
    public void execute(HouseholdId id, AccountId actor, MemberId target) {
        var household = HouseholdLoader.load(households, id);
        household.removeMember(household.activeMemberFor(actor).id(), target, clock.instant());
        households.save(household);
        cleanup.memberBecameInactive(id.value(),target.value());
    }
}
