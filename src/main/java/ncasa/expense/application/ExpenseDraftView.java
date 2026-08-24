package ncasa.expense.application;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import ncasa.expense.domain.*;

public record ExpenseDraftView(UUID id,UUID householdId,UUID createdByMemberId,String description,
        UUID payerMemberId,BigDecimal amount,String currency,LocalDate expenseDate,UUID categoryId,
        ExpenseSplitType splitType,List<DraftAllocationView> allocations,ExpenseDraftStatus status,
        UUID confirmedExpenseId,Instant createdAt,Instant updatedAt,Instant confirmedAt,Instant discardedAt,long version) {
    public record DraftAllocationView(UUID memberId,BigDecimal amount,BigDecimal percentage){}
    public static ExpenseDraftView from(ExpenseDraft draft){
        var c=draft.content();var allocations=toAllocations(c.split());
        return new ExpenseDraftView(draft.id().value(),draft.householdId().value(),draft.createdByMemberId().value(),
                c.description()==null?null:c.description().value(),c.payerMemberId()==null?null:c.payerMemberId().value(),
                c.total()==null?null:c.total().amount(),c.total()==null?null:c.total().currency(),c.expenseDate(),
                c.categoryId()==null?null:c.categoryId().value(),c.split()==null?null:c.split().type(),allocations,draft.status(),
                draft.confirmedExpenseId()==null?null:draft.confirmedExpenseId().value(),draft.createdAt(),draft.updatedAt(),
                draft.confirmedAt(),draft.discardedAt(),draft.version());
    }
    private static List<DraftAllocationView> toAllocations(DraftSplit split){
        if(split==null)return List.of();
        if(split instanceof EqualDraftSplit equal)return equal.members().stream().map(m->new DraftAllocationView(m.value(),null,null)).toList();
        if(split instanceof ExactDraftSplit exact)return exact.allocations().stream().map(a->new DraftAllocationView(a.memberId().value(),a.amount().amount(),null)).toList();
        var percentage=(PercentageDraftSplit)split;
        return percentage.allocations().stream().map(a->new DraftAllocationView(a.memberId().value(),null,a.percentage().value())).toList();
    }
}
