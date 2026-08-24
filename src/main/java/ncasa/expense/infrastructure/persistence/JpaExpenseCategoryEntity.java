package ncasa.expense.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expense_categories")
class JpaExpenseCategoryEntity {
    @Id private UUID id;
    @Column(name = "household_id", nullable = false) private UUID householdId;
    @Column(name = "created_by_member_id", nullable = false) private UUID createdByMemberId;
    @Column(nullable = false, length = 80) private String name;
    @Column(name = "name_key", nullable = false, length = 80) private String nameKey;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "archived_at") private Instant archivedAt;
    @Version private long version;

    protected JpaExpenseCategoryEntity() {}
    JpaExpenseCategoryEntity(UUID id, UUID householdId, UUID createdByMemberId, String name, String status,
            Instant createdAt, Instant updatedAt, Instant archivedAt, long version) {
        this.id=id; this.householdId=householdId; this.createdByMemberId=createdByMemberId; this.name=name;
        this.nameKey=name.toLowerCase(java.util.Locale.ROOT);
        this.status=status; this.createdAt=createdAt; this.updatedAt=updatedAt; this.archivedAt=archivedAt; this.version=version;
    }
    UUID id(){return id;} UUID householdId(){return householdId;} UUID createdByMemberId(){return createdByMemberId;}
    String name(){return name;} String status(){return status;} Instant createdAt(){return createdAt;}
    Instant updatedAt(){return updatedAt;} Instant archivedAt(){return archivedAt;} long version(){return version;}
}
