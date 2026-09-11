package ncasa.shoppinglist.application;
import java.util.*;
public record ShoppingListHouseholdContext(UUID actorMemberId,Set<UUID> activeMemberIds){
 public ShoppingListHouseholdContext{activeMemberIds=Set.copyOf(activeMemberIds);}
 public void requireActive(UUID member){if(member!=null&&!activeMemberIds.contains(member))throw new ShoppingListAccessDeniedException("Responsible member is not active in household");}
}
