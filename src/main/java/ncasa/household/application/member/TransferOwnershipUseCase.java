package ncasa.household.application.member;

import java.time.Clock;
import ncasa.household.application.*;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class TransferOwnershipUseCase {
    private final HouseholdRepository households; private final Clock clock; private final HouseholdViewAssembler views;
    public TransferOwnershipUseCase(HouseholdRepository households, Clock clock, HouseholdViewAssembler views) {
        this.households = households; this.clock = clock; this.views = views;
    }
    public HouseholdView execute(HouseholdId id, AccountId actor, MemberId target) {
        var household = HouseholdLoader.load(households, id);
        household.transferOwnership(household.activeMemberFor(actor).id(), target, clock.instant());
        return views.assemble(households.save(household));
    }
}
