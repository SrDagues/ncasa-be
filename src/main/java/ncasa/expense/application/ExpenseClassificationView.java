package ncasa.expense.application;

import java.time.Instant;
import java.util.UUID;
import ncasa.expense.domain.ExpenseClassificationChange;

public record ExpenseClassificationView(UUID id,UUID expenseId,UUID changedByMemberId,UUID previousCategoryId,
        UUID newCategoryId,String reason,Instant changedAt){
    public static ExpenseClassificationView from(ExpenseClassificationChange c){return new ExpenseClassificationView(c.id(),c.expenseId().value(),c.changedByMemberId().value(),
            c.previousCategoryId()==null?null:c.previousCategoryId().value(),c.newCategoryId()==null?null:c.newCategoryId().value(),c.reason(),c.changedAt());}
}
