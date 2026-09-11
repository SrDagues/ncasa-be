package ncasa.shoppinglist.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import ncasa.shoppinglist.application.*;
import ncasa.shoppinglist.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{householdId}/shopping-lists")
@Transactional
public class ShoppingListController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingListController.class);

    private final CreateShoppingListUseCase create;
    private final ListShoppingListsUseCase list;
    private final GetShoppingListUseCase get;
    private final UpdateShoppingListUseCase update;
    private final ShoppingListLifecycleUseCase lifecycle;
    private final AddShoppingItemUseCase addItem;
    private final UpdateShoppingItemUseCase updateItem;
    private final ShoppingItemLifecycleUseCase itemLifecycle;
    private final ReorderShoppingItemsUseCase reorder;

    public ShoppingListController(CreateShoppingListUseCase create, ListShoppingListsUseCase list,
            GetShoppingListUseCase get, UpdateShoppingListUseCase update, ShoppingListLifecycleUseCase lifecycle,
            AddShoppingItemUseCase addItem, UpdateShoppingItemUseCase updateItem,
            ShoppingItemLifecycleUseCase itemLifecycle, ReorderShoppingItemsUseCase reorder) {
        this.create = create;
        this.list = list;
        this.get = get;
        this.update = update;
        this.lifecycle = lifecycle;
        this.addItem = addItem;
        this.updateItem = updateItem;
        this.itemLifecycle = itemLifecycle;
        this.reorder = reorder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    List<ListResponse> lists(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @RequestParam(defaultValue = "false") boolean trashed) {
        return list.execute(user.id(), householdId, trashed).stream().map(ListResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ListResponse create(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @Valid @RequestBody CreateListRequest request) {
        var result = create.execute(user.id(), householdId, request.name());
        audit("create", householdId, result.id(), null, "success");
        return ListResponse.from(result);
    }

    @GetMapping("/{listId}")
    @Transactional(readOnly = true)
    ResponseEntity<DetailResponse> detail(@AuthenticationPrincipal IdentityUserDetails user,
            @PathVariable UUID householdId, @PathVariable UUID listId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        var detail = get.execute(user.id(), householdId, listId);
        String etag = etag(detail.list());
        if (etag.equals(ifNoneMatch)) return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        return ResponseEntity.ok().eTag(etag).body(DetailResponse.from(detail));
    }

    @PutMapping("/{listId}")
    ListResponse update(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @Valid @RequestBody UpdateListRequest request) {
        var result = update.execute(user.id(), householdId, listId, request.version(), request.name(), request.calendarSeriesId());
        audit("update", householdId, listId, null, "success");
        return ListResponse.from(result);
    }

    @PostMapping("/{listId}/trash")
    ListResponse trash(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @Valid @RequestBody VersionRequest request) {
        var result = lifecycle.trash(user.id(), householdId, listId, request.version());
        audit("trash", householdId, listId, null, "success");
        return ListResponse.from(result);
    }

    @PostMapping("/{listId}/restore")
    ListResponse restore(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @Valid @RequestBody VersionRequest request) {
        var result = lifecycle.restore(user.id(), householdId, listId, request.version());
        audit("restore", householdId, listId, null, "success");
        return ListResponse.from(result);
    }

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void purge(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @RequestParam @PositiveOrZero long version) {
        lifecycle.purge(user.id(), householdId, listId, version);
        audit("purge", householdId, listId, null, "success");
    }

    @PostMapping("/{listId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    ItemResponse addItem(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @Valid @RequestBody ItemRequest request) {
        var result = addItem.execute(user.id(), householdId, listId, request.command());
        audit("add_item", householdId, listId, result.id(), "success");
        return ItemResponse.from(result);
    }

    @PutMapping("/{listId}/items/{itemId}")
    ItemResponse updateItem(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @PathVariable UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
        var result = updateItem.execute(user.id(), householdId, listId, itemId, request.version(), request.item().command());
        audit("update_item", householdId, listId, itemId, "success");
        return ItemResponse.from(result);
    }

    @DeleteMapping("/{listId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteItem(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @PathVariable UUID itemId, @RequestParam @PositiveOrZero long version) {
        itemLifecycle.delete(user.id(), householdId, listId, itemId, version);
        audit("delete_item", householdId, listId, itemId, "success");
    }

    @PostMapping("/{listId}/items/{itemId}/purchase")
    ItemResponse purchase(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @PathVariable UUID itemId, @Valid @RequestBody VersionRequest request) {
        var result = itemLifecycle.purchase(user.id(), householdId, listId, itemId, request.version());
        audit("purchase_item", householdId, listId, itemId, "success");
        return ItemResponse.from(result);
    }

    @PostMapping("/{listId}/items/{itemId}/reopen")
    ItemResponse reopen(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @PathVariable UUID itemId, @Valid @RequestBody VersionRequest request) {
        var result = itemLifecycle.reopen(user.id(), householdId, listId, itemId, request.version());
        audit("reopen_item", householdId, listId, itemId, "success");
        return ItemResponse.from(result);
    }

    @PutMapping("/{listId}/items/order")
    ListResponse reorder(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @Valid @RequestBody ReorderRequest request) {
        var result = reorder.execute(user.id(), householdId, listId, request.contentRevision(), request.itemIds());
        audit("reorder_items", householdId, listId, null, "success");
        return ListResponse.from(result);
    }

    @DeleteMapping("/{listId}/items/purchased")
    ClearPurchasedResponse clearPurchased(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID listId, @RequestParam @PositiveOrZero long contentRevision) {
        int deleted = itemLifecycle.clearPurchased(user.id(), householdId, listId, contentRevision);
        audit("clear_purchased", householdId, listId, null, "success");
        return new ClearPurchasedResponse(deleted);
    }

    private static String etag(ShoppingList value) {
        return "\"" + value.version() + "-" + value.contentRevision() + "\"";
    }

    private static void audit(String action, UUID householdId, UUID listId, UUID itemId, String result) {
        LOGGER.atInfo().addKeyValue("event.action", action).addKeyValue("household.id", householdId)
                .addKeyValue("shopping_list.id", listId).addKeyValue("shopping_item.id", itemId)
                .addKeyValue("event.outcome", result).log("shopping_list_operation");
    }

    record CreateListRequest(@NotBlank @Size(max = 80) String name) {}
    record UpdateListRequest(@PositiveOrZero long version, @NotBlank @Size(max = 80) String name, UUID calendarSeriesId) {}
    record VersionRequest(@PositiveOrZero long version) {}
    record ReorderRequest(@PositiveOrZero long contentRevision, @NotNull List<@NotNull UUID> itemIds) {}
    record UpdateItemRequest(@PositiveOrZero long version, @NotNull @Valid ItemRequest item) {}
    record ItemRequest(@NotBlank @Size(max = 160) String name, BigDecimal quantity, ShoppingUnit unit,
            @Size(max = 30) String customUnit, @Size(max = 500) String note, UUID responsibleMemberId) {
        ShoppingItemCommand command() {
            return new ShoppingItemCommand(name, quantity, unit, customUnit, note, responsibleMemberId);
        }
    }

    record ClearPurchasedResponse(int deleted) {}
    record ListResponse(UUID id, String name, UUID calendarSeriesId, ShoppingListStatus status,
            UUID createdByMemberId, Instant createdAt, Instant updatedAt, Instant deletedAt, long version,
            long contentRevision) {
        static ListResponse from(ShoppingList value) {
            return new ListResponse(value.id(), value.name(), value.calendarSeriesId(), value.status(),
                    value.createdByMemberId(), value.createdAt(), value.updatedAt(), value.deletedAt(),
                    value.version(), value.contentRevision());
        }
    }

    record DetailResponse(ListResponse list, List<ItemResponse> pending, List<ItemResponse> purchased) {
        static DetailResponse from(ShoppingListDetail value) {
            return new DetailResponse(ListResponse.from(value.list()), value.pending().stream().map(ItemResponse::from).toList(),
                    value.purchased().stream().map(ItemResponse::from).toList());
        }
    }

    record ItemResponse(UUID id, UUID listId, String name, BigDecimal quantity, ShoppingUnit unit,
            String customUnit, String note, UUID responsibleMemberId, UUID addedByMemberId,
            UUID purchasedByMemberId, ShoppingItemStatus status, long position, Instant createdAt,
            Instant updatedAt, Instant purchasedAt, long version) {
        static ItemResponse from(ShoppingItem value) {
            return new ItemResponse(value.id(), value.listId(), value.name(), value.quantity(), value.unit(),
                    value.customUnit(), value.note(), value.responsibleMemberId(), value.addedByMemberId(),
                    value.purchasedByMemberId(), value.status(), value.position(), value.createdAt(),
                    value.updatedAt(), value.purchasedAt(), value.version());
        }
    }
}
