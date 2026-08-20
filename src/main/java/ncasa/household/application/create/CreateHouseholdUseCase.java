package ncasa.household.application.create;

import java.time.Clock;
import java.util.UUID;
import ncasa.household.application.HouseholdView;
import ncasa.household.application.HouseholdViewAssembler;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdName;
import ncasa.household.domain.MemberId;

public final class CreateHouseholdUseCase {
    private final HouseholdRepository households;
    private final Clock clock;
    private final HouseholdViewAssembler views;
    public CreateHouseholdUseCase(HouseholdRepository households, Clock clock, HouseholdViewAssembler views) {
        this.households = households; this.clock = clock; this.views = views;
    }
    public HouseholdView execute(AccountId actor, String rawName) {
        var household = Household.create(new HouseholdId(UUID.randomUUID()), HouseholdName.of(rawName),
                new MemberId(UUID.randomUUID()), actor, clock.instant());
        return views.assemble(households.save(household));
    }
}
