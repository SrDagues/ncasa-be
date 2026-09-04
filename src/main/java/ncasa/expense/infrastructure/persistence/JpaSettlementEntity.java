package ncasa.expense.infrastructure.persistence;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.*;import java.util.UUID;
@Entity @Table(name="settlements")
class JpaSettlementEntity {
 @Id UUID id; @Column(name="household_id",nullable=false) UUID householdId; @Column(name="created_by_member_id",nullable=false) UUID createdByMemberId;
 @Column(name="from_member_id",nullable=false) UUID fromMemberId; @Column(name="to_member_id",nullable=false) UUID toMemberId;
 @Column(nullable=false,precision=19,scale=4) BigDecimal amount; @Column(nullable=false,length=3) String currency;
 @Column(name="settlement_date",nullable=false) LocalDate settlementDate; @Column(length=240) String note; @Column(nullable=false,length=20) String status;
 @Column(name="void_reason",length=500) String voidReason; @Column(name="idempotency_key",nullable=false,updatable=false) UUID idempotencyKey;
 @Column(name="created_at",nullable=false,updatable=false) Instant createdAt; @Column(name="updated_at",nullable=false) Instant updatedAt; @Column(name="voided_at") Instant voidedAt; @Version long version;
 protected JpaSettlementEntity(){}
 JpaSettlementEntity(UUID id,UUID household,UUID creator,UUID from,UUID to,BigDecimal amount,String currency,LocalDate date,String note,String status,String reason,UUID key,Instant created,Instant updated,Instant voided,long version){this.id=id;householdId=household;createdByMemberId=creator;fromMemberId=from;toMemberId=to;this.amount=amount;this.currency=currency;settlementDate=date;this.note=note;this.status=status;voidReason=reason;idempotencyKey=key;createdAt=created;updatedAt=updated;voidedAt=voided;this.version=version;}
}
