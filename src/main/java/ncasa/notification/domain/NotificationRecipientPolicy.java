package ncasa.notification.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class NotificationRecipientPolicy {
    public Set<AccountRef> recipients(NotificationKind kind, PlanAudience audience, List<Candidate> directory) {
        Set<UUID> targetMembers = new LinkedHashSet<>();
        if (kind == NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED) {
            targetMembers.add(audience.createdByMemberId());
            directory.stream().filter(Candidate::administrator).map(Candidate::memberId).forEach(targetMembers::add);
        } else {
            targetMembers.add(audience.payerMemberId());
            targetMembers.addAll(audience.participantMemberIds());
        }
        Set<AccountRef> result = new LinkedHashSet<>();
        directory.stream().filter(Candidate::active).filter(c -> targetMembers.contains(c.memberId()))
                .map(Candidate::accountId).map(AccountRef::new).forEach(result::add);
        return Set.copyOf(result);
    }
    public record PlanAudience(UUID createdByMemberId, UUID payerMemberId, Set<UUID> participantMemberIds) {
        public PlanAudience { participantMemberIds=Set.copyOf(participantMemberIds); }
    }
    public record Candidate(UUID memberId, Long accountId, boolean active, boolean administrator) {}
}
