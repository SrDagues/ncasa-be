package ncasa.household.application.get;

import java.util.List;
import java.util.UUID;
import ncasa.household.application.HouseholdLoader;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;

public final class GetHouseholdNotificationDirectoryUseCase {
    private final HouseholdRepository households;
    public GetHouseholdNotificationDirectoryUseCase(HouseholdRepository households){this.households=households;}
    public Directory execute(UUID householdId){
        var household=HouseholdLoader.load(households,new HouseholdId(householdId));
        return new Directory(household.status()==HouseholdStatus.ACTIVE,household.members().stream()
                .map(m->new Member(m.id().value(),m.accountId().value(),m.isActive(),m.role()==HouseholdRole.ADMIN)).toList());
    }
    public record Directory(boolean active,List<Member> members){public Directory{members=List.copyOf(members);}}
    public record Member(UUID memberId,Long accountId,boolean active,boolean administrator){}
}
