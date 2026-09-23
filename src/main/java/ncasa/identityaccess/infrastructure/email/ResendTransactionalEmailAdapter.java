package ncasa.identityaccess.infrastructure.email;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import ncasa.identityaccess.application.port.out.TransactionalEmail;
import ncasa.identityaccess.application.port.out.TransactionalEmailSender;
import ncasa.identityaccess.infrastructure.config.ResendProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "ncasa.email.resend.enabled", havingValue = "true", matchIfMissing = true)
public class ResendTransactionalEmailAdapter implements TransactionalEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendTransactionalEmailAdapter.class);
    private final RestClient client;
    private final ObjectMapper json;
    private final ResendProperties properties;

    @Autowired
    public ResendTransactionalEmailAdapter(ObjectMapper json, ResendProperties properties) {
        this(json, properties, createClient(properties));
    }

    ResendTransactionalEmailAdapter(ObjectMapper json, ResendProperties properties, RestClient client) {
        this.json = json;
        this.properties = properties;
        this.client = client;
    }

    private static RestClient createClient(ResendProperties properties) {
        var httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder().baseUrl(properties.apiUrl().toString())
                .requestFactory(requestFactory).build();
    }

    @Override
    public void send(TransactionalEmail email) {
        var request = new SendEmailRequest(properties.from(), email.recipient(), email.subject(), email.html(),
                email.text(), email.tags().entrySet().stream().map(entry -> new Tag(entry.getKey(), entry.getValue())).toList());
        RuntimeException last = null;
        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
            try {
                sendOnce(email.idempotencyKey(), request);
                return;
            } catch (ResendDeliveryException failure) {
                last = failure;
                if (failure.quotaExceeded()) {
                    LOGGER.atError().addKeyValue("event.action", "transactional_email_quota_exceeded")
                            .log("transactional_email_quota_exceeded");
                }
                if (!failure.retryable() || attempt == properties.maxAttempts()) throw failure;
                pause(attempt, failure.retryAfterSeconds());
            } catch (RestClientException failure) {
                last = new IllegalStateException("Transactional email delivery failed", failure);
                if (attempt == properties.maxAttempts()) throw last;
                pause(attempt, 0);
            }
        }
        throw last == null ? new IllegalStateException("Transactional email delivery failed") : last;
    }

    private void sendOnce(String idempotencyKey, SendEmailRequest request) {
        client.post().uri("/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .exchange((httpRequest, response) -> {
                    byte[] bytes = response.getBody().readAllBytes();
                    if (response.getStatusCode().is2xxSuccessful()) {
                        var sent = json.readValue(new String(bytes, StandardCharsets.UTF_8), SendEmailResponse.class);
                        if (sent.id() == null || sent.id().isBlank()) {
                            throw new ResendDeliveryException(true, false, 0);
                        }
                        return null;
                    }
                    String type = errorType(bytes);
                    int status = response.getStatusCode().value();
                    boolean quota = status == 429 && ("daily_quota_exceeded".equals(type)
                            || "monthly_quota_exceeded".equals(type));
                    boolean retryable = status >= 500
                            || status == 409 && "concurrent_idempotent_requests".equals(type)
                            || status == 429 && "rate_limit_exceeded".equals(type);
                    throw new ResendDeliveryException(retryable, quota,
                            retryAfter(response.getHeaders().getFirst("Retry-After")));
                });
    }

    private String errorType(byte[] body) {
        try {
            return json.readValue(new String(body, StandardCharsets.UTF_8), ErrorResponse.class).name();
        } catch (RuntimeException failure) {
            return "unknown";
        }
    }

    private long retryAfter(String value) {
        try { return value == null ? 0 : Math.max(0, Long.parseLong(value)); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private void pause(int attempt, long retryAfterSeconds) {
        long exponentialMillis = Math.min(4_000, 250L * (1L << Math.min(attempt - 1, 4)));
        long millis = retryAfterSeconds > 0 ? Duration.ofSeconds(retryAfterSeconds).toMillis()
                : exponentialMillis + ThreadLocalRandom.current().nextLong(101);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transactional email retry interrupted");
        }
    }

    private record SendEmailRequest(String from, String to, String subject, String html, String text, List<Tag> tags) {}
    private record Tag(String name, String value) {}
    private record SendEmailResponse(String id) {}
    private record ErrorResponse(String name) {}
}
