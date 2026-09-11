package ncasa.shoppinglist.domain;

import java.util.Locale;

public record ShoppingListName(String value, String normalized) {
    public ShoppingListName {
        if (value == null || normalized == null) throw new ShoppingListRuleViolationException("List name is required");
        if (value.isBlank() || value.length() > 80) throw new ShoppingListRuleViolationException("List name must contain 1 to 80 characters");
    }
    public static ShoppingListName of(String raw) {
        if (raw == null) throw new ShoppingListRuleViolationException("List name is required");
        String display = raw.trim().replaceAll("\\s+", " ");
        return new ShoppingListName(display, display.toLowerCase(Locale.ROOT));
    }
}
