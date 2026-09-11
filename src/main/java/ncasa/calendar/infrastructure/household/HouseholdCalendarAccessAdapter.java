package ncasa.calendar.infrastructure.household;

import ncasa.calendar.application.CalendarHouseholdContext;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.HouseholdId;
import java.util.UUID;

public final class HouseholdCalendarAccessAdapter implements CalendarHouseholdAccessPort {
    private final GetHouseholdMembershipContextUseCase memberships;
    public HouseholdCalendarAccessAdapter(GetHouseholdMembershipContextUseCase memberships){this.memberships=memberships;}
    @Override public CalendarHouseholdContext getContext(UUID householdId,Long actor){
        var context=memberships.execute(new HouseholdId(householdId),new AccountId(actor));
        return new CalendarHouseholdContext(context.actorMemberId(),context.activeMemberIds());
    }
}
