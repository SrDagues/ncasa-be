package ncasa.identityaccess.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "auth_identities", uniqueConstraints =
        @UniqueConstraint(name = "uk_auth_identity_provider_user", columnNames = {"provider", "provider_user_id"}))
class JpaAuthIdentityEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private JpaUserAccountEntity user;
    @Column(nullable = false, length = 32)
    private String provider;
    @Column(name = "provider_user_id", nullable = false, length = 320)
    private String providerUserId;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected JpaAuthIdentityEntity() {}

    JpaAuthIdentityEntity(JpaUserAccountEntity user, String provider, String providerUserId,
            String passwordHash, Instant createdAt) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    JpaUserAccountEntity user() { return user; }
    String passwordHash() { return passwordHash; }
}
