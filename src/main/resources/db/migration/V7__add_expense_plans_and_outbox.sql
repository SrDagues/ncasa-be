CREATE TABLE expense_plans (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    created_by_member_id UUID NOT NULL REFERENCES household_members(id),
    description VARCHAR(240) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payer_member_id UUID NOT NULL REFERENCES household_members(id),
    category_id UUID REFERENCES expense_categories(id),
    split_type VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    zone_id VARCHAR(80) NOT NULL,
    end_type VARCHAR(30) NOT NULL,
    end_date DATE,
    total_occurrences INTEGER,
    reminder_days_before INTEGER NOT NULL,
    schedule_cursor BIGINT NOT NULL,
    materialized_occurrences INTEGER NOT NULL,
    next_occurrence DATE,
    next_occurrence_due_at TIMESTAMP WITH TIME ZONE,
    next_reminder_at TIMESTAMP WITH TIME ZONE,
    reminded_occurrence_key VARCHAR(10),
    status VARCHAR(20) NOT NULL,
    pause_reason VARCHAR(500),
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paused_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_expense_plan_amount CHECK (amount > 0),
    CONSTRAINT ck_expense_plan_frequency CHECK (frequency IN ('ONCE','WEEKLY','MONTHLY','YEARLY')),
    CONSTRAINT ck_expense_plan_split CHECK (split_type IN ('EQUAL','EXACT','PERCENTAGE')),
    CONSTRAINT ck_expense_plan_end CHECK ((end_type='UNTIL_DATE' AND end_date IS NOT NULL AND total_occurrences IS NULL) OR (end_type='AFTER_OCCURRENCES' AND end_date IS NULL AND total_occurrences > 0)),
    CONSTRAINT ck_expense_plan_reminder CHECK (reminder_days_before BETWEEN 0 AND 30),
    CONSTRAINT ck_expense_plan_status CHECK (status IN ('ACTIVE','PAUSED','CANCELLED','COMPLETED')),
    CONSTRAINT ck_expense_plan_lifecycle CHECK (
      (status='ACTIVE' AND next_occurrence IS NOT NULL AND cancelled_at IS NULL AND completed_at IS NULL)
      OR (status='PAUSED' AND paused_at IS NOT NULL AND cancelled_at IS NULL AND completed_at IS NULL)
      OR (status='CANCELLED' AND cancellation_reason IS NOT NULL AND cancelled_at IS NOT NULL AND next_occurrence IS NULL)
      OR (status='COMPLETED' AND completed_at IS NOT NULL AND next_occurrence IS NULL)
    )
);

CREATE TABLE expense_plan_allocations (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES expense_plans(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES household_members(id),
    amount NUMERIC(19,4),
    percentage NUMERIC(7,2),
    CONSTRAINT uk_expense_plan_allocation_member UNIQUE(plan_id,member_id),
    CONSTRAINT ck_expense_plan_allocation_value CHECK (amount IS NULL OR percentage IS NULL)
);

CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    deduplication_key VARCHAR(300) NOT NULL UNIQUE,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    locked_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000),
    CONSTRAINT ck_outbox_status CHECK(status IN ('PENDING','PROCESSING','PUBLISHED','FAILED'))
);

ALTER TABLE expenses ADD COLUMN source_plan_id UUID REFERENCES expense_plans(id);
ALTER TABLE expenses ADD COLUMN occurrence_key VARCHAR(10);
ALTER TABLE expenses DROP CONSTRAINT ck_expense_source;
ALTER TABLE expenses ADD CONSTRAINT ck_expense_source CHECK(source IN ('MANUAL','PLAN'));
ALTER TABLE expenses ADD CONSTRAINT ck_expense_source_metadata CHECK(
  (source='MANUAL' AND source_plan_id IS NULL AND occurrence_key IS NULL)
  OR (source='PLAN' AND source_plan_id IS NOT NULL AND occurrence_key IS NOT NULL)
);
ALTER TABLE expenses ADD CONSTRAINT uk_expense_plan_occurrence UNIQUE(source_plan_id,occurrence_key);

CREATE INDEX idx_expense_plans_household_status_next ON expense_plans(household_id,status,next_occurrence);
CREATE INDEX idx_expense_plans_payer ON expense_plans(household_id,payer_member_id);
CREATE INDEX idx_expense_plan_allocations_member ON expense_plan_allocations(member_id);
CREATE INDEX idx_expense_plans_due ON expense_plans(status,next_occurrence_due_at,id);
CREATE INDEX idx_expense_plans_reminder_due ON expense_plans(status,next_reminder_at,id);
CREATE INDEX idx_outbox_pending ON outbox_messages(status,available_at,id);
CREATE INDEX idx_expenses_source_plan ON expenses(source_plan_id,occurrence_key);
