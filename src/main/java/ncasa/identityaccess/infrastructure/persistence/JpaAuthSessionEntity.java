package ncasa.identityaccess.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
class JpaAuthSessionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean revoked;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    protected JpaAuthSessionEntity() {}

    JpaAuthSessionEntity(Long id, String tokenHash, Long userId, Instant expiresAt,
            boolean revoked, Instant createdAt, Long replacedByTokenId) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.replacedByTokenId = replacedByTokenId;
    }

    Long id() { return id; }
    String tokenHash() { return tokenHash; }
    Long userId() { return userId; }
    Instant expiresAt() { return expiresAt; }
    boolean revoked() { return revoked; }
    Instant createdAt() { return createdAt; }
    Long replacedByTokenId() { return replacedByTokenId; }
}
