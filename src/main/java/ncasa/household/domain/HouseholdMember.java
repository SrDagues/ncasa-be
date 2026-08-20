package ncasa.household.domain;

import java.time.Instant;
import java.util.Objects;

public final class HouseholdMember {
    private final MemberId id;
    private final AccountId accountId;
    private HouseholdRole role;
    private MembershipStatus status;
    private final Instant joinedAt;
    private Instant statusChangedAt;

    private HouseholdMember(MemberId id, AccountId accountId, HouseholdRole role, MembershipStatus status,
            Instant joinedAt, Instant statusChangedAt) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.joinedAt = Objects.requireNonNull(joinedAt);
        this.statusChangedAt = Objects.requireNonNull(statusChangedAt);
    }

    public static HouseholdMember join(MemberId id, AccountId accountId, HouseholdRole role, Instant now) {
        return new HouseholdMember(id, accountId, role, MembershipStatus.ACTIVE, now, now);
    }

    public static HouseholdMember rehydrate(MemberId id, AccountId accountId, HouseholdRole role,
            MembershipStatus status, Instant joinedAt, Instant statusChangedAt) {
        return new HouseholdMember(id, accountId, role, status, joinedAt, statusChangedAt);
    }

    void leave(Instant now) { changeStatus(MembershipStatus.LEFT, now); }
    void remove(Instant now) { changeStatus(MembershipStatus.REMOVED, now); }

    void reactivate(HouseholdRole role, Instant now) {
        if (status == MembershipStatus.ACTIVE) throw new HouseholdRuleViolationException("Account is already a member");
        this.role = Objects.requireNonNull(role);
        changeStatus(MembershipStatus.ACTIVE, now);
    }

    void changeRole(HouseholdRole role, Instant now) {
        this.role = Objects.requireNonNull(role);
        this.statusChangedAt = Objects.requireNonNull(now);
    }

    private void changeStatus(MembershipStatus status, Instant now) {
        this.status = status;
        this.statusChangedAt = Objects.requireNonNull(now);
    }

    public boolean isActive() { return status == MembershipStatus.ACTIVE; }
    public MemberId id() { return id; }
    public AccountId accountId() { return accountId; }
    public HouseholdRole role() { return role; }
    public MembershipStatus status() { return status; }
    public Instant joinedAt() { return joinedAt; }
    public Instant statusChangedAt() { return statusChangedAt; }
}
