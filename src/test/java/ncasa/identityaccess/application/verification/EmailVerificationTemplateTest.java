package ncasa.identityaccess.application.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import ncasa.identityaccess.domain.Email;
import org.junit.jupiter.api.Test;

class EmailVerificationTemplateTest {
    @Test
    void shouldRenderTheWarmNcasaDesignWithAnEquivalentPlainTextFallback() {
        var template = new EmailVerificationTemplate(URI.create("https://app.ncasa.es"));
        var event = new EmailVerificationRequested(UUID.fromString("3b7e9d77-9af2-4d48-88de-97def9f16308"),
                Email.of("user@example.com"), "safe_token-123", Instant.parse("2026-09-20T12:00:00Z"));

        var email = template.render(event, Locale.forLanguageTag("es"));

        assertThat(email.subject()).isEqualTo("Solo falta confirmar tu correo en nCasa");
        assertThat(email.html())
                .contains("role=\"presentation\"")
                .contains("background-color:#f7f1e7")
                .contains("background-color:#e3ece5")
                .contains("color:#123c36")
                .contains("background-color:#f26b5b")
                .contains("¡Tu casa ya casi está lista!")
                .contains("Confirmar mi correo")
                .contains("https://app.ncasa.es/verify-email#token=safe_token-123")
                .contains("20 de septiembre de 2026");
        assertThat(email.text())
                .contains("¡Tu casa ya casi está lista!")
                .contains("https://app.ncasa.es/verify-email#token=safe_token-123")
                .contains("20 de septiembre de 2026")
                .doesNotContain("<table", "style=");
    }
}
