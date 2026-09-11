package ncasa.shoppinglist.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ShoppingItem {
    private final UUID id; private final UUID listId; private String name; private BigDecimal quantity;
    private ShoppingUnit unit; private String customUnit; private String note; private UUID responsibleMemberId;
    private final UUID addedByMemberId; private UUID purchasedByMemberId; private ShoppingItemStatus status;
    private long position; private final Instant createdAt; private Instant updatedAt; private Instant purchasedAt; private long version;

    private ShoppingItem(UUID id,UUID listId,String name,BigDecimal quantity,ShoppingUnit unit,String customUnit,String note,
            UUID responsibleMemberId,UUID addedByMemberId,UUID purchasedByMemberId,ShoppingItemStatus status,long position,
            Instant createdAt,Instant updatedAt,Instant purchasedAt,long version){
        this.id=Objects.requireNonNull(id);this.listId=Objects.requireNonNull(listId);this.addedByMemberId=Objects.requireNonNull(addedByMemberId);
        this.createdAt=Objects.requireNonNull(createdAt);this.status=Objects.requireNonNull(status);this.position=position;this.version=version;
        replace(name,quantity,unit,customUnit,note,responsibleMemberId);this.purchasedByMemberId=purchasedByMemberId;
        this.updatedAt=Objects.requireNonNull(updatedAt);this.purchasedAt=purchasedAt;validatePurchaseState();
    }
    public static ShoppingItem create(UUID id,UUID listId,String name,BigDecimal quantity,ShoppingUnit unit,String customUnit,
            String note,UUID responsible,UUID actor,long position,Instant now){return new ShoppingItem(id,listId,name,quantity,unit,
                    customUnit,note,responsible,actor,null,ShoppingItemStatus.PENDING,position,now,now,null,0);}
    public static ShoppingItem rehydrate(UUID id,UUID listId,String name,BigDecimal quantity,ShoppingUnit unit,String customUnit,
            String note,UUID responsible,UUID addedBy,UUID purchasedBy,ShoppingItemStatus status,long position,
            Instant createdAt,Instant updatedAt,Instant purchasedAt,long version){return new ShoppingItem(id,listId,name,quantity,unit,
                    customUnit,note,responsible,addedBy,purchasedBy,status,position,createdAt,updatedAt,purchasedAt,version);}
    public void edit(String name,BigDecimal quantity,ShoppingUnit unit,String customUnit,String note,UUID responsible,Instant now){
        replace(name,quantity,unit,customUnit,note,responsible);updatedAt=now;
    }
    public void purchase(UUID actor,Instant now){if(status==ShoppingItemStatus.PURCHASED)return;status=ShoppingItemStatus.PURCHASED;purchasedByMemberId=Objects.requireNonNull(actor);purchasedAt=now;updatedAt=now;}
    public void reopen(long endPosition,Instant now){if(status==ShoppingItemStatus.PENDING)return;status=ShoppingItemStatus.PENDING;purchasedByMemberId=null;purchasedAt=null;position=endPosition;updatedAt=now;}
    public void moveTo(long value,Instant now){if(status!=ShoppingItemStatus.PENDING)throw new ShoppingListStateException("Only pending items can be reordered");if(value<0)throw new ShoppingListRuleViolationException("Position cannot be negative");position=value;updatedAt=now;}
    public void unassign(Instant now){if(responsibleMemberId!=null){responsibleMemberId=null;updatedAt=now;}}
    private void replace(String rawName,BigDecimal quantity,ShoppingUnit unit,String customUnit,String note,UUID responsible){
        name=text(rawName,160,true,"Product name");
        if(quantity!=null&&(quantity.signum()<=0||quantity.scale()>3))throw new ShoppingListRuleViolationException("Quantity must be positive with at most 3 decimals");
        if(unit==ShoppingUnit.OTHER)customUnit=text(customUnit,30,true,"Custom unit");
        else if(customUnit!=null&&!customUnit.isBlank())throw new ShoppingListRuleViolationException("Custom unit is only allowed for OTHER");
        this.quantity=quantity;this.unit=unit;this.customUnit=unit==ShoppingUnit.OTHER?customUnit:null;
        this.note=text(note,500,false,"Note");this.responsibleMemberId=responsible;
    }
    private void validatePurchaseState(){boolean purchased=status==ShoppingItemStatus.PURCHASED;if(purchased!=(purchasedAt!=null&&purchasedByMemberId!=null))throw new ShoppingListRuleViolationException("Purchased audit is inconsistent");}
    private static String text(String value,int max,boolean required,String label){if(value==null){if(required)throw new ShoppingListRuleViolationException(label+" is required");return null;}String normalized=value.trim().replaceAll("\\s+"," ");if(required&&normalized.isEmpty())throw new ShoppingListRuleViolationException(label+" is required");if(normalized.isEmpty())return null;if(normalized.length()>max)throw new ShoppingListRuleViolationException(label+" is too long");return normalized;}
    public UUID id(){return id;}public UUID listId(){return listId;}public String name(){return name;}public BigDecimal quantity(){return quantity;}
    public ShoppingUnit unit(){return unit;}public String customUnit(){return customUnit;}public String note(){return note;}public UUID responsibleMemberId(){return responsibleMemberId;}
    public UUID addedByMemberId(){return addedByMemberId;}public UUID purchasedByMemberId(){return purchasedByMemberId;}public ShoppingItemStatus status(){return status;}
    public long position(){return position;}public Instant createdAt(){return createdAt;}public Instant updatedAt(){return updatedAt;}public Instant purchasedAt(){return purchasedAt;}public long version(){return version;}
}
