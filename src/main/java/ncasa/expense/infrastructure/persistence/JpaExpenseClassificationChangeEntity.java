package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="expense_classification_changes")
class JpaExpenseClassificationChangeEntity {
    @Id private UUID id;
    @Column(name="expense_id",nullable=false)private UUID expenseId;
    @Column(name="household_id",nullable=false)private UUID householdId;
    @Column(name="changed_by_member_id",nullable=false)private UUID changedByMemberId;
    @Column(name="previous_category_id")private UUID previousCategoryId;
    @Column(name="new_category_id")private UUID newCategoryId;
    @Column(length=500)private String reason;
    @Column(name="changed_at",nullable=false)private Instant changedAt;
    protected JpaExpenseClassificationChangeEntity(){}
    JpaExpenseClassificationChangeEntity(UUID id,UUID expenseId,UUID householdId,UUID changedByMemberId,UUID previousCategoryId,UUID newCategoryId,String reason,Instant changedAt){
        this.id=id;this.expenseId=expenseId;this.householdId=householdId;this.changedByMemberId=changedByMemberId;this.previousCategoryId=previousCategoryId;this.newCategoryId=newCategoryId;this.reason=reason;this.changedAt=changedAt;}
    UUID id(){return id;}UUID expenseId(){return expenseId;}UUID householdId(){return householdId;}UUID changedByMemberId(){return changedByMemberId;}
    UUID previousCategoryId(){return previousCategoryId;}UUID newCategoryId(){return newCategoryId;}String reason(){return reason;}Instant changedAt(){return changedAt;}
}
