package ncasa.shoppinglist.infrastructure.persistence;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="shopping_lists") class JpaShoppingListEntity{
 @Id UUID id;@Column(name="household_id",nullable=false)UUID householdId;@Column(nullable=false,length=80)String name;
 @Column(name="normalized_name",length=80)String normalizedName;@Column(name="calendar_series_id")UUID calendarSeriesId;
 @Column(nullable=false,length=20)String status;@Column(name="created_by_member_id",nullable=false)UUID createdByMemberId;
 @Column(name="created_at",nullable=false,updatable=false)Instant createdAt;@Column(name="updated_at",nullable=false)Instant updatedAt;
 @Column(name="deleted_at")Instant deletedAt;@Version long version;@Column(name="content_revision",nullable=false)long contentRevision;
 protected JpaShoppingListEntity(){}
}
