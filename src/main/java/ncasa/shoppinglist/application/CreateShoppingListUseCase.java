package ncasa.shoppinglist.application;
import java.time.Clock;import java.util.UUID;import ncasa.shoppinglist.application.port.out.*;import ncasa.shoppinglist.domain.*;
public final class CreateShoppingListUseCase{
 private final ShoppingListRepository lists;private final ShoppingListHouseholdAccessPort access;private final Clock clock;
 public CreateShoppingListUseCase(ShoppingListRepository l,ShoppingListHouseholdAccessPort a,Clock c){lists=l;access=a;clock=c;}
 public ShoppingList execute(Long actor,UUID householdId,String name){var ctx=access.getContext(householdId,actor);var n=ShoppingListName.of(name);if(lists.existsActiveName(householdId,n.normalized(),null))throw new ShoppingListConflictException("Shopping list name already exists");return lists.save(ShoppingList.create(UUID.randomUUID(),householdId,n.value(),ctx.actorMemberId(),clock.instant()));}
}
