package ncasa.identityaccess.domain;

import java.time.Instant;

public final class AuthSession {
    private final Long id;
    private final UserId userId;
    private final RefreshTokenHash tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private boolean revoked;
    private Long replacedById;

    private AuthSession(Long id, UserId userId, RefreshTokenHash tokenHash, Instant expiresAt,
            Instant createdAt, boolean revoked, Long replacedById) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.revoked = revoked;
        this.replacedById = replacedById;
    }

    public static AuthSession create(UserId userId, RefreshTokenHash tokenHash, Instant expiresAt, Instant now) {
        return new AuthSession(null, userId, tokenHash, expiresAt, now, false, null);
    }

    public static AuthSession rehydrate(Long id, UserId userId, RefreshTokenHash tokenHash,
            Instant expiresAt, Instant createdAt, boolean revoked, Long replacedById) {
        return new AuthSession(id, userId, tokenHash, expiresAt, createdAt, revoked, replacedById);
    }

    public boolean isUsableAt(Instant now) {
        return !revoked && expiresAt.isAfter(now);
    }

    public void revoke(Long replacementId) {
        this.revoked = true;
        this.replacedById = replacementId;
    }

    public void revoke() { this.revoked = true; }

    public Long id() { return id; }
    public UserId userId() { return userId; }
    public RefreshTokenHash tokenHash() { return tokenHash; }
    public Instant expiresAt() { return expiresAt; }
    public Instant createdAt() { return createdAt; }
    public boolean revoked() { return revoked; }
    public Long replacedById() { return replacedById; }
}
