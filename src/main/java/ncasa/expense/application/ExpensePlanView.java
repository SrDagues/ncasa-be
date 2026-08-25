package ncasa.expense.application;

import java.math.BigDecimal;import java.time.*;import java.util.*;import ncasa.expense.domain.*;

public record ExpensePlanView(UUID id,UUID householdId,UUID createdByMemberId,TemplateView template,
        ExpensePlanFrequency frequency,LocalDate startDate,String zoneId,String endCondition,LocalDate endDate,
        Integer totalOccurrences,int reminderDaysBefore,int materializedOccurrences,LocalDate nextOccurrence,
        Instant nextOccurrenceDueAt,Instant nextReminderAt,ExpensePlanStatus status,String pauseReason,
        String cancellationReason,Instant createdAt,Instant updatedAt,Instant pausedAt,Instant cancelledAt,
        Instant completedAt,long version){
    public record TemplateView(String description,BigDecimal amount,String currency,UUID payerMemberId,UUID categoryId,
            ExpenseSplitType splitType,List<AllocationView> allocations){}
    public record AllocationView(UUID memberId,BigDecimal amount,BigDecimal percentage){}
    public static ExpensePlanView from(ExpensePlan p){
        var split=p.template().split();List<AllocationView> allocations;
        if(split instanceof EqualTemplateSplit equal) allocations=equal.participants().stream().map(m->new AllocationView(m.value(),null,null)).toList();
        else if(split instanceof ExactTemplateSplit exact) allocations=exact.allocations().stream().map(a->new AllocationView(a.memberId().value(),a.amount().amount(),null)).toList();
        else {var pct=(PercentageTemplateSplit)split;allocations=pct.percentages().stream().map(a->new AllocationView(a.memberId().value(),null,a.percentage().value())).toList();}
        var template=new TemplateView(p.template().description().value(),p.template().total().amount(),p.template().total().currency(),p.template().payerMemberId().value(),p.template().categoryId()==null?null:p.template().categoryId().value(),split.type(),allocations);
        return new ExpensePlanView(p.id().value(),p.householdId().value(),p.createdByMemberId().value(),template,p.schedule().frequency(),p.schedule().startDate(),p.zoneId().getId(),p.endCondition().type(),p.endCondition().endDate(),p.endCondition().occurrenceLimit(),p.reminderDaysBefore(),p.materializedOccurrences(),p.nextOccurrence(),p.nextOccurrenceDueAt(),p.nextReminderAt(),p.status(),p.pauseReason(),p.cancellationReason(),p.createdAt(),p.updatedAt(),p.pausedAt(),p.cancelledAt(),p.completedAt(),p.version());
    }
}
