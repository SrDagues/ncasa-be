package ncasa.notification.application;

import java.time.Clock;import java.util.*;import ncasa.notification.application.port.out.*;import ncasa.notification.domain.*;

public final class ConsumeExpensePlanNotificationUseCase {
    private final NotificationRepository notifications;private final ExpensePlanNotificationSourcePort plans;
    private final HouseholdNotificationDirectoryPort households;private final NotificationRecipientPolicy policy;private final Clock clock;
    public ConsumeExpensePlanNotificationUseCase(NotificationRepository n,ExpensePlanNotificationSourcePort p,
            HouseholdNotificationDirectoryPort h,NotificationRecipientPolicy policy,Clock clock){notifications=n;plans=p;households=h;this.policy=policy;this.clock=clock;}
    public int execute(ConsumeExpensePlanNotificationCommand command){
        var plan=plans.get(command.householdId(),command.planId());
        if(!plan.planId().equals(command.planId())||!plan.householdId().equals(command.householdId()))throw new IllegalArgumentException("Event and plan context do not match");
        var directory=households.get(command.householdId());if(!directory.active())return 0;
        var candidates=directory.members().stream().map(m->new NotificationRecipientPolicy.Candidate(m.memberId(),m.accountId(),m.active(),m.administrator())).toList();
        var audience=new NotificationRecipientPolicy.PlanAudience(plan.createdByMemberId(),plan.payerMemberId(),plan.participantMemberIds());
        var recipients=policy.recipients(command.kind(),audience,candidates);var eventId=new IntegrationEventId(command.eventId());
        var household=new HouseholdRef(command.householdId());var planId=new PlanRef(command.planId());var now=clock.instant();
        var created=recipients.stream().filter(recipient->!notifications.exists(eventId,recipient)).map(recipient->Notification.create(
                new NotificationId(UUID.randomUUID()),eventId,recipient,household,planId,command.kind(),plan.subject(),
                new NotificationAmount(command.amount(),command.currency()),command.occurrenceDate(),command.occurrenceNumber(),
                command.totalOccurrences(),command.kind()==NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED?plan.attentionReason():null,
                command.occurredAt(),now)).toList();
        if(!created.isEmpty())notifications.saveAll(created);return created.size();
    }
}
