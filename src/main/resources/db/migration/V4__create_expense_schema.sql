CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    created_by_member_id UUID NOT NULL REFERENCES household_members(id),
    payer_member_id UUID NOT NULL REFERENCES household_members(id),
    description VARCHAR(240) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    expense_date DATE NOT NULL,
    split_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    void_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    voided_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_expense_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_expense_currency CHECK (char_length(currency) = 3),
    CONSTRAINT ck_expense_split_type CHECK (split_type IN ('EQUAL', 'EXACT')),
    CONSTRAINT ck_expense_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'VOIDED')),
    CONSTRAINT ck_expense_source CHECK (source IN ('MANUAL')),
    CONSTRAINT ck_expense_void_lifecycle CHECK (
        (status = 'VOIDED' AND void_reason IS NOT NULL AND voided_at IS NOT NULL)
        OR (status <> 'VOIDED' AND void_reason IS NULL AND voided_at IS NULL)
    )
);

CREATE TABLE expense_allocations (
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES household_members(id),
    amount NUMERIC(19,4) NOT NULL,
    CONSTRAINT uk_expense_allocation_member UNIQUE (expense_id, member_id),
    CONSTRAINT ck_expense_allocation_positive CHECK (amount > 0)
);

CREATE INDEX idx_expenses_household_date
    ON expenses(household_id, expense_date DESC, created_at DESC);
CREATE INDEX idx_expenses_household_status_date
    ON expenses(household_id, status, expense_date DESC, created_at DESC);
CREATE INDEX idx_expense_allocations_member
    ON expense_allocations(member_id);
