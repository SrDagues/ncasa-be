package ncasa.household.application.get;

import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.HouseholdView;
import ncasa.household.application.HouseholdViewAssembler;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdId;

public final class GetHouseholdUseCase {
    private final HouseholdRepository households;
    private final HouseholdViewAssembler views;
    public GetHouseholdUseCase(HouseholdRepository households, HouseholdViewAssembler views) {
        this.households = households; this.views = views;
    }
    public HouseholdView execute(HouseholdId id, AccountId actor) {
        var household = HouseholdLoader.load(households, id);
        household.activeMemberFor(actor);
        return views.assemble(household);
    }
}
