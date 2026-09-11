package ncasa.shoppinglist.application;
import java.math.BigDecimal;import java.util.UUID;import ncasa.shoppinglist.domain.ShoppingUnit;
public record ShoppingItemCommand(String name,BigDecimal quantity,ShoppingUnit unit,String customUnit,String note,UUID responsibleMemberId){}
