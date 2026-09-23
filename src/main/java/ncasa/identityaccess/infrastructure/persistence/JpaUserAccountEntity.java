package ncasa.identityaccess.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
class JpaUserAccountEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    private boolean enabled;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 32)
    private Set<String> roles = new HashSet<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    protected JpaUserAccountEntity() {}

    JpaUserAccountEntity(String email, boolean enabled, Set<String> roles, Instant createdAt, Instant updatedAt,
            Instant emailVerifiedAt) {
        this.email = email;
        this.enabled = enabled;
        this.roles.addAll(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    void update(boolean enabled, Instant updatedAt, Instant emailVerifiedAt) {
        this.enabled = enabled;
        this.updatedAt = updatedAt;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    Long id() { return id; }
    String email() { return email; }
    boolean enabled() { return enabled; }
    Set<String> roles() { return Set.copyOf(roles); }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
    Instant emailVerifiedAt() { return emailVerifiedAt; }
}
