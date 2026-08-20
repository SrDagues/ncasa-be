package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_allocations", uniqueConstraints =
        @UniqueConstraint(name = "uk_expense_allocation_member", columnNames = {"expense_id", "member_id"}))
class JpaExpenseAllocationEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private JpaExpenseEntity expense;
    @Column(name = "member_id", nullable = false) private UUID memberId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;

    protected JpaExpenseAllocationEntity() {}
    JpaExpenseAllocationEntity(UUID id, UUID memberId, BigDecimal amount) {
        this.id = id; this.memberId = memberId; this.amount = amount;
    }
    void attachTo(JpaExpenseEntity expense) { this.expense = expense; }
    UUID memberId() { return memberId; }
    BigDecimal amount() { return amount; }
}
