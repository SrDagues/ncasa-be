package ncasa.household.application.archive;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class ArchiveHouseholdUseCase {
    private final HouseholdRepository households; private final Clock clock;
    public ArchiveHouseholdUseCase(HouseholdRepository households, Clock clock) { this.households = households; this.clock = clock; }
    public void execute(HouseholdId id, AccountId actor) {
        var household = HouseholdLoader.load(households, id);
        household.archive(household.activeMemberFor(actor).id(), clock.instant());
        households.save(household);
    }
}
