package ncasa.identityaccess.application.verification;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import ncasa.identityaccess.application.port.out.TransactionalEmail;

public final class EmailVerificationTemplate {
    private final URI frontendUrl;

    public EmailVerificationTemplate(URI frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public TransactionalEmail render(EmailVerificationRequested event, Locale locale) {
        String link = frontendUrl.toString().replaceAll("/$", "") + "/verify-email#token=" + event.rawToken();
        String escapedLink = escape(link);
        String subject = "Confirma tu email en nCasa";
        String html = """
                <!doctype html><html lang="es"><body style="font-family:Arial,sans-serif;color:#173b2d">
                <main style="max-width:560px;margin:auto;padding:32px 20px">
                <h1>Confirma tu email</h1>
                <p>Confirma tu dirección para activar tu cuenta de nCasa.</p>
                <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#173b2d;color:white;text-decoration:none;border-radius:6px">Confirmar email</a></p>
                <p>Este enlace caduca el %s.</p>
                <p>Si no has creado esta cuenta, puedes ignorar este mensaje.</p>
                </main></body></html>
                """.formatted(escapedLink, event.expiresAt());
        String text = "Confirma tu dirección para activar tu cuenta de nCasa.\n\n" + link
                + "\n\nEste enlace caduca el " + event.expiresAt()
                + ".\n\nSi no has creado esta cuenta, puedes ignorar este mensaje.";
        return new TransactionalEmail(event.recipient().value(), subject, html, text,
                "email-verification/" + event.requestId(), java.util.Map.of("category", "email-verification"));
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
