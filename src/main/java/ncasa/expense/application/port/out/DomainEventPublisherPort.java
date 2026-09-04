package ncasa.expense.application.port.out;
public interface DomainEventPublisherPort{void publish(OutboxMessage message);}
