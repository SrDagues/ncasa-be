package ncasa.household.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Household {
    private final HouseholdId id;
    private HouseholdName name;
    private HouseholdStatus status;
    private MemberId ownerMemberId;
    private final Map<MemberId, HouseholdMember> members;
    private final AccountId createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    private Household(HouseholdId id, HouseholdName name, HouseholdStatus status, MemberId ownerMemberId,
            Collection<HouseholdMember> members, AccountId createdBy, Instant createdAt, Instant updatedAt,
            long version) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.ownerMemberId = Objects.requireNonNull(ownerMemberId);
        this.members = new LinkedHashMap<>();
        members.forEach(member -> this.members.put(member.id(), member));
        this.createdBy = Objects.requireNonNull(createdBy);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        this.version = version;
        validateOwner();
    }

    public static Household create(HouseholdId id, HouseholdName name, MemberId ownerMemberId,
            AccountId creator, Instant now) {
        HouseholdMember owner = HouseholdMember.join(ownerMemberId, creator, HouseholdRole.ADMIN, now);
        return new Household(id, name, HouseholdStatus.ACTIVE, ownerMemberId, java.util.List.of(owner), creator, now, now, 0);
    }

    public static Household rehydrate(HouseholdId id, HouseholdName name, HouseholdStatus status,
            MemberId ownerMemberId, Collection<HouseholdMember> members, AccountId createdBy,
            Instant createdAt, Instant updatedAt) {
        return new Household(id, name, status, ownerMemberId, members, createdBy, createdAt, updatedAt, 0);
    }

    public static Household rehydrate(HouseholdId id, HouseholdName name, HouseholdStatus status,
            MemberId ownerMemberId, Collection<HouseholdMember> members, AccountId createdBy,
            Instant createdAt, Instant updatedAt, long version) {
        return new Household(id, name, status, ownerMemberId, members, createdBy, createdAt, updatedAt, version);
    }

    public void rename(MemberId actor, HouseholdName newName, Instant now) {
        ensureActive();
        requireAdmin(actor);
        this.name = Objects.requireNonNull(newName);
        touch(now);
    }

    public HouseholdMember addOrReactivateMember(MemberId newMemberId, AccountId accountId,
            HouseholdRole role, Instant now) {
        ensureActive();
        HouseholdMember existing = members.values().stream()
                .filter(member -> member.accountId().equals(accountId)).findFirst().orElse(null);
        if (existing != null) {
            existing.reactivate(role, now);
            touch(now);
            return existing;
        }
        HouseholdMember member = HouseholdMember.join(newMemberId, accountId, role, now);
        members.put(member.id(), member);
        touch(now);
        return member;
    }

    public void leave(MemberId memberId, Instant now) {
        ensureActive();
        HouseholdMember member = requireActiveMember(memberId);
        if (member.id().equals(ownerMemberId)) throw new HouseholdRuleViolationException("Owner must transfer ownership first");
        member.leave(now);
        touch(now);
    }

    public void removeMember(MemberId actorId, MemberId targetId, Instant now) {
        ensureActive();
        HouseholdMember actor = requireAdmin(actorId);
        HouseholdMember target = requireActiveMember(targetId);
        if (actorId.equals(targetId)) throw new HouseholdAccessDeniedException("Use leave to leave a household");
        if (targetId.equals(ownerMemberId)) throw new HouseholdRuleViolationException("Owner cannot be removed");
        if (target.role() == HouseholdRole.ADMIN && !actorId.equals(ownerMemberId)) {
            throw new HouseholdAccessDeniedException("Only owner can manage administrators");
        }
        target.remove(now);
        touch(now);
    }

    public void changeRole(MemberId actorId, MemberId targetId, HouseholdRole newRole, Instant now) {
        ensureActive();
        requireOwner(actorId);
        HouseholdMember target = requireActiveMember(targetId);
        if (targetId.equals(ownerMemberId) && newRole != HouseholdRole.ADMIN) {
            throw new HouseholdRuleViolationException("Owner must remain administrator");
        }
        target.changeRole(newRole, now);
        touch(now);
    }

    public void transferOwnership(MemberId actorId, MemberId targetId, Instant now) {
        ensureActive();
        requireOwner(actorId);
        HouseholdMember target = requireActiveMember(targetId);
        target.changeRole(HouseholdRole.ADMIN, now);
        ownerMemberId = targetId;
        touch(now);
    }

    public void authorizeInvitation(MemberId actorId, HouseholdRole invitedRole) {
        ensureActive();
        requireAdmin(actorId);
        if (invitedRole == HouseholdRole.ADMIN && !actorId.equals(ownerMemberId)) {
            throw new HouseholdAccessDeniedException("Only owner can invite administrators");
        }
    }

    public void authorizeInvitationManagement(MemberId actorId) {
        ensureActive();
        requireAdmin(actorId);
    }

    public void archive(MemberId actorId, Instant now) {
        ensureActive();
        requireOwner(actorId);
        if (members.values().stream().filter(HouseholdMember::isActive).count() != 1) {
            throw new HouseholdRuleViolationException("Household can only be archived by its sole active member");
        }
        status = HouseholdStatus.ARCHIVED;
        touch(now);
    }

    public HouseholdMember member(MemberId id) {
        HouseholdMember member = members.get(id);
        if (member == null) throw new HouseholdRuleViolationException("Member not found");
        return member;
    }

    public HouseholdMember activeMemberFor(AccountId accountId) {
        return members.values().stream().filter(HouseholdMember::isActive)
                .filter(member -> member.accountId().equals(accountId)).findFirst()
                .orElseThrow(() -> new HouseholdAccessDeniedException("Account is not an active household member"));
    }

    private HouseholdMember requireAdmin(MemberId id) {
        HouseholdMember member = requireActiveMember(id);
        if (member.role() != HouseholdRole.ADMIN) throw new HouseholdAccessDeniedException("Administrator role required");
        return member;
    }

    private void requireOwner(MemberId id) {
        requireActiveMember(id);
        if (!ownerMemberId.equals(id)) throw new HouseholdAccessDeniedException("Owner role required");
    }

    private HouseholdMember requireActiveMember(MemberId id) {
        HouseholdMember member = member(id);
        if (!member.isActive()) throw new HouseholdRuleViolationException("Membership is not active");
        return member;
    }

    private void validateOwner() {
        HouseholdMember owner = members.get(ownerMemberId);
        if (owner == null || !owner.isActive() || owner.role() != HouseholdRole.ADMIN) {
            throw new HouseholdRuleViolationException("Active household must have an active administrator owner");
        }
    }

    private void ensureActive() {
        if (status != HouseholdStatus.ACTIVE) throw new HouseholdRuleViolationException("Household is archived");
    }

    private void touch(Instant now) { updatedAt = Objects.requireNonNull(now); }

    public HouseholdId id() { return id; }
    public HouseholdName name() { return name; }
    public HouseholdStatus status() { return status; }
    public MemberId ownerMemberId() { return ownerMemberId; }
    public Collection<HouseholdMember> members() { return java.util.Collections.unmodifiableList(new ArrayList<>(members.values())); }
    public AccountId createdBy() { return createdBy; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }
}
