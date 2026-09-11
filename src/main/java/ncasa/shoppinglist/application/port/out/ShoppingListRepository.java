package ncasa.shoppinglist.application.port.out;
import java.util.*;import ncasa.shoppinglist.domain.*;
public interface ShoppingListRepository{
 ShoppingList save(ShoppingList value);Optional<ShoppingList> find(UUID id,UUID householdId);List<ShoppingList> list(UUID householdId,ShoppingListStatus status);
 default Optional<ShoppingList> findForContentUpdate(UUID id,UUID householdId){return find(id,householdId);}
 boolean existsActiveName(UUID householdId,String normalizedName,UUID excludedId);Optional<ShoppingList> findActiveByCalendarSeries(UUID seriesId);void delete(ShoppingList value);
 default void touchContent(UUID listId,java.time.Instant now){}
 default boolean advanceContentRevision(UUID listId,long expectedRevision,java.time.Instant now){return true;}
}
