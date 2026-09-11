package ncasa.shoppinglist.application;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;
import ncasa.shoppinglist.application.port.out.*;
import ncasa.shoppinglist.domain.*;
import org.junit.jupiter.api.Test;

class ShoppingListUseCasesTest {
    private final UUID household=UUID.randomUUID(), actorMember=UUID.randomUUID(), otherMember=UUID.randomUUID();
    private final Clock clock=Clock.fixed(Instant.parse("2026-09-11T10:00:00Z"),ZoneOffset.UTC);
    private final Lists lists=new Lists(); private final Items items=new Items();
    private final ShoppingListHouseholdAccessPort access=(id,actor)->new ShoppingListHouseholdContext(actorMember,Set.of(actorMember,otherMember));

    @Test void rejectsDuplicateActiveListNameIgnoringCaseAndSpaces(){
        new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra semanal");
        assertThatThrownBy(()->new CreateShoppingListUseCase(lists,access,clock).execute(1L,household," COMPRA   SEMANAL "))
                .isInstanceOf(ShoppingListConflictException.class);
    }

    @Test void restoresUsingFirstAvailableNumericSuffix(){
        var lifecycle=new ShoppingListLifecycleUseCase(lists,access,clock);
        var original=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Semanal");
        lifecycle.trash(1L,household,original.id(),original.version());
        new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Semanal");
        new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Semanal_2");
        var restored=lifecycle.restore(1L,household,original.id(),original.version());
        assertThat(restored.name()).isEqualTo("Semanal_3");
    }

    @Test void rejectsInactiveResponsibleWhenAddingItem(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");
        assertThatThrownBy(()->new AddShoppingItemUseCase(lists,items,access,clock).execute(1L,household,list.id(),
                new ShoppingItemCommand("Leche",null,null,null,null,UUID.randomUUID())))
                .isInstanceOf(ShoppingListAccessDeniedException.class);
    }

    @Test void addsAtTheEndWhileHoldingTheContentLockAndReturnsTheExactRevision(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");

        var result=new AddShoppingItemUseCase(lists,items,access,clock).execute(1L,household,list.id(),
                new ShoppingItemCommand("Leche",null,null,null,null,null));

        assertThat(result.item().position()).isZero();
        assertThat(result.list().contentRevision()).isEqualTo(1);
        assertThat(lists.contentLocked).isTrue();
    }

    private static final class Lists implements ShoppingListRepository{
        final Map<UUID,ShoppingList> values=new LinkedHashMap<>();boolean contentLocked;
        public ShoppingList save(ShoppingList value){values.put(value.id(),value);return value;}
        public Optional<ShoppingList> find(UUID id,UUID household){return Optional.ofNullable(values.get(id)).filter(x->x.householdId().equals(household));}
        public Optional<ShoppingList> findForContentUpdate(UUID id,UUID household){contentLocked=true;return find(id,household);}
        public List<ShoppingList> list(UUID household,ShoppingListStatus status){return values.values().stream().filter(x->x.householdId().equals(household)&&x.status()==status).toList();}
        public boolean existsActiveName(UUID household,String normalized,UUID excluded){return values.values().stream().anyMatch(x->x.householdId().equals(household)&&x.status()==ShoppingListStatus.ACTIVE&&x.normalizedName().equals(normalized)&&!x.id().equals(excluded));}
        public Optional<ShoppingList> findActiveByCalendarSeries(UUID series){return values.values().stream().filter(x->x.status()==ShoppingListStatus.ACTIVE&&series.equals(x.calendarSeriesId())).findFirst();}
        public void touchContent(UUID listId,Instant now){values.get(listId).contentChanged(now);}
        public void delete(ShoppingList value){values.remove(value.id());}
    }
    private static final class Items implements ShoppingItemRepository{
        public ShoppingItem save(ShoppingItem value){return value;} public Optional<ShoppingItem> find(UUID id,UUID list){return Optional.empty();}
        public List<ShoppingItem> list(UUID list){return List.of();}public long nextPendingPosition(UUID list){return 0;}
        public void delete(ShoppingItem value){}public int deletePurchased(UUID list){return 0;}public int unassign(UUID household,UUID member,Instant now){return 0;}
    }
}
