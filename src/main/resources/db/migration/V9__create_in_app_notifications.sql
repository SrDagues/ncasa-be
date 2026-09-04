CREATE TABLE in_app_notifications (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    recipient_account_id BIGINT NOT NULL,
    household_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    kind VARCHAR(80) NOT NULL,
    subject VARCHAR(240) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    occurrence_date DATE NOT NULL,
    occurrence_number INTEGER NOT NULL,
    total_occurrences INTEGER,
    attention_reason VARCHAR(500),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_notification_event_recipient UNIQUE(event_id, recipient_account_id),
    CONSTRAINT ck_notification_kind CHECK(kind IN ('EXPENSE_PLAN_OCCURRENCE_APPROACHING','EXPENSE_PLAN_LAST_INSTALLMENT_APPROACHING','EXPENSE_PLAN_ATTENTION_REQUIRED')),
    CONSTRAINT ck_notification_amount CHECK(amount > 0),
    CONSTRAINT ck_notification_currency CHECK(LENGTH(currency) = 3 AND currency = UPPER(currency)),
    CONSTRAINT ck_notification_occurrence CHECK(occurrence_number > 0 AND (total_occurrences IS NULL OR total_occurrences >= occurrence_number)),
    CONSTRAINT ck_notification_attention CHECK(
      (kind = 'EXPENSE_PLAN_ATTENTION_REQUIRED' AND attention_reason IS NOT NULL)
      OR (kind <> 'EXPENSE_PLAN_ATTENTION_REQUIRED' AND attention_reason IS NULL)
    ),
    CONSTRAINT ck_notification_read_time CHECK(read_at IS NULL OR read_at >= created_at)
);

CREATE INDEX idx_notifications_account_created ON in_app_notifications(recipient_account_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_account_unread ON in_app_notifications(recipient_account_id, read_at, created_at DESC);
CREATE INDEX idx_notifications_household_account ON in_app_notifications(household_id, recipient_account_id);
