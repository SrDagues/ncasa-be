package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="expense_draft_allocations")
class JpaExpenseDraftAllocationEntity {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="draft_id",nullable=false)private JpaExpenseDraftEntity draft;
    @Column(name="member_id",nullable=false)private UUID memberId;
    @Column(precision=19,scale=4)private BigDecimal amount;
    @Column(precision=7,scale=2)private BigDecimal percentage;
    protected JpaExpenseDraftAllocationEntity(){}
    JpaExpenseDraftAllocationEntity(UUID id,UUID memberId,BigDecimal amount,BigDecimal percentage){this.id=id;this.memberId=memberId;this.amount=amount;this.percentage=percentage;}
    void attachTo(JpaExpenseDraftEntity draft){this.draft=draft;}UUID memberId(){return memberId;}BigDecimal amount(){return amount;}BigDecimal percentage(){return percentage;}
}
