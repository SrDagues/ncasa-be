package ncasa.household.application.rename;

import java.time.Clock;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.HouseholdView;
import ncasa.household.application.HouseholdViewAssembler;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdName;

public final class RenameHouseholdUseCase {
    private final HouseholdRepository households; private final Clock clock; private final HouseholdViewAssembler views;
    public RenameHouseholdUseCase(HouseholdRepository households, Clock clock, HouseholdViewAssembler views) {
        this.households = households; this.clock = clock; this.views = views;
    }
    public HouseholdView execute(HouseholdId id, AccountId actor, String name) {
        var household = HouseholdLoader.load(households, id);
        household.rename(household.activeMemberFor(actor).id(), HouseholdName.of(name), clock.instant());
        return views.assemble(households.save(household));
    }
}
