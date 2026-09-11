package ncasa.shoppinglist.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoppingListTest {
    private static final UUID HOUSEHOLD = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-09-11T10:00:00Z");

    @Test void normalizesListNamesForUniqueness() {
        assertThat(ShoppingListName.of("  Compra   Semanal ").normalized()).isEqualTo("compra semanal");
        assertThat(ShoppingListName.of("  Compra   Semanal ").value()).isEqualTo("Compra Semanal");
    }

    @Test void movesListToTrashAndRemovesCalendarLink() {
        var list = ShoppingList.create(UUID.randomUUID(), HOUSEHOLD, "Compra", ACTOR, NOW);
        list.linkCalendar(UUID.randomUUID(), NOW.plusSeconds(1));
        list.trash(NOW.plusSeconds(2));
        assertThat(list.status()).isEqualTo(ShoppingListStatus.TRASHED);
        assertThat(list.calendarSeriesId()).isNull();
        assertThatThrownBy(() -> list.rename("Otra", NOW.plusSeconds(3)))
                .isInstanceOf(ShoppingListStateException.class);
    }

    @Test void purchasesAndReopensAnItemPreservingAudit() {
        var item = ShoppingItem.create(UUID.randomUUID(), UUID.randomUUID(), "Leche", new BigDecimal("2.5"),
                ShoppingUnit.LITER, null, "Entera", ACTOR, ACTOR, 3, NOW);
        var buyer = UUID.randomUUID();
        item.purchase(buyer, NOW.plusSeconds(2));
        assertThat(item.status()).isEqualTo(ShoppingItemStatus.PURCHASED);
        assertThat(item.purchasedByMemberId()).isEqualTo(buyer);
        item.reopen(8, NOW.plusSeconds(3));
        assertThat(item.status()).isEqualTo(ShoppingItemStatus.PENDING);
        assertThat(item.position()).isEqualTo(8);
        assertThat(item.addedByMemberId()).isEqualTo(ACTOR);
        assertThat(item.purchasedByMemberId()).isNull();
    }

    @Test void requiresCustomUnitOnlyForOther() {
        assertThatThrownBy(() -> ShoppingItem.create(UUID.randomUUID(), UUID.randomUUID(), "Cable",
                BigDecimal.ONE, ShoppingUnit.OTHER, null, null, null, ACTOR, 0, NOW))
                .isInstanceOf(ShoppingListRuleViolationException.class);
        assertThatThrownBy(() -> ShoppingItem.create(UUID.randomUUID(), UUID.randomUUID(), "Leche",
                BigDecimal.ONE, ShoppingUnit.LITER, "botella", null, null, ACTOR, 0, NOW))
                .isInstanceOf(ShoppingListRuleViolationException.class);
    }
}
