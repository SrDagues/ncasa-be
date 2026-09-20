package ncasa.shoppinglist.application;

import ncasa.shoppinglist.domain.ShoppingItem;
import ncasa.shoppinglist.domain.ShoppingList;

public record AddShoppingItemResult(ShoppingItem item, ShoppingList list) {}
