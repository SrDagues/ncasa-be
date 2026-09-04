package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name="expense_drafts")
class JpaExpenseDraftEntity {
    @Id private UUID id;
    @Column(name="household_id",nullable=false)private UUID householdId;
    @Column(name="created_by_member_id",nullable=false)private UUID createdByMemberId;
    @Column(length=240)private String description;
    @Column(name="payer_member_id")private UUID payerMemberId;
    @Column(precision=19,scale=4)private BigDecimal amount;
    @Column(length=3)private String currency;
    @Column(name="expense_date")private LocalDate expenseDate;
    @Column(name="category_id")private UUID categoryId;
    @Column(name="split_type",length=20)private String splitType;
    @Column(nullable=false,length=20)private String status;
    @Column(name="confirmed_expense_id")private UUID confirmedExpenseId;
    @Column(name="created_at",nullable=false,updatable=false)private Instant createdAt;
    @Column(name="updated_at",nullable=false)private Instant updatedAt;
    @Column(name="confirmed_at")private Instant confirmedAt;
    @Column(name="discarded_at")private Instant discardedAt;
    @Version private long version;
    @OneToMany(mappedBy="draft",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY)
    private List<JpaExpenseDraftAllocationEntity> allocations=new ArrayList<>();
    protected JpaExpenseDraftEntity(){}
    JpaExpenseDraftEntity(UUID id,UUID householdId,UUID createdByMemberId,String description,UUID payerMemberId,
            BigDecimal amount,String currency,LocalDate expenseDate,UUID categoryId,String splitType,String status,
            UUID confirmedExpenseId,Instant createdAt,Instant updatedAt,Instant confirmedAt,Instant discardedAt,long version){
        this.id=id;this.householdId=householdId;this.createdByMemberId=createdByMemberId;this.description=description;
        this.payerMemberId=payerMemberId;this.amount=amount;this.currency=currency;this.expenseDate=expenseDate;
        this.categoryId=categoryId;this.splitType=splitType;this.status=status;this.confirmedExpenseId=confirmedExpenseId;
        this.createdAt=createdAt;this.updatedAt=updatedAt;this.confirmedAt=confirmedAt;this.discardedAt=discardedAt;this.version=version;
    }
    void addAllocation(JpaExpenseDraftAllocationEntity allocation){allocations.add(allocation);allocation.attachTo(this);}
    UUID id(){return id;}UUID householdId(){return householdId;}UUID createdByMemberId(){return createdByMemberId;}
    String description(){return description;}UUID payerMemberId(){return payerMemberId;}BigDecimal amount(){return amount;}
    String currency(){return currency;}LocalDate expenseDate(){return expenseDate;}UUID categoryId(){return categoryId;}
    String splitType(){return splitType;}String status(){return status;}UUID confirmedExpenseId(){return confirmedExpenseId;}
    Instant createdAt(){return createdAt;}Instant updatedAt(){return updatedAt;}Instant confirmedAt(){return confirmedAt;}
    Instant discardedAt(){return discardedAt;}long version(){return version;}List<JpaExpenseDraftAllocationEntity> allocations(){return List.copyOf(allocations);}
}
