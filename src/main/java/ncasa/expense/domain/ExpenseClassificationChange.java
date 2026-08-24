package ncasa.expense.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ExpenseClassificationChange(UUID id,ExpenseId expenseId,HouseholdRef householdId,
        MemberRef changedByMemberId,ExpenseCategoryId previousCategoryId,ExpenseCategoryId newCategoryId,
        String reason,Instant changedAt){
    public ExpenseClassificationChange{
        Objects.requireNonNull(id);Objects.requireNonNull(expenseId);Objects.requireNonNull(householdId);
        Objects.requireNonNull(changedByMemberId);Objects.requireNonNull(changedAt);
        if(Objects.equals(previousCategoryId,newCategoryId))throw new ExpenseRuleViolationException("Classification must change");
        if(reason!=null){reason=reason.trim();if(reason.isEmpty())reason=null;else if(reason.length()>500)throw new ExpenseRuleViolationException("Classification reason is too long");}
    }
}
