package ncasa.shoppinglist.application;

import java.time.Clock;
import java.util.Comparator;
import java.util.UUID;
import ncasa.shoppinglist.application.port.out.ShoppingItemRepository;
import ncasa.shoppinglist.application.port.out.ShoppingListHouseholdAccessPort;
import ncasa.shoppinglist.application.port.out.ShoppingListRepository;
import ncasa.shoppinglist.domain.ShoppingItem;
import ncasa.shoppinglist.domain.ShoppingItemStatus;

public final class ReusePurchasedShoppingItemsUseCase {
    private final ShoppingListRepository lists;
    private final ShoppingItemRepository items;
    private final ShoppingListHouseholdAccessPort access;
    private final Clock clock;

    public ReusePurchasedShoppingItemsUseCase(ShoppingListRepository lists, ShoppingItemRepository items,
            ShoppingListHouseholdAccessPort access, Clock clock) {
        this.lists = lists;
        this.items = items;
        this.access = access;
        this.clock = clock;
    }

    public ReusePurchasedShoppingItemsResult execute(Long actor, UUID householdId, UUID listId,
            long expectedContentRevision) {
        access.getContext(householdId, actor);
        var list = GetShoppingListUseCase.load(lists, listId, householdId);
        list.requireActive();
        if (list.contentRevision() != expectedContentRevision) {
            throw new ShoppingListConflictException("Shopping list content changed concurrently");
        }

        var all = items.list(listId);
        var purchased = all.stream()
                .filter(item -> item.status() == ShoppingItemStatus.PURCHASED)
                .sorted(Comparator.comparing(ShoppingItem::purchasedAt).thenComparing(ShoppingItem::id))
                .toList();
        if (purchased.isEmpty()) {
            return new ReusePurchasedShoppingItemsResult(GetShoppingListUseCase.detail(list, all), 0);
        }

        var now = clock.instant();
        if (!lists.advanceContentRevision(listId, expectedContentRevision, now)) {
            throw new ShoppingListConflictException("Shopping list content changed concurrently");
        }
        long position = items.nextPendingPosition(listId);
        for (var item : purchased) {
            item.reopen(position++, now);
            items.save(item);
        }

        var updated = GetShoppingListUseCase.load(lists, listId, householdId);
        return new ReusePurchasedShoppingItemsResult(
                GetShoppingListUseCase.detail(updated, items.list(listId)), purchased.size());
    }
}
