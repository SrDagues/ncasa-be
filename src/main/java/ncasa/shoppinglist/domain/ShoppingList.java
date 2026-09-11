package ncasa.shoppinglist.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ShoppingList {
    private final UUID id;
    private final UUID householdId;
    private ShoppingListName name;
    private UUID calendarSeriesId;
    private ShoppingListStatus status;
    private final UUID createdByMemberId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private long version;
    private long contentRevision;

    private ShoppingList(UUID id, UUID householdId, ShoppingListName name, UUID calendarSeriesId,
            ShoppingListStatus status, UUID createdByMemberId, Instant createdAt, Instant updatedAt,
            Instant deletedAt, long version, long contentRevision) {
        this.id=Objects.requireNonNull(id);this.householdId=Objects.requireNonNull(householdId);this.name=Objects.requireNonNull(name);
        this.calendarSeriesId=calendarSeriesId;this.status=Objects.requireNonNull(status);this.createdByMemberId=Objects.requireNonNull(createdByMemberId);
        this.createdAt=Objects.requireNonNull(createdAt);this.updatedAt=Objects.requireNonNull(updatedAt);this.deletedAt=deletedAt;
        this.version=version;this.contentRevision=contentRevision;
    }
    public static ShoppingList create(UUID id,UUID householdId,String name,UUID actor,Instant now){
        return new ShoppingList(id,householdId,ShoppingListName.of(name),null,ShoppingListStatus.ACTIVE,actor,now,now,null,0,0);
    }
    public static ShoppingList rehydrate(UUID id,UUID householdId,String name,String normalizedName,UUID calendarSeriesId,
            ShoppingListStatus status,UUID createdByMemberId,Instant createdAt,Instant updatedAt,Instant deletedAt,long version,long contentRevision){
        return new ShoppingList(id,householdId,new ShoppingListName(name,normalizedName),calendarSeriesId,status,createdByMemberId,
                createdAt,updatedAt,deletedAt,version,contentRevision);
    }
    public void rename(String value,Instant now){requireActive();name=ShoppingListName.of(value);updatedAt=now;}
    public void linkCalendar(UUID seriesId,Instant now){requireActive();calendarSeriesId=Objects.requireNonNull(seriesId);updatedAt=now;}
    public void unlinkCalendar(Instant now){if(calendarSeriesId!=null){calendarSeriesId=null;updatedAt=now;}}
    public void trash(Instant now){requireActive();calendarSeriesId=null;status=ShoppingListStatus.TRASHED;deletedAt=now;updatedAt=now;}
    public void restore(String restoredName,Instant now){if(status!=ShoppingListStatus.TRASHED)throw new ShoppingListStateException("Only trashed lists can be restored");name=ShoppingListName.of(restoredName);status=ShoppingListStatus.ACTIVE;deletedAt=null;calendarSeriesId=null;updatedAt=now;}
    public void contentChanged(Instant now){requireActive();contentRevision++;updatedAt=now;}
    public void requireActive(){if(status!=ShoppingListStatus.ACTIVE)throw new ShoppingListStateException("Shopping list is in trash");}
    public UUID id(){return id;} public UUID householdId(){return householdId;} public String name(){return name.value();}
    public String normalizedName(){return name.normalized();} public UUID calendarSeriesId(){return calendarSeriesId;}
    public ShoppingListStatus status(){return status;} public UUID createdByMemberId(){return createdByMemberId;}
    public Instant createdAt(){return createdAt;} public Instant updatedAt(){return updatedAt;} public Instant deletedAt(){return deletedAt;}
    public long version(){return version;} public long contentRevision(){return contentRevision;}
}
