package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.HouseholdView;
import ncasa.household.application.HouseholdViewAssembler;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class ChangeMemberRoleUseCase {
    private final HouseholdRepository households; private final Clock clock; private final HouseholdViewAssembler views;
    public ChangeMemberRoleUseCase(HouseholdRepository households, Clock clock, HouseholdViewAssembler views) {
        this.households = households; this.clock = clock; this.views = views;
    }
    public HouseholdView execute(HouseholdId id, AccountId actor, MemberId target, HouseholdRole role) {
        var household = HouseholdLoader.load(households, id);
        household.changeRole(household.activeMemberFor(actor).id(), target, role, clock.instant());
        return views.assemble(households.save(household));
    }
}
