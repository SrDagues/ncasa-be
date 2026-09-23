package ncasa.identityaccess.infrastructure.event;

import ncasa.identityaccess.application.verification.EmailVerificationRequested;
import ncasa.identityaccess.application.verification.SendEmailVerificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailVerificationRequestedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationRequestedListener.class);
    private final SendEmailVerificationHandler handler;

    public EmailVerificationRequestedListener(SendEmailVerificationHandler handler) { this.handler = handler; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EmailVerificationRequested event) {
        try {
            handler.handle(event);
        } catch (RuntimeException failure) {
            LOGGER.atError().addKeyValue("event.action", "email_verification_delivery_failed")
                    .addKeyValue("verificationRequestId", event.requestId())
                    .addKeyValue("failure.type", failure.getClass().getSimpleName())
                    .log("email_verification_delivery_failed");
        }
    }
}
