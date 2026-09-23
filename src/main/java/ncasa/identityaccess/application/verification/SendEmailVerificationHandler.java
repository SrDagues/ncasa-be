package ncasa.identityaccess.application.verification;

import java.util.Locale;
import ncasa.identityaccess.application.port.out.TransactionalEmailSender;

public final class SendEmailVerificationHandler {
    private final EmailVerificationTemplate template;
    private final TransactionalEmailSender sender;

    public SendEmailVerificationHandler(EmailVerificationTemplate template, TransactionalEmailSender sender) {
        this.template = template;
        this.sender = sender;
    }

    public void handle(EmailVerificationRequested event) {
        sender.send(template.render(event, Locale.forLanguageTag("es")));
    }
}
