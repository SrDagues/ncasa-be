package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class LeaveHouseholdUseCase {
    private final HouseholdRepository households; private final Clock clock;
    public LeaveHouseholdUseCase(HouseholdRepository households, Clock clock) { this.households = households; this.clock = clock; }
    public void execute(HouseholdId id, AccountId actor) {
        var household = HouseholdLoader.load(households, id);
        household.leave(household.activeMemberFor(actor).id(), clock.instant());
        households.save(household);
    }
}
