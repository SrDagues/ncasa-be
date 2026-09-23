package ncasa.identityaccess.infrastructure.event;

import ncasa.identityaccess.application.port.out.EmailVerificationEventPublisher;
import ncasa.identityaccess.application.verification.EmailVerificationRequested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEmailVerificationEventPublisher implements EmailVerificationEventPublisher {
    private final ApplicationEventPublisher publisher;
    public SpringEmailVerificationEventPublisher(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    @Override public void publish(EmailVerificationRequested event) { publisher.publishEvent(event); }
}
