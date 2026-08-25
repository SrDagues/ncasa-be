DROP INDEX idx_expense_plans_due;
DROP INDEX idx_expense_plans_reminder_due;
DROP INDEX idx_outbox_pending;

CREATE INDEX idx_expense_plans_due ON expense_plans(next_occurrence_due_at,id)
    WHERE status='ACTIVE';
CREATE INDEX idx_expense_plans_reminder_due ON expense_plans(next_reminder_at,id)
    WHERE status='ACTIVE' AND next_reminder_at IS NOT NULL;
CREATE INDEX idx_outbox_pending ON outbox_messages(available_at,id)
    WHERE status='PENDING';
