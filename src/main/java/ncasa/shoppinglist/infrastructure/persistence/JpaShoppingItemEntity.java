package ncasa.shoppinglist.infrastructure.persistence;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="shopping_items") class JpaShoppingItemEntity{
 @Id UUID id;@Column(name="list_id",nullable=false)UUID listId;@Column(nullable=false,length=160)String name;@Column(precision=12,scale=3)BigDecimal quantity;
 @Column(length=20)String unit;@Column(name="custom_unit",length=30)String customUnit;@Column(length=500)String note;
 @Column(name="responsible_member_id")UUID responsibleMemberId;@Column(name="added_by_member_id",nullable=false)UUID addedByMemberId;
 @Column(name="purchased_by_member_id")UUID purchasedByMemberId;@Column(nullable=false,length=20)String status;@Column(nullable=false)long position;
 @Column(name="created_at",nullable=false,updatable=false)Instant createdAt;@Column(name="updated_at",nullable=false)Instant updatedAt;
 @Column(name="purchased_at")Instant purchasedAt;@Version long version;protected JpaShoppingItemEntity(){}
}
