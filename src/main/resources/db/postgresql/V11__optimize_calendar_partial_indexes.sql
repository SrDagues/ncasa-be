DROP INDEX IF EXISTS ix_calendar_household_start;
DROP INDEX IF EXISTS ix_calendar_household_deleted;
CREATE INDEX ix_calendar_household_active ON calendar_entries(household_id, start_date) WHERE deleted_at IS NULL;
CREATE INDEX ix_calendar_household_trash ON calendar_entries(household_id, deleted_at) WHERE deleted_at IS NOT NULL;
