package ncasa.notification.infrastructure.expense;

import java.util.UUID;import ncasa.expense.application.plan.GetExpensePlanNotificationContextUseCase;import ncasa.notification.application.port.out.ExpensePlanNotificationSourcePort;import org.springframework.stereotype.Component;

@Component public class ExpensePlanNotificationSourceAdapter implements ExpensePlanNotificationSourcePort{
    private final GetExpensePlanNotificationContextUseCase query;public ExpensePlanNotificationSourceAdapter(GetExpensePlanNotificationContextUseCase q){query=q;}
    public Context get(UUID householdId,UUID planId){var c=query.execute(householdId,planId);return new Context(c.planId(),c.householdId(),c.createdByMemberId(),c.payerMemberId(),c.participantMemberIds(),c.description(),c.attentionReason());}
}
