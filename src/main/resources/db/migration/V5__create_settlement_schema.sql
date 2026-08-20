CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    created_by_member_id UUID NOT NULL REFERENCES household_members(id),
    from_member_id UUID NOT NULL REFERENCES household_members(id),
    to_member_id UUID NOT NULL REFERENCES household_members(id),
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    settlement_date DATE NOT NULL,
    note VARCHAR(240),
    status VARCHAR(20) NOT NULL,
    void_reason VARCHAR(500),
    idempotency_key UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    voided_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_settlement_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_settlement_currency CHECK (char_length(currency) = 3),
    CONSTRAINT ck_settlement_members_differ CHECK (from_member_id <> to_member_id),
    CONSTRAINT ck_settlement_status CHECK (status IN ('CONFIRMED', 'VOIDED')),
    CONSTRAINT ck_settlement_void_lifecycle CHECK (
        (status = 'VOIDED' AND void_reason IS NOT NULL AND voided_at IS NOT NULL)
        OR (status = 'CONFIRMED' AND void_reason IS NULL AND voided_at IS NULL)
    ),
    CONSTRAINT uk_settlement_idempotency UNIQUE (household_id, created_by_member_id, idempotency_key)
);
CREATE INDEX idx_settlements_household_status_date ON settlements(household_id, status, settlement_date DESC, created_at DESC);
CREATE INDEX idx_settlements_household_from ON settlements(household_id, from_member_id);
CREATE INDEX idx_settlements_household_to ON settlements(household_id, to_member_id);
