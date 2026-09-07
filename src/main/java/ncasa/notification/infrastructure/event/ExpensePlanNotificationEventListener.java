package ncasa.notification.infrastructure.event;

import ncasa.expense.infrastructure.outbox.PublishedOutboxEvent;import ncasa.notification.application.ConsumeExpensePlanNotificationUseCase;import org.slf4j.*;import org.springframework.context.event.EventListener;import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;

@Component public class ExpensePlanNotificationEventListener{
    private static final Logger LOG=LoggerFactory.getLogger(ExpensePlanNotificationEventListener.class);
    private final ExpensePlanNotificationEventParser parser;private final ConsumeExpensePlanNotificationUseCase consume;
    public ExpensePlanNotificationEventListener(ExpensePlanNotificationEventParser p,ConsumeExpensePlanNotificationUseCase c){parser=p;consume=c;}
    @EventListener @Transactional public void on(PublishedOutboxEvent event){try{parser.parse(event).ifPresent(command->{int created=consume.execute(command);LOG.atInfo().addKeyValue("event.action",created==0?"notification_deduplicated":"notification_created").addKeyValue("integration_event.id",event.eventId()).addKeyValue("notification.kind",command.kind()).addKeyValue("notification.created_count",created).log("expense_plan_notification_consumed");});}catch(RuntimeException ex){LOG.atError().addKeyValue("event.action","notification_consumption_failed").addKeyValue("integration_event.id",event.eventId()).addKeyValue("integration_event.type",event.eventType()).setCause(ex).log("notification_consumption_failed");throw ex;}}
}
