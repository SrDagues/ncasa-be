package ncasa.notification.application.port.out;

import java.util.List;import java.util.Set;import java.util.UUID;

public interface HouseholdNotificationDirectoryPort {
    Directory get(UUID householdId);
    Set<UUID> activeHouseholdIds(Long accountId);
    record Directory(boolean active,List<Member> members){public Directory{members=List.copyOf(members);}}
    record Member(UUID memberId,Long accountId,boolean active,boolean administrator){}
}
