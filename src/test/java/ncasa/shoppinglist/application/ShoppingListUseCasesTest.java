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

    @Test void reusesAllPurchasedItemsAfterExistingPendingAndPreservesTheirDetails(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");
        var pending=ShoppingItem.create(UUID.randomUUID(),list.id(),"Pan",null,null,null,"Integral",otherMember,actorMember,0,clock.instant());
        var milk=ShoppingItem.create(UUID.randomUUID(),list.id(),"Leche",null,ShoppingUnit.LITER,null,"Entera",otherMember,actorMember,4,clock.instant());
        var coffee=ShoppingItem.create(UUID.randomUUID(),list.id(),"Café",null,ShoppingUnit.GRAM,null,null,null,actorMember,2,clock.instant());
        milk.purchase(actorMember,clock.instant().plusSeconds(2));coffee.purchase(otherMember,clock.instant().plusSeconds(1));
        items.save(pending);items.save(milk);items.save(coffee);

        var result=new ReusePurchasedShoppingItemsUseCase(lists,items,access,clock)
                .execute(1L,household,list.id(),0);
        var detail=result.detail();

        assertThat(result.reusedCount()).isEqualTo(2);
        assertThat(detail.list().contentRevision()).isEqualTo(1);
        assertThat(detail.pending()).extracting(ShoppingItem::name).containsExactly("Pan","Café","Leche");
        assertThat(detail.pending()).extracting(ShoppingItem::position).containsExactly(0L,1L,2L);
        assertThat(detail.purchased()).isEmpty();
        var reused=detail.pending().get(2);
        assertThat(reused.note()).isEqualTo("Entera");
        assertThat(reused.unit()).isEqualTo(ShoppingUnit.LITER);
        assertThat(reused.responsibleMemberId()).isEqualTo(otherMember);
        assertThat(reused.purchasedAt()).isNull();
        assertThat(reused.purchasedByMemberId()).isNull();
    }

    @Test void reusingAnEmptyPurchasedSectionIsANoOpButStillChecksRevision(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");
        var useCase=new ReusePurchasedShoppingItemsUseCase(lists,items,access,clock);

        assertThat(useCase.execute(1L,household,list.id(),0).detail().list().contentRevision()).isZero();
        assertThatThrownBy(()->useCase.execute(1L,household,list.id(),1))
                .isInstanceOf(ShoppingListConflictException.class);
    }

    @Test void rejectsReuseWhenTheListIsInTrash(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");
        new ShoppingListLifecycleUseCase(lists,access,clock).trash(1L,household,list.id(),list.version());

        assertThatThrownBy(()->new ReusePurchasedShoppingItemsUseCase(lists,items,access,clock)
                .execute(1L,household,list.id(),0))
                .isInstanceOf(ShoppingListStateException.class);
    }

    @Test void rejectsReuseWithoutActiveHouseholdAccess(){
        var list=new CreateShoppingListUseCase(lists,access,clock).execute(1L,household,"Compra");
        ShoppingListHouseholdAccessPort denied=(id,actor)->{throw new ShoppingListAccessDeniedException("Not active");};

        assertThatThrownBy(()->new ReusePurchasedShoppingItemsUseCase(lists,items,denied,clock)
                .execute(2L,household,list.id(),0))
                .isInstanceOf(ShoppingListAccessDeniedException.class);
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
        public boolean advanceContentRevision(UUID listId,long expected,Instant now){var value=values.get(listId);if(value.contentRevision()!=expected)return false;value.contentChanged(now);return true;}
        public void delete(ShoppingList value){values.remove(value.id());}
    }
    private static final class Items implements ShoppingItemRepository{
        final Map<UUID,ShoppingItem> values=new LinkedHashMap<>();
        public ShoppingItem save(ShoppingItem value){values.put(value.id(),value);return value;} public Optional<ShoppingItem> find(UUID id,UUID list){return Optional.ofNullable(values.get(id)).filter(item->item.listId().equals(list));}
        public List<ShoppingItem> list(UUID list){return values.values().stream().filter(item->item.listId().equals(list)).toList();}public long nextPendingPosition(UUID list){return list(list).stream().filter(item->item.status()==ShoppingItemStatus.PENDING).mapToLong(ShoppingItem::position).max().orElse(-1)+1;}
        public void delete(ShoppingItem value){values.remove(value.id());}public int deletePurchased(UUID list){var ids=list(list).stream().filter(item->item.status()==ShoppingItemStatus.PURCHASED).map(ShoppingItem::id).toList();ids.forEach(values::remove);return ids.size();}public int unassign(UUID household,UUID member,Instant now){return 0;}
    }
}
