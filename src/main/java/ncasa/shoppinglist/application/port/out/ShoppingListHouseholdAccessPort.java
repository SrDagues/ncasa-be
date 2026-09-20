package ncasa.shoppinglist.application.port.out;
import java.util.UUID;import ncasa.shoppinglist.application.ShoppingListHouseholdContext;
public interface ShoppingListHouseholdAccessPort{ShoppingListHouseholdContext getContext(UUID householdId,Long actorAccountId);}
