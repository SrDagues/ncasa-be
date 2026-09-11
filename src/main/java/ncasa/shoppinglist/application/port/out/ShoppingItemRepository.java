package ncasa.shoppinglist.application.port.out;
import java.time.Instant;import java.util.*;import ncasa.shoppinglist.domain.ShoppingItem;
public interface ShoppingItemRepository{
 ShoppingItem save(ShoppingItem value);Optional<ShoppingItem> find(UUID id,UUID listId);List<ShoppingItem> list(UUID listId);long nextPendingPosition(UUID listId);
 void delete(ShoppingItem value);int deletePurchased(UUID listId);int unassign(UUID householdId,UUID memberId,Instant now);
 default List<UUID> assignedListIds(UUID householdId,UUID memberId){return List.of();}
}
