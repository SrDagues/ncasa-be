package ncasa.identityaccess.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EmailVerificationToken {
    private final UUID id;
    private final UserId userId;
    private final EmailVerificationTokenHash tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant consumedAt;
    private Instant invalidatedAt;

    private EmailVerificationToken(UUID id, UserId userId, EmailVerificationTokenHash tokenHash,
            Instant createdAt, Instant expiresAt, Instant consumedAt, Instant invalidatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        if (!expiresAt.isAfter(createdAt)) throw new IllegalArgumentException("Token expiry must be after creation");
        this.consumedAt = consumedAt;
        this.invalidatedAt = invalidatedAt;
    }

    public static EmailVerificationToken create(UUID id, UserId userId, EmailVerificationTokenHash tokenHash,
            Instant createdAt, Instant expiresAt) {
        return new EmailVerificationToken(id, userId, tokenHash, createdAt, expiresAt, null, null);
    }

    public static EmailVerificationToken rehydrate(UUID id, UserId userId, EmailVerificationTokenHash tokenHash,
            Instant createdAt, Instant expiresAt, Instant consumedAt, Instant invalidatedAt) {
        return new EmailVerificationToken(id, userId, tokenHash, createdAt, expiresAt, consumedAt, invalidatedAt);
    }

    public boolean hasExpired(Instant now) { return !now.isBefore(expiresAt); }
    public boolean isUsableAt(Instant now) {
        return consumedAt == null && invalidatedAt == null && !hasExpired(now);
    }
    public void consume(Instant now) {
        if (!isUsableAt(now)) throw new IllegalStateException("Email verification token is not usable");
        consumedAt = now;
    }
    public void invalidate(Instant now) {
        if (consumedAt == null && invalidatedAt == null) invalidatedAt = now;
    }

    public UUID id() { return id; }
    public UserId userId() { return userId; }
    public EmailVerificationTokenHash tokenHash() { return tokenHash; }
    public Instant createdAt() { return createdAt; }
    public Instant expiresAt() { return expiresAt; }
    public Instant consumedAt() { return consumedAt; }
    public Instant invalidatedAt() { return invalidatedAt; }
}
