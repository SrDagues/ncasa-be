package ncasa.shoppinglist.infrastructure.config;

import java.time.Clock;
import ncasa.calendar.application.CalendarSeriesReferenceUseCase;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import ncasa.shoppinglist.application.*;
import ncasa.shoppinglist.application.port.out.*;
import ncasa.shoppinglist.infrastructure.calendar.CalendarEntryReferenceAdapter;
import ncasa.shoppinglist.infrastructure.household.HouseholdShoppingListAccessAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingListConfiguration {
    @Bean ShoppingListHouseholdAccessPort shoppingListHouseholdAccess(GetHouseholdMembershipContextUseCase memberships) {
        return new HouseholdShoppingListAccessAdapter(memberships);
    }

    @Bean CalendarEntryReferencePort shoppingListCalendarReference(CalendarSeriesReferenceUseCase calendar) {
        return new CalendarEntryReferenceAdapter(calendar);
    }

    @Bean CreateShoppingListUseCase createShoppingList(ShoppingListRepository lists, ShoppingListHouseholdAccessPort access, Clock clock) {
        return new CreateShoppingListUseCase(lists, access, clock);
    }

    @Bean ListShoppingListsUseCase listShoppingLists(ShoppingListRepository lists, ShoppingListHouseholdAccessPort access) {
        return new ListShoppingListsUseCase(lists, access);
    }

    @Bean GetShoppingListUseCase getShoppingList(ShoppingListRepository lists, ShoppingItemRepository items, ShoppingListHouseholdAccessPort access) {
        return new GetShoppingListUseCase(lists, items, access);
    }

    @Bean UpdateShoppingListUseCase updateShoppingList(ShoppingListRepository lists, ShoppingListHouseholdAccessPort access,
            CalendarEntryReferencePort calendar, Clock clock) {
        return new UpdateShoppingListUseCase(lists, access, calendar, clock);
    }

    @Bean ShoppingListLifecycleUseCase shoppingListLifecycle(ShoppingListRepository lists, ShoppingListHouseholdAccessPort access, Clock clock) {
        return new ShoppingListLifecycleUseCase(lists, access, clock);
    }

    @Bean AddShoppingItemUseCase addShoppingItem(ShoppingListRepository lists, ShoppingItemRepository items,
            ShoppingListHouseholdAccessPort access, Clock clock) {
        return new AddShoppingItemUseCase(lists, items, access, clock);
    }

    @Bean UpdateShoppingItemUseCase updateShoppingItem(ShoppingListRepository lists, ShoppingItemRepository items,
            ShoppingListHouseholdAccessPort access, Clock clock) {
        return new UpdateShoppingItemUseCase(lists, items, access, clock);
    }

    @Bean ShoppingItemLifecycleUseCase shoppingItemLifecycle(ShoppingListRepository lists, ShoppingItemRepository items,
            ShoppingListHouseholdAccessPort access, Clock clock) {
        return new ShoppingItemLifecycleUseCase(lists, items, access, clock);
    }

    @Bean ReorderShoppingItemsUseCase reorderShoppingItems(ShoppingListRepository lists, ShoppingItemRepository items,
            ShoppingListHouseholdAccessPort access, Clock clock) {
        return new ReorderShoppingItemsUseCase(lists, items, access, clock);
    }

    @Bean UnlinkShoppingListFromCalendarUseCase unlinkShoppingListFromCalendar(ShoppingListRepository lists, Clock clock) {
        return new UnlinkShoppingListFromCalendarUseCase(lists, clock);
    }

    @Bean UnassignShoppingItemsUseCase unassignShoppingItems(ShoppingItemRepository items, ShoppingListRepository lists, Clock clock) {
        return new UnassignShoppingItemsUseCase(items, lists, clock);
    }
}
