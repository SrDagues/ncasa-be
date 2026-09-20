package ncasa.shoppinglist.infrastructure.household;
import java.util.UUID;import ncasa.household.application.port.out.InactiveMemberCleanupPort;import ncasa.shoppinglist.application.UnassignShoppingItemsUseCase;import org.springframework.stereotype.Component;
@Component public final class ShoppingListInactiveMemberAdapter implements InactiveMemberCleanupPort{private final UnassignShoppingItemsUseCase unassign;public ShoppingListInactiveMemberAdapter(UnassignShoppingItemsUseCase u){unassign=u;}public void memberBecameInactive(UUID household,UUID member){unassign.execute(household,member);}}
