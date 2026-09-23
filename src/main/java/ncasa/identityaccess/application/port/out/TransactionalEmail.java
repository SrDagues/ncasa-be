package ncasa.identityaccess.application.port.out;

import java.util.Map;

public record TransactionalEmail(String recipient, String subject, String html, String text,
        String idempotencyKey, Map<String, String> tags) {
    public TransactionalEmail {
        tags = Map.copyOf(tags);
    }
}
