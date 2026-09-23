package ncasa.identityaccess.infrastructure.email;

final class ResendDeliveryException extends RuntimeException {
    private final boolean retryable;
    private final boolean quotaExceeded;
    private final long retryAfterSeconds;

    ResendDeliveryException(boolean retryable, boolean quotaExceeded, long retryAfterSeconds) {
        super("Transactional email delivery failed");
        this.retryable = retryable;
        this.quotaExceeded = quotaExceeded;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    boolean retryable() { return retryable; }
    boolean quotaExceeded() { return quotaExceeded; }
    long retryAfterSeconds() { return retryAfterSeconds; }
}
