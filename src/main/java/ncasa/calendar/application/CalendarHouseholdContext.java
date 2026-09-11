package ncasa.calendar.application;

import java.util.Set;
import java.util.UUID;

public record CalendarHouseholdContext(UUID actorMemberId, Set<UUID> activeMemberIds) {
    public CalendarHouseholdContext { activeMemberIds=Set.copyOf(activeMemberIds); }
    public void requireMembers(Set<UUID> members) {
        if (!activeMemberIds.containsAll(members)) throw new CalendarAccessDeniedException("Calendar member is not active in household");
    }
    public void requireMember(UUID member) {
        if (member != null && !activeMemberIds.contains(member)) throw new CalendarAccessDeniedException("Calendar member is not active in household");
    }
}
