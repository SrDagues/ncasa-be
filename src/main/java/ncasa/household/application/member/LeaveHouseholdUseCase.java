package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.application.port.out.InactiveMemberCleanupPort;
import ncasa.household.domain.*;

public final class LeaveHouseholdUseCase {
    private final HouseholdRepository households; private final Clock clock;private final InactiveMemberCleanupPort cleanup;
    public LeaveHouseholdUseCase(HouseholdRepository households, Clock clock) { this(households,clock,InactiveMemberCleanupPort.NONE); }
    public LeaveHouseholdUseCase(HouseholdRepository households, Clock clock,InactiveMemberCleanupPort cleanup) { this.households = households; this.clock = clock;this.cleanup=cleanup; }
    public void execute(HouseholdId id, AccountId actor) {
        var household = HouseholdLoader.load(households, id);
        var member=household.activeMemberFor(actor).id();
        household.leave(member, clock.instant());
        households.save(household);
        cleanup.memberBecameInactive(id.value(),member.value());
    }
}
