package ncasa.identityaccess.infrastructure.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ncasa.identityaccess.infrastructure.config.EmailVerificationProperties;
import org.springframework.stereotype.Component;

@Component
public class PublicAuthRateLimiter {
    private static final int MAX_BUCKETS = 10_000;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final EmailVerificationProperties properties;
    private final Clock clock;

    public PublicAuthRateLimiter(EmailVerificationProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void checkConfirmation(String remoteAddress) {
        consume("confirm", remoteAddress, properties.confirmRatePerMinute());
    }

    public void checkResend(String remoteAddress) {
        consume("resend", remoteAddress, properties.resendRatePerMinute());
    }

    private void consume(String scope, String remoteAddress, int capacity) {
        Instant now = clock.instant();
        var bucket = bucket(scope + ":" + remoteAddress, capacity, now);
        long retryAfter = bucket.consume(capacity, now);
        if (retryAfter > 0) throw new PublicAuthRateLimitExceededException(retryAfter);
    }

    private Bucket bucket(String key, int capacity, Instant now) {
        var existing = buckets.get(key);
        if (existing != null) return existing;
        synchronized (buckets) {
            existing = buckets.get(key);
            if (existing != null) return existing;
            if (buckets.size() >= MAX_BUCKETS) discardStale(now.minus(Duration.ofMinutes(10)));
            if (buckets.size() >= MAX_BUCKETS) throw new PublicAuthRateLimitExceededException(60);
            var created = new Bucket(capacity, now);
            buckets.put(key, created);
            return created;
        }
    }

    private void discardStale(Instant cutoff) {
        Iterator<Map.Entry<String, Bucket>> iterator = buckets.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().lastAccess().isBefore(cutoff)) iterator.remove();
        }
    }

    private static final class Bucket {
        private double tokens;
        private Instant lastRefill;
        private Instant lastAccess;

        private Bucket(int capacity, Instant now) {
            tokens = capacity;
            lastRefill = now;
            lastAccess = now;
        }

        synchronized long consume(int capacity, Instant now) {
            double elapsedSeconds = Math.max(0, Duration.between(lastRefill, now).toMillis() / 1000.0);
            tokens = Math.min(capacity, tokens + elapsedSeconds * capacity / 60.0);
            lastRefill = now;
            lastAccess = now;
            if (tokens >= 1) {
                tokens--;
                return 0;
            }
            return Math.max(1, (long) Math.ceil((1 - tokens) * 60.0 / capacity));
        }

        synchronized Instant lastAccess() { return lastAccess; }
    }
}
