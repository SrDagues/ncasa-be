package ncasa.expense.application.plan;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import ncasa.expense.application.ExpensePlanNotFoundException;
import ncasa.expense.application.port.out.ExpensePlanRepository;
import ncasa.expense.domain.ExpensePlanId;
import ncasa.expense.domain.HouseholdRef;

public final class GetExpensePlanNotificationContextUseCase {
    private final ExpensePlanRepository plans;
    public GetExpensePlanNotificationContextUseCase(ExpensePlanRepository plans){this.plans=plans;}
    public Context execute(UUID householdId,UUID planId){
        var plan=plans.findByIdAndHousehold(new ExpensePlanId(planId),new HouseholdRef(householdId))
                .orElseThrow(ExpensePlanNotFoundException::new);
        return new Context(plan.id().value(),plan.householdId().value(),plan.createdByMemberId().value(),
                plan.template().payerMemberId().value(),plan.template().split().participants().stream()
                        .map(member->member.value()).collect(Collectors.toUnmodifiableSet()),
                plan.template().description().value(),plan.pauseReason());
    }
    public record Context(UUID planId,UUID householdId,UUID createdByMemberId,UUID payerMemberId,
            Set<UUID> participantMemberIds,String description,String attentionReason){
        public Context{participantMemberIds=Set.copyOf(participantMemberIds);}
    }
}
