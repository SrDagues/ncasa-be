package ncasa.identityaccess.application.port.out;

import ncasa.identityaccess.application.verification.EmailVerificationRequested;

public interface EmailVerificationEventPublisher {
    void publish(EmailVerificationRequested event);
}
