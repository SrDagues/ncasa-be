package ncasa.shoppinglist.application;
import java.util.List;import ncasa.shoppinglist.domain.*;
public record ShoppingListDetail(ShoppingList list,List<ShoppingItem> pending,List<ShoppingItem> purchased){public ShoppingListDetail{pending=List.copyOf(pending);purchased=List.copyOf(purchased);}}
