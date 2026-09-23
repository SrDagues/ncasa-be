package ncasa.identityaccess.infrastructure.web;

public class PublicAuthRateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
    public PublicAuthRateLimitExceededException(long retryAfterSeconds) {
        super("Too many requests");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }
    public long retryAfterSeconds() { return retryAfterSeconds; }
}
