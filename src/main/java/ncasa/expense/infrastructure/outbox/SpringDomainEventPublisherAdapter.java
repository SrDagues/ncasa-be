package ncasa.expense.infrastructure.outbox;
import ncasa.expense.application.port.out.*;import org.springframework.context.ApplicationEventPublisher;import org.springframework.stereotype.Component;
@Component public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort{private final ApplicationEventPublisher publisher;public SpringDomainEventPublisherAdapter(ApplicationEventPublisher p){publisher=p;}public void publish(OutboxMessage message){publisher.publishEvent(new PublishedOutboxEvent(message.id(),message.eventType(),message.payload(),message.occurredAt()));}}
