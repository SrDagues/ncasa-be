CREATE TABLE shopping_lists (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    name VARCHAR(80) NOT NULL,
    normalized_name VARCHAR(80),
    calendar_series_id UUID,
    status VARCHAR(20) NOT NULL,
    created_by_member_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    content_revision BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_shopping_list_active_name UNIQUE (household_id, normalized_name),
    CONSTRAINT uk_shopping_list_calendar_series UNIQUE (calendar_series_id),
    CONSTRAINT ck_shopping_list_status CHECK (status IN ('ACTIVE','TRASHED')),
    CONSTRAINT ck_shopping_list_lifecycle CHECK (
        (status = 'ACTIVE' AND normalized_name IS NOT NULL AND deleted_at IS NULL)
        OR (status = 'TRASHED' AND normalized_name IS NULL AND deleted_at IS NOT NULL AND calendar_series_id IS NULL)
    )
);

CREATE TABLE shopping_items (
    id UUID PRIMARY KEY,
    list_id UUID NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    quantity NUMERIC(12,3),
    unit VARCHAR(20),
    custom_unit VARCHAR(30),
    note VARCHAR(500),
    responsible_member_id UUID,
    added_by_member_id UUID NOT NULL,
    purchased_by_member_id UUID,
    status VARCHAR(20) NOT NULL,
    position BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    purchased_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_shopping_item_quantity CHECK (quantity IS NULL OR quantity > 0),
    CONSTRAINT ck_shopping_item_unit CHECK (unit IS NULL OR unit IN ('UNIT','KILOGRAM','GRAM','LITER','MILLILITER','PACKAGE','OTHER')),
    CONSTRAINT ck_shopping_item_custom_unit CHECK (
        (unit = 'OTHER' AND custom_unit IS NOT NULL) OR (unit IS DISTINCT FROM 'OTHER' AND custom_unit IS NULL)
    ),
    CONSTRAINT ck_shopping_item_status CHECK (status IN ('PENDING','PURCHASED')),
    CONSTRAINT ck_shopping_item_purchase CHECK (
        (status = 'PENDING' AND purchased_by_member_id IS NULL AND purchased_at IS NULL)
        OR (status = 'PURCHASED' AND purchased_by_member_id IS NOT NULL AND purchased_at IS NOT NULL)
    )
);

CREATE INDEX idx_shopping_lists_household_status ON shopping_lists(household_id, status, updated_at DESC);
CREATE INDEX idx_shopping_items_list_status_position ON shopping_items(list_id, status, position, id);
CREATE INDEX idx_shopping_items_list_purchased ON shopping_items(list_id, purchased_at, id);
CREATE INDEX idx_shopping_items_responsible ON shopping_items(responsible_member_id);
