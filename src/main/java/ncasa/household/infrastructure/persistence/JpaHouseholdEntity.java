package ncasa.household.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "households")
class JpaHouseholdEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "owner_member_id", nullable = false) private UUID ownerMemberId;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<JpaHouseholdMemberEntity> members = new ArrayList<>();

    protected JpaHouseholdEntity() {}

    JpaHouseholdEntity(UUID id, String name, String status, UUID ownerMemberId, Long createdBy,
            Instant createdAt, Instant updatedAt, long version) {
        this.id = id; this.name = name; this.status = status; this.ownerMemberId = ownerMemberId;
        this.createdBy = createdBy; this.createdAt = createdAt; this.updatedAt = updatedAt; this.version = version;
    }

    void addMember(JpaHouseholdMemberEntity member) { members.add(member); member.attachTo(this); }
    UUID id() { return id; }
    String name() { return name; }
    String status() { return status; }
    UUID ownerMemberId() { return ownerMemberId; }
    Long createdBy() { return createdBy; }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
    long version() { return version; }
    List<JpaHouseholdMemberEntity> members() { return List.copyOf(members); }
}
