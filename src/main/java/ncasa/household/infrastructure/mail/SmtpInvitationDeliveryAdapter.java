package ncasa.household.infrastructure.mail;

import ncasa.household.application.port.out.InvitationDeliveryPort;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.infrastructure.config.HouseholdInvitationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpInvitationDeliveryAdapter implements InvitationDeliveryPort {
    private final JavaMailSender mailSender; private final HouseholdInvitationProperties properties;
    public SmtpInvitationDeliveryAdapter(JavaMailSender mailSender, HouseholdInvitationProperties properties) {
        this.mailSender = mailSender; this.properties = properties;
    }
    public void deliver(HouseholdInvitation invitation, String rawToken) {
        var message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(invitation.email().value());
        message.setSubject("Invitación a un hogar de nCasa");
        message.setText("Has recibido una invitación para unirte a un hogar de nCasa.\n\n" + properties.frontendUrl()
                + "\n\nLa invitación caduca el " + invitation.expiry().value() + ".");
        mailSender.send(message);
    }
}
