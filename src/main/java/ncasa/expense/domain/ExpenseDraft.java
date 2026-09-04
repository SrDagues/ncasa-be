package ncasa.expense.domain;

import java.time.Instant;
import java.util.Objects;

public final class ExpenseDraft {
    private final ExpenseDraftId id;
    private final HouseholdRef householdId;
    private final MemberRef createdByMemberId;
    private ExpenseDraftContent content;
    private ExpenseDraftStatus status;
    private ExpenseId confirmedExpenseId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant confirmedAt;
    private Instant discardedAt;
    private final long version;

    private ExpenseDraft(ExpenseDraftId id, HouseholdRef householdId, MemberRef createdByMemberId,
            ExpenseDraftContent content, ExpenseDraftStatus status, ExpenseId confirmedExpenseId,
            Instant createdAt, Instant updatedAt, Instant confirmedAt, Instant discardedAt, long version) {
        this.id = Objects.requireNonNull(id); this.householdId = Objects.requireNonNull(householdId);
        this.createdByMemberId = Objects.requireNonNull(createdByMemberId); this.content = Objects.requireNonNull(content);
        this.status = Objects.requireNonNull(status); this.confirmedExpenseId = confirmedExpenseId;
        this.createdAt = Objects.requireNonNull(createdAt); this.updatedAt = Objects.requireNonNull(updatedAt);
        this.confirmedAt = confirmedAt; this.discardedAt = discardedAt;
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        this.version = version; validateLifecycle();
    }

    public static ExpenseDraft create(ExpenseDraftId id, HouseholdRef householdId,
            MemberRef createdByMemberId, Instant now) {
        return new ExpenseDraft(id, householdId, createdByMemberId,
                new ExpenseDraftContent(null, null, null, null, null, null), ExpenseDraftStatus.OPEN,
                null, now, now, null, null, 0);
    }

    public static ExpenseDraft rehydrate(ExpenseDraftId id, HouseholdRef householdId,
            MemberRef createdByMemberId, ExpenseDraftContent content, ExpenseDraftStatus status,
            ExpenseId confirmedExpenseId, Instant createdAt, Instant updatedAt, Instant confirmedAt,
            Instant discardedAt, long version) {
        return new ExpenseDraft(id, householdId, createdByMemberId, content, status, confirmedExpenseId,
                createdAt, updatedAt, confirmedAt, discardedAt, version);
    }

    public void update(ExpenseDraftContent content, Instant now) {
        requireOpen(); this.content = Objects.requireNonNull(content); updatedAt = Objects.requireNonNull(now);
    }

    public void confirm(ExpenseId expenseId, Instant now) {
        requireOpen(); status = ExpenseDraftStatus.CONFIRMED; confirmedExpenseId = Objects.requireNonNull(expenseId);
        confirmedAt = Objects.requireNonNull(now); updatedAt = now;
    }

    public void discard(Instant now) {
        requireOpen(); status = ExpenseDraftStatus.DISCARDED; discardedAt = Objects.requireNonNull(now); updatedAt = now;
    }

    private void requireOpen() {
        if (status != ExpenseDraftStatus.OPEN) throw new ExpenseStateException("Only an open draft can be changed");
    }

    private void validateLifecycle() {
        boolean confirmed = status == ExpenseDraftStatus.CONFIRMED;
        boolean discarded = status == ExpenseDraftStatus.DISCARDED;
        if (confirmed != (confirmedExpenseId != null && confirmedAt != null) || discarded != (discardedAt != null)) {
            throw new ExpenseRuleViolationException("Draft lifecycle is inconsistent");
        }
        if (confirmed && discardedAt != null || discarded && (confirmedExpenseId != null || confirmedAt != null)) {
            throw new ExpenseRuleViolationException("Draft lifecycle is inconsistent");
        }
    }

    public ExpenseDraftId id() { return id; }
    public HouseholdRef householdId() { return householdId; }
    public MemberRef createdByMemberId() { return createdByMemberId; }
    public ExpenseDraftContent content() { return content; }
    public ExpenseDraftStatus status() { return status; }
    public ExpenseId confirmedExpenseId() { return confirmedExpenseId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant confirmedAt() { return confirmedAt; }
    public Instant discardedAt() { return discardedAt; }
    public long version() { return version; }
}
