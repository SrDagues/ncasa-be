CREATE INDEX idx_shopping_lists_active_household
    ON shopping_lists(household_id, updated_at DESC)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_shopping_items_pending_order
    ON shopping_items(list_id, position, id)
    WHERE status = 'PENDING';

CREATE INDEX idx_shopping_items_purchased_order
    ON shopping_items(list_id, purchased_at, id)
    WHERE status = 'PURCHASED';
