package ncasa.shoppinglist.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import ncasa.shoppinglist.application.port.out.ShoppingItemRepository;
import ncasa.shoppinglist.application.port.out.ShoppingListRepository;
import ncasa.shoppinglist.domain.ShoppingItem;
import ncasa.shoppinglist.domain.ShoppingList;
import ncasa.shoppinglist.domain.ShoppingUnit;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

class JpaShoppingListRepositoriesIT extends PostgresIntegrationTest {
    @Autowired ShoppingListRepository lists;
    @Autowired ShoppingItemRepository items;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;

    private final Instant now = Instant.parse("2026-09-11T10:00:00Z");
    private UUID householdId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM shopping_items");
        jdbc.update("DELETE FROM shopping_lists");
        householdId = UUID.randomUUID();
        memberId = UUID.randomUUID();
    }

    @Test
    void persistsAuditOrderingAndContentRevisionAndCascadesItems() {
        var list = lists.save(ShoppingList.create(UUID.randomUUID(), householdId, "Compra semanal", memberId, now));
        var milk = items.save(ShoppingItem.create(UUID.randomUUID(), list.id(), "Leche", new BigDecimal("2.000"),
                ShoppingUnit.LITER, null, "Entera", memberId, memberId, 0, now));
        var bread = items.save(ShoppingItem.create(UUID.randomUUID(), list.id(), "Pan", null,
                null, null, null, null, memberId, 1, now.plusSeconds(1)));
        var coffee = ShoppingItem.create(UUID.randomUUID(), list.id(), "Café", new BigDecimal("500"),
                ShoppingUnit.GRAM, null, null, null, memberId, 2, now.plusSeconds(2));
        coffee.purchase(memberId, now.plusSeconds(3));
        items.save(coffee);

        assertThat(items.nextPendingPosition(list.id())).isEqualTo(2);
        assertThat(items.find(milk.id(), list.id()).orElseThrow().addedByMemberId()).isEqualTo(memberId);
        assertThat(items.find(bread.id(), list.id()).orElseThrow().quantity()).isNull();
        assertThat(items.find(coffee.id(), list.id()).orElseThrow().purchasedAt()).isEqualTo(now.plusSeconds(3));

        transactions.executeWithoutResult(status -> lists.touchContent(list.id(), now.plusSeconds(4)));
        assertThat(lists.find(list.id(), householdId).orElseThrow().contentRevision()).isEqualTo(1);
        Boolean advanced = transactions.execute(
                status -> lists.advanceContentRevision(list.id(), 1, now.plusSeconds(5)));
        Boolean staleAdvance = transactions.execute(
                status -> lists.advanceContentRevision(list.id(), 1, now.plusSeconds(6)));
        assertThat(advanced).isTrue();
        assertThat(staleAdvance).isFalse();

        lists.delete(lists.find(list.id(), householdId).orElseThrow());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM shopping_items WHERE list_id=?", Long.class, list.id()))
                .isZero();
    }

    @Test
    void enforcesNormalizedActiveNameAndCalendarSeriesUniqueness() {
        var seriesId = UUID.randomUUID();
        var first = ShoppingList.create(UUID.randomUUID(), householdId, "Compra semanal", memberId, now);
        first.linkCalendar(seriesId, now);
        lists.save(first);

        assertThatThrownBy(() -> lists.save(ShoppingList.create(
                UUID.randomUUID(), householdId, "  COMPRA   SEMANAL  ", memberId, now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        var otherHouseholdList = ShoppingList.create(
                UUID.randomUUID(), UUID.randomUUID(), "Otra compra", memberId, now);
        otherHouseholdList.linkCalendar(seriesId, now);
        assertThatThrownBy(() -> lists.save(otherHouseholdList))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsIncoherentCustomUnitsAndPurchaseAudit() {
        var list = lists.save(ShoppingList.create(UUID.randomUUID(), householdId, "Compra", memberId, now));

        assertThatThrownBy(() -> insertItem(list.id(), "OTHER", null, "PENDING", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(list.id(), "UNIT", "caja", "PENDING", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(list.id(), null, null, "PURCHASED", null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertItem(UUID listId, String unit, String customUnit, String status,
            UUID purchasedBy, Instant purchasedAt) {
        jdbc.update("""
                INSERT INTO shopping_items(
                    id, list_id, name, unit, custom_unit, added_by_member_id,
                    purchased_by_member_id, status, position, created_at, updated_at, purchased_at, version
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0)
                """, UUID.randomUUID(), listId, "Producto", unit, customUnit, memberId,
                purchasedBy, status, 0, now, now, purchasedAt);
    }
}
