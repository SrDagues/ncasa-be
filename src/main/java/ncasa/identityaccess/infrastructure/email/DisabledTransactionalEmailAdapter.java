package ncasa.identityaccess.infrastructure.email;

import ncasa.identityaccess.application.port.out.TransactionalEmail;
import ncasa.identityaccess.application.port.out.TransactionalEmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ncasa.email.resend.enabled", havingValue = "false")
public class DisabledTransactionalEmailAdapter implements TransactionalEmailSender {
    @Override public void send(TransactionalEmail email) {}
}
