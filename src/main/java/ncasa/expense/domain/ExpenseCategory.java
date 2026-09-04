package ncasa.expense.domain;

import java.time.Instant;
import java.util.Objects;

public final class ExpenseCategory {
    private final ExpenseCategoryId id;
    private final HouseholdRef householdId;
    private final MemberRef createdByMemberId;
    private CategoryName name;
    private ExpenseCategoryStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant archivedAt;
    private final long version;

    private ExpenseCategory(ExpenseCategoryId id, HouseholdRef householdId, MemberRef createdByMemberId,
            CategoryName name, ExpenseCategoryStatus status, Instant createdAt, Instant updatedAt,
            Instant archivedAt, long version) {
        this.id = Objects.requireNonNull(id); this.householdId = Objects.requireNonNull(householdId);
        this.createdByMemberId = Objects.requireNonNull(createdByMemberId); this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status); this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt); this.archivedAt = archivedAt;
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        this.version = version;
        if ((status == ExpenseCategoryStatus.ARCHIVED) != (archivedAt != null)) {
            throw new ExpenseRuleViolationException("Archived category lifecycle is inconsistent");
        }
    }

    public static ExpenseCategory create(ExpenseCategoryId id, HouseholdRef householdId,
            MemberRef createdByMemberId, CategoryName name, Instant now) {
        return new ExpenseCategory(id, householdId, createdByMemberId, name, ExpenseCategoryStatus.ACTIVE,
                now, now, null, 0);
    }

    public static ExpenseCategory rehydrate(ExpenseCategoryId id, HouseholdRef householdId,
            MemberRef createdByMemberId, CategoryName name, ExpenseCategoryStatus status,
            Instant createdAt, Instant updatedAt, Instant archivedAt, long version) {
        return new ExpenseCategory(id, householdId, createdByMemberId, name, status,
                createdAt, updatedAt, archivedAt, version);
    }

    public void rename(CategoryName newName, Instant now) {
        requireActive();
        name = Objects.requireNonNull(newName); updatedAt = Objects.requireNonNull(now);
    }

    public void archive(Instant now) {
        requireActive(); status = ExpenseCategoryStatus.ARCHIVED;
        archivedAt = Objects.requireNonNull(now); updatedAt = now;
    }

    public void requireActive() {
        if (status != ExpenseCategoryStatus.ACTIVE) throw new ExpenseStateException("Category is archived");
    }

    public ExpenseCategoryId id() { return id; }
    public HouseholdRef householdId() { return householdId; }
    public MemberRef createdByMemberId() { return createdByMemberId; }
    public CategoryName name() { return name; }
    public ExpenseCategoryStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant archivedAt() { return archivedAt; }
    public long version() { return version; }
}
