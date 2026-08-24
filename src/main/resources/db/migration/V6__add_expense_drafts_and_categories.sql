CREATE TABLE expense_categories (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    created_by_member_id UUID NOT NULL REFERENCES household_members(id),
    name VARCHAR(80) NOT NULL,
    name_key VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    archived_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_expense_category_name UNIQUE (household_id, name_key),
    CONSTRAINT ck_expense_category_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_expense_category_lifecycle CHECK (
        (status = 'ACTIVE' AND archived_at IS NULL) OR (status = 'ARCHIVED' AND archived_at IS NOT NULL)
    )
);

ALTER TABLE expenses ADD COLUMN category_id UUID REFERENCES expense_categories(id);
ALTER TABLE expenses DROP CONSTRAINT ck_expense_split_type;
ALTER TABLE expenses ADD CONSTRAINT ck_expense_split_type CHECK (split_type IN ('EQUAL', 'EXACT', 'PERCENTAGE'));

CREATE INDEX idx_expense_categories_household_status ON expense_categories(household_id, status, name);
CREATE INDEX idx_expenses_household_category_date ON expenses(household_id, category_id, status, expense_date DESC);

CREATE TABLE expense_drafts (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    created_by_member_id UUID NOT NULL REFERENCES household_members(id),
    description VARCHAR(240),
    payer_member_id UUID REFERENCES household_members(id),
    amount NUMERIC(19,4),
    currency VARCHAR(3),
    expense_date DATE,
    category_id UUID REFERENCES expense_categories(id),
    split_type VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    confirmed_expense_id UUID UNIQUE REFERENCES expenses(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    discarded_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_expense_draft_money CHECK (
        (amount IS NULL AND currency IS NULL) OR (amount > 0 AND char_length(currency) = 3)
    ),
    CONSTRAINT ck_expense_draft_split_type CHECK (split_type IS NULL OR split_type IN ('EQUAL', 'EXACT', 'PERCENTAGE')),
    CONSTRAINT ck_expense_draft_status CHECK (status IN ('OPEN', 'CONFIRMED', 'DISCARDED')),
    CONSTRAINT ck_expense_draft_lifecycle CHECK (
        (status = 'OPEN' AND confirmed_expense_id IS NULL AND confirmed_at IS NULL AND discarded_at IS NULL)
        OR (status = 'CONFIRMED' AND confirmed_expense_id IS NOT NULL AND confirmed_at IS NOT NULL AND discarded_at IS NULL)
        OR (status = 'DISCARDED' AND confirmed_expense_id IS NULL AND confirmed_at IS NULL AND discarded_at IS NOT NULL)
    )
);

CREATE TABLE expense_draft_allocations (
    id UUID PRIMARY KEY,
    draft_id UUID NOT NULL REFERENCES expense_drafts(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES household_members(id),
    amount NUMERIC(19,4),
    percentage NUMERIC(7,2),
    CONSTRAINT uk_expense_draft_allocation_member UNIQUE (draft_id, member_id),
    CONSTRAINT ck_expense_draft_allocation_amount CHECK (amount IS NULL OR amount > 0),
    CONSTRAINT ck_expense_draft_allocation_percentage CHECK (percentage IS NULL OR (percentage > 0 AND percentage <= 100.00)),
    CONSTRAINT ck_expense_draft_allocation_value CHECK (amount IS NULL OR percentage IS NULL)
);

CREATE INDEX idx_expense_drafts_owner_status ON expense_drafts(household_id, created_by_member_id, status, updated_at DESC);

CREATE TABLE expense_classification_changes (
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL REFERENCES expenses(id),
    household_id UUID NOT NULL REFERENCES households(id),
    changed_by_member_id UUID NOT NULL REFERENCES household_members(id),
    previous_category_id UUID REFERENCES expense_categories(id),
    new_category_id UUID REFERENCES expense_categories(id),
    reason VARCHAR(500),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_expense_classification_changed CHECK (
        previous_category_id IS DISTINCT FROM new_category_id
    )
);

CREATE INDEX idx_expense_classification_history ON expense_classification_changes(expense_id, changed_at, id);
