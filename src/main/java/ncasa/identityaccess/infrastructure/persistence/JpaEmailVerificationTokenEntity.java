package ncasa.identityaccess.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
class JpaEmailVerificationTokenEntity {
    @Id UUID id;
    @Column(name = "user_id", nullable = false) Long userId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) String tokenHash;
    @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
    @Column(name = "expires_at", nullable = false) Instant expiresAt;
    @Column(name = "consumed_at") Instant consumedAt;
    @Column(name = "invalidated_at") Instant invalidatedAt;

    protected JpaEmailVerificationTokenEntity() {}

    JpaEmailVerificationTokenEntity(UUID id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt,
            Instant consumedAt, Instant invalidatedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.invalidatedAt = invalidatedAt;
    }

    void update(Instant consumedAt, Instant invalidatedAt) {
        this.consumedAt = consumedAt;
        this.invalidatedAt = invalidatedAt;
    }
}
