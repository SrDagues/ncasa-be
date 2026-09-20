package ncasa.shoppinglist.application;
import java.util.*;import ncasa.shoppinglist.application.port.out.*;import ncasa.shoppinglist.domain.*;
public final class ListShoppingListsUseCase{private final ShoppingListRepository lists;private final ShoppingListHouseholdAccessPort access;public ListShoppingListsUseCase(ShoppingListRepository l,ShoppingListHouseholdAccessPort a){lists=l;access=a;}public List<ShoppingList> execute(Long actor,UUID household,boolean trashed){access.getContext(household,actor);return lists.list(household,trashed?ShoppingListStatus.TRASHED:ShoppingListStatus.ACTIVE);}}
