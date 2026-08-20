package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class RemoveMemberUseCase {
    private final HouseholdRepository households; private final Clock clock;
    public RemoveMemberUseCase(HouseholdRepository households, Clock clock) { this.households = households; this.clock = clock; }
    public void execute(HouseholdId id, AccountId actor, MemberId target) {
        var household = HouseholdLoader.load(households, id);
        household.removeMember(household.activeMemberFor(actor).id(), target, clock.instant());
        households.save(household);
    }
}
