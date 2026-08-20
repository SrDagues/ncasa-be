package ncasa.expense.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class Expense {
    private final ExpenseId id;
    private final HouseholdRef householdId;
    private final MemberRef createdByMemberId;
    private final MemberRef payerMemberId;
    private final Money total;
    private final ExpenseDescription description;
    private final LocalDate expenseDate;
    private final ExpenseSplit split;
    private ExpenseStatus status;
    private final ExpenseSource source;
    private final Instant createdAt;
    private Instant updatedAt;
    private VoidReason voidReason;
    private Instant voidedAt;
    private final long version;

    private Expense(ExpenseId id, HouseholdRef householdId, MemberRef createdByMemberId,
            MemberRef payerMemberId, Money total, ExpenseDescription description, LocalDate expenseDate,
            ExpenseSplit split, ExpenseStatus status, ExpenseSource source, Instant createdAt,
            Instant updatedAt, VoidReason voidReason, Instant voidedAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.householdId = Objects.requireNonNull(householdId);
        this.createdByMemberId = Objects.requireNonNull(createdByMemberId);
        this.payerMemberId = Objects.requireNonNull(payerMemberId);
        this.total = Objects.requireNonNull(total);
        if (!total.isPositive()) throw new ExpenseRuleViolationException("Expense total must be positive");
        this.description = Objects.requireNonNull(description);
        this.expenseDate = Objects.requireNonNull(expenseDate);
        this.split = Objects.requireNonNull(split);
        total.requireSameCurrency(split.total());
        if (total.amount().compareTo(split.total().amount()) != 0) {
            throw new ExpenseRuleViolationException("Expense split total must match expense total");
        }
        this.status = Objects.requireNonNull(status);
        this.source = Objects.requireNonNull(source);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        this.version = version;
        this.voidReason = voidReason;
        this.voidedAt = voidedAt;
        validateLifecycle();
    }

    public static Expense confirmedManual(ExpenseId id, HouseholdRef householdId, MemberRef createdByMemberId,
            MemberRef payerMemberId, Money total, ExpenseDescription description, LocalDate expenseDate,
            ExpenseSplit split, Instant now) {
        return new Expense(id, householdId, createdByMemberId, payerMemberId, total, description,
                expenseDate, split, ExpenseStatus.CONFIRMED, ExpenseSource.MANUAL, now, now, null, null, 0);
    }

    public static Expense rehydrate(ExpenseId id, HouseholdRef householdId, MemberRef createdByMemberId,
            MemberRef payerMemberId, Money total, ExpenseDescription description, LocalDate expenseDate,
            ExpenseSplit split, ExpenseStatus status, ExpenseSource source, Instant createdAt,
            Instant updatedAt, VoidReason voidReason, Instant voidedAt, long version) {
        return new Expense(id, householdId, createdByMemberId, payerMemberId, total, description,
                expenseDate, split, status, source, createdAt, updatedAt, voidReason, voidedAt, version);
    }

    public void voidExpense(VoidReason reason, Instant now) {
        if (status != ExpenseStatus.CONFIRMED) {
            throw new ExpenseStateException("Only a confirmed expense can be voided");
        }
        status = ExpenseStatus.VOIDED;
        voidReason = Objects.requireNonNull(reason);
        voidedAt = Objects.requireNonNull(now);
        updatedAt = now;
    }

    private void validateLifecycle() {
        if (status == ExpenseStatus.VOIDED && (voidReason == null || voidedAt == null)) {
            throw new ExpenseRuleViolationException("A voided expense requires reason and timestamp");
        }
        if (status != ExpenseStatus.VOIDED && (voidReason != null || voidedAt != null)) {
            throw new ExpenseRuleViolationException("Only a voided expense can have void details");
        }
    }

    public ExpenseId id() { return id; }
    public HouseholdRef householdId() { return householdId; }
    public MemberRef createdByMemberId() { return createdByMemberId; }
    public MemberRef payerMemberId() { return payerMemberId; }
    public Money total() { return total; }
    public ExpenseDescription description() { return description; }
    public LocalDate expenseDate() { return expenseDate; }
    public ExpenseSplit split() { return split; }
    public ExpenseStatus status() { return status; }
    public ExpenseSource source() { return source; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public VoidReason voidReason() { return voidReason; }
    public Instant voidedAt() { return voidedAt; }
    public long version() { return version; }
}
