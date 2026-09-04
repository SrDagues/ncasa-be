package ncasa.expense.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class Settlement {
    private final SettlementId id; private final HouseholdRef householdId;
    private final MemberRef createdByMemberId; private final MemberRef fromMemberId; private final MemberRef toMemberId;
    private final Money money; private final LocalDate settlementDate; private final String note;
    private SettlementStatus status; private String voidReason; private final Instant createdAt;
    private Instant updatedAt; private Instant voidedAt; private final long version;

    private Settlement(SettlementId id, HouseholdRef householdId, MemberRef creator, MemberRef from,
            MemberRef to, Money money, LocalDate date, String note, SettlementStatus status,
            String voidReason, Instant createdAt, Instant updatedAt, Instant voidedAt, long version) {
        this.id=Objects.requireNonNull(id); this.householdId=Objects.requireNonNull(householdId);
        this.createdByMemberId=Objects.requireNonNull(creator); this.fromMemberId=Objects.requireNonNull(from);
        this.toMemberId=Objects.requireNonNull(to); this.money=Objects.requireNonNull(money);
        this.settlementDate=Objects.requireNonNull(date);
        if (!money.isPositive()) throw new ExpenseRuleViolationException("Settlement amount must be positive");
        if (from.equals(to)) throw new ExpenseRuleViolationException("Settlement members must differ");
        this.note=normalize(note,240,"Settlement note"); this.status=Objects.requireNonNull(status);
        this.voidReason=normalize(voidReason,500,"Settlement void reason"); this.createdAt=Objects.requireNonNull(createdAt);
        this.updatedAt=Objects.requireNonNull(updatedAt); this.voidedAt=voidedAt; this.version=version;
        if(version<0) throw new IllegalArgumentException("Version cannot be negative"); validateLifecycle();
    }
    public static Settlement confirmed(SettlementId id, HouseholdRef household, MemberRef creator,
            MemberRef from, MemberRef to, Money money, LocalDate date, String note, Instant now) {
        return new Settlement(id,household,creator,from,to,money,date,note,SettlementStatus.CONFIRMED,null,now,now,null,0);
    }
    public static Settlement rehydrate(SettlementId id, HouseholdRef household, MemberRef creator,
            MemberRef from, MemberRef to, Money money, LocalDate date, String note, SettlementStatus status,
            String reason, Instant createdAt, Instant updatedAt, Instant voidedAt, long version) {
        return new Settlement(id,household,creator,from,to,money,date,note,status,reason,createdAt,updatedAt,voidedAt,version);
    }
    public void voidSettlement(String reason, Instant now) {
        if(status!=SettlementStatus.CONFIRMED) throw new SettlementStateException("Only a confirmed settlement can be voided");
        voidReason=normalize(reason,500,"Settlement void reason");
        if(voidReason==null) throw new ExpenseRuleViolationException("Settlement void reason is required");
        status=SettlementStatus.VOIDED; voidedAt=Objects.requireNonNull(now); updatedAt=now;
    }
    private void validateLifecycle(){
        if(status==SettlementStatus.VOIDED&&(voidReason==null||voidedAt==null)) throw new ExpenseRuleViolationException("A voided settlement requires reason and timestamp");
        if(status==SettlementStatus.CONFIRMED&&(voidReason!=null||voidedAt!=null)) throw new ExpenseRuleViolationException("A confirmed settlement cannot have void details");
    }
    private static String normalize(String value,int max,String label){if(value==null||value.isBlank())return null;var n=value.trim();if(n.length()>max)throw new ExpenseRuleViolationException(label+" exceeds "+max+" characters");return n;}
    public SettlementId id(){return id;} public HouseholdRef householdId(){return householdId;} public MemberRef createdByMemberId(){return createdByMemberId;}
    public MemberRef fromMemberId(){return fromMemberId;} public MemberRef toMemberId(){return toMemberId;} public Money money(){return money;}
    public LocalDate settlementDate(){return settlementDate;} public String note(){return note;} public SettlementStatus status(){return status;}
    public String voidReason(){return voidReason;} public Instant createdAt(){return createdAt;} public Instant updatedAt(){return updatedAt;}
    public Instant voidedAt(){return voidedAt;} public long version(){return version;}
}
