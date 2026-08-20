package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
class JpaExpenseEntity {
    @Id private UUID id;
    @Column(name = "household_id", nullable = false) private UUID householdId;
    @Column(name = "created_by_member_id", nullable = false) private UUID createdByMemberId;
    @Column(name = "payer_member_id", nullable = false) private UUID payerMemberId;
    @Column(nullable = false, length = 240) private String description;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "expense_date", nullable = false) private LocalDate expenseDate;
    @Column(name = "split_type", nullable = false, length = 20) private String splitType;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 20) private String source;
    @Column(name = "void_reason", length = 500) private String voidReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "voided_at") private Instant voidedAt;
    @Version private long version;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JpaExpenseAllocationEntity> allocations = new ArrayList<>();

    protected JpaExpenseEntity() {}

    JpaExpenseEntity(UUID id, UUID householdId, UUID createdByMemberId, UUID payerMemberId,
            String description, BigDecimal amount, String currency, LocalDate expenseDate,
            String splitType, String status, String source, String voidReason, Instant createdAt,
            Instant updatedAt, Instant voidedAt, long version) {
        this.id = id; this.householdId = householdId; this.createdByMemberId = createdByMemberId;
        this.payerMemberId = payerMemberId; this.description = description; this.amount = amount;
        this.currency = currency; this.expenseDate = expenseDate; this.splitType = splitType;
        this.status = status; this.source = source; this.voidReason = voidReason; this.createdAt = createdAt;
        this.updatedAt = updatedAt; this.voidedAt = voidedAt; this.version = version;
    }

    void addAllocation(JpaExpenseAllocationEntity allocation) {
        allocations.add(allocation); allocation.attachTo(this);
    }
    UUID id() { return id; }
    UUID householdId() { return householdId; }
    UUID createdByMemberId() { return createdByMemberId; }
    UUID payerMemberId() { return payerMemberId; }
    String description() { return description; }
    BigDecimal amount() { return amount; }
    String currency() { return currency; }
    LocalDate expenseDate() { return expenseDate; }
    String splitType() { return splitType; }
    String status() { return status; }
    String source() { return source; }
    String voidReason() { return voidReason; }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
    Instant voidedAt() { return voidedAt; }
    long version() { return version; }
    List<JpaExpenseAllocationEntity> allocations() { return List.copyOf(allocations); }
}
