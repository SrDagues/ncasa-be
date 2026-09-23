package ncasa.identityaccess.application.verification;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
        String formattedExpiration = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
                .withLocale(locale).withZone(ZoneOffset.UTC).format(event.expiresAt()) + " UTC";
        String subject = "Solo falta confirmar tu correo en nCasa";
        String html = """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="color-scheme" content="light">
                  <title>Confirma tu correo en nCasa</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f7f1e7;color:#202624;font-family:Arial,Helvetica,sans-serif">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent">
                    Confirma tu correo y empieza a organizar vuestro hogar.
                  </div>
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                         style="width:100%%;background-color:#f7f1e7">
                    <tr>
                      <td align="center" style="padding:32px 16px">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0"
                               style="width:100%%;max-width:600px;background-color:#fffcf7;border-radius:18px;overflow:hidden;box-shadow:0 12px 34px rgba(18,60,54,0.09)">
                          <tr>
                            <td style="padding:22px 38px;background-color:#e3ece5;border-bottom:4px solid #f26b5b">
                              <span style="color:#123c36;font-size:22px;font-weight:700;letter-spacing:-0.8px">nCasa</span><span style="color:#f26b5b;font-size:22px;font-weight:700">.</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 38px 38px">
                              <span style="display:inline-block;padding:8px 12px;border-radius:999px;background-color:#e3ece5;color:#123c36;font-size:13px;font-weight:700">Tu hogar digital</span>
                              <p style="margin:24px 0 10px;color:#e05543;font-size:12px;font-weight:700;letter-spacing:1.1px;text-transform:uppercase">Un último paso</p>
                              <h1 style="margin:0;color:#123c36;font-size:30px;line-height:1.15;letter-spacing:-0.8px">¡Tu casa ya casi está lista!</h1>
                              <p style="margin:16px 0 0;color:#202624;font-size:16px;line-height:1.6">Confirma tu correo para entrar en nCasa y empezar a organizar todo lo que compartís en el hogar.</p>
                              <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin-top:24px">
                                <tr>
                                  <td bgcolor="#f26b5b" style="border-radius:12px;background-color:#f26b5b">
                                    <a href="%s" target="_blank" rel="noopener" style="display:inline-block;padding:14px 24px;color:#fffcf7;font-size:16px;font-weight:700;text-decoration:none">Confirmar mi correo</a>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:18px 0 0;color:#66706c;font-size:13px;line-height:1.5">Por seguridad, este enlace caduca el %s.</p>
                              <p style="margin:24px 0 0;padding-top:18px;border-top:1px solid #dcd8cf;color:#66706c;font-size:12px;line-height:1.5">Si el botón no funciona, copia y pega este enlace en tu navegador:<br><a href="%s" style="color:#123c36;word-break:break-all">%s</a></p>
                            </td>
                          </tr>
                        </table>
                        <p style="max-width:576px;margin:18px 12px 0;color:#66706c;font-size:12px;line-height:1.55;text-align:center">Si no has creado una cuenta en nCasa, puedes ignorar este mensaje con tranquilidad.</p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escapedLink, formattedExpiration, escapedLink, escapedLink);
        String text = """
                ¡Tu casa ya casi está lista!

                Confirma tu correo para entrar en nCasa y empezar a organizar todo lo que compartís en el hogar.

                %s

                Por seguridad, este enlace caduca el %s.

                Si no has creado una cuenta en nCasa, puedes ignorar este mensaje con tranquilidad.
                """.formatted(link, formattedExpiration);
        return new TransactionalEmail(event.recipient().value(), subject, html, text,
                "email-verification/" + event.requestId(), java.util.Map.of("category", "email-verification"));
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
