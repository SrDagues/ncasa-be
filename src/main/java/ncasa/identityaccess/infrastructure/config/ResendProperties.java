package ncasa.identityaccess.infrastructure.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ncasa.email.resend")
public final class ResendProperties {
    private final String apiKey;
    private final String from;
    private final URI apiUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxAttempts;

    public ResendProperties(String apiKey, String from, URI apiUrl, Duration connectTimeout,
            Duration readTimeout, int maxAttempts) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("Resend API key is required");
        if (from == null || from.isBlank()) throw new IllegalArgumentException("Resend sender is required");
        if (apiUrl == null || !"https".equals(apiUrl.getScheme()) && !"http".equals(apiUrl.getScheme())) {
            throw new IllegalArgumentException("Resend API URL must be HTTP(S)");
        }
        if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()
                || readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalArgumentException("Resend timeouts must be positive");
        }
        if (maxAttempts < 1) throw new IllegalArgumentException("Resend max attempts must be positive");
        this.apiKey = apiKey;
        this.from = from;
        this.apiUrl = apiUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxAttempts = maxAttempts;
    }

    public String apiKey() { return apiKey; }
    public String from() { return from; }
    public URI apiUrl() { return apiUrl; }
    public Duration connectTimeout() { return connectTimeout; }
    public Duration readTimeout() { return readTimeout; }
    public int maxAttempts() { return maxAttempts; }

    @Override public String toString() {
        return "ResendProperties[from=" + from + ", apiUrl=" + apiUrl + ", connectTimeout="
                + connectTimeout + ", readTimeout=" + readTimeout + ", maxAttempts=" + maxAttempts + "]";
    }
}
