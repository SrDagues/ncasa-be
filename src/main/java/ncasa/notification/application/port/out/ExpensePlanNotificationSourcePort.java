package ncasa.notification.application.port.out;

import java.util.Set;import java.util.UUID;

public interface ExpensePlanNotificationSourcePort {
    Context get(UUID householdId,UUID planId);
    record Context(UUID planId,UUID householdId,UUID createdByMemberId,UUID payerMemberId,
            Set<UUID> participantMemberIds,String subject,String attentionReason){
        public Context{participantMemberIds=Set.copyOf(participantMemberIds);}
    }
}
