package ncasa.household.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "household_members", uniqueConstraints =
        @UniqueConstraint(name = "uk_household_member_account", columnNames = {"household_id", "account_id"}))
class JpaHouseholdMemberEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private JpaHouseholdEntity household;
    @Column(name = "account_id", nullable = false) private Long accountId;
    @Column(nullable = false, length = 20) private String role;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "is_owner", nullable = false) private boolean owner;
    @Column(name = "joined_at", nullable = false, updatable = false) private Instant joinedAt;
    @Column(name = "status_changed_at", nullable = false) private Instant statusChangedAt;

    protected JpaHouseholdMemberEntity() {}
    JpaHouseholdMemberEntity(UUID id, Long accountId, String role, String status, boolean owner,
            Instant joinedAt, Instant statusChangedAt) {
        this.id = id; this.accountId = accountId; this.role = role; this.status = status; this.owner = owner;
        this.joinedAt = joinedAt; this.statusChangedAt = statusChangedAt;
    }
    void attachTo(JpaHouseholdEntity household) { this.household = household; }
    UUID id() { return id; } Long accountId() { return accountId; } String role() { return role; }
    String status() { return status; } Instant joinedAt() { return joinedAt; }
    Instant statusChangedAt() { return statusChangedAt; }
}
