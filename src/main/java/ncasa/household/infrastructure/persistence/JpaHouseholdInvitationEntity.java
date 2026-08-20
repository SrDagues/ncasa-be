package ncasa.household.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "household_invitations")
class JpaHouseholdInvitationEntity {
    @Id private UUID id;
    @Column(name = "household_id", nullable = false) private UUID householdId;
    @Column(nullable = false, length = 320) private String email;
    @Column(name = "invited_role", nullable = false, length = 20) private String invitedRole;
    @Column(name = "invited_by", nullable = false) private UUID invitedBy;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "status_changed_at", nullable = false) private Instant statusChangedAt;

    protected JpaHouseholdInvitationEntity() {}
    JpaHouseholdInvitationEntity(UUID id, UUID householdId, String email, String invitedRole, UUID invitedBy,
            String tokenHash, String status, Instant createdAt, Instant expiresAt, Instant statusChangedAt) {
        this.id = id; this.householdId = householdId; this.email = email; this.invitedRole = invitedRole;
        this.invitedBy = invitedBy; this.tokenHash = tokenHash; this.status = status; this.createdAt = createdAt;
        this.expiresAt = expiresAt; this.statusChangedAt = statusChangedAt;
    }
    UUID id() { return id; } UUID householdId() { return householdId; } String email() { return email; }
    String invitedRole() { return invitedRole; } UUID invitedBy() { return invitedBy; }
    String tokenHash() { return tokenHash; } String status() { return status; } Instant createdAt() { return createdAt; }
    Instant expiresAt() { return expiresAt; } Instant statusChangedAt() { return statusChangedAt; }
}
