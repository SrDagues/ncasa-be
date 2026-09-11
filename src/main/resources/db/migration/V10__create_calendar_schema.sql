CREATE TABLE calendar_entries (
    id UUID PRIMARY KEY,
    series_id UUID NOT NULL,
    household_id UUID NOT NULL REFERENCES households(id),
    kind VARCHAR(24) NOT NULL,
    title VARCHAR(240) NOT NULL,
    all_day BOOLEAN NOT NULL,
    start_date DATE NOT NULL,
    start_time TIME,
    end_date DATE,
    end_time TIME,
    zone_id VARCHAR(64) NOT NULL DEFAULT 'Europe/Madrid',
    color VARCHAR(7) NOT NULL,
    location VARCHAR(240),
    note VARCHAR(1000),
    link VARCHAR(1000),
    recurrence_frequency VARCHAR(16),
    recurrence_end_type VARCHAR(24),
    recurrence_until DATE,
    recurrence_count INTEGER,
    special_date_type VARCHAR(24),
    related_member_id UUID,
    created_by_member_id UUID NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_calendar_kind CHECK (kind IN ('TASK','EVENT','SPECIAL_DATE')),
    CONSTRAINT ck_calendar_color CHECK (color ~ '^#[0-9a-fA-F]{6}$'),
    CONSTRAINT ck_calendar_timing CHECK ((all_day AND start_time IS NULL AND end_time IS NULL) OR (NOT all_day AND start_time IS NOT NULL AND ((end_date IS NULL AND end_time IS NULL) OR (end_date IS NOT NULL AND end_time IS NOT NULL)))),
    CONSTRAINT ck_calendar_recurrence CHECK ((recurrence_frequency IS NULL AND recurrence_end_type IS NULL AND recurrence_until IS NULL AND recurrence_count IS NULL) OR (recurrence_frequency IN ('WEEKLY','MONTHLY','YEARLY') AND recurrence_end_type IN ('NEVER','UNTIL_DATE','AFTER_OCCURRENCES'))),
    CONSTRAINT ck_calendar_special_date CHECK ((kind='SPECIAL_DATE' AND special_date_type IS NOT NULL) OR (kind<>'SPECIAL_DATE' AND special_date_type IS NULL AND related_member_id IS NULL))
);

CREATE TABLE calendar_entry_participants (
    entry_id UUID NOT NULL REFERENCES calendar_entries(id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    PRIMARY KEY(entry_id, member_id)
);

CREATE TABLE calendar_entry_reminders (
    entry_id UUID NOT NULL REFERENCES calendar_entries(id) ON DELETE CASCADE,
    days_before INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL,
    PRIMARY KEY(entry_id, days_before)
);

CREATE TABLE calendar_reminder_recipients (
    entry_id UUID NOT NULL REFERENCES calendar_entries(id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    PRIMARY KEY(entry_id, member_id)
);

CREATE TABLE calendar_completed_occurrences (
    entry_id UUID NOT NULL REFERENCES calendar_entries(id) ON DELETE CASCADE,
    occurrence_key VARCHAR(10) NOT NULL,
    PRIMARY KEY(entry_id, occurrence_key)
);

CREATE INDEX ix_calendar_household_start ON calendar_entries(household_id, start_date);
CREATE INDEX ix_calendar_household_deleted ON calendar_entries(household_id, deleted_at);
