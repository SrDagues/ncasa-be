# Expense-plan scheduled jobs

Three jobs run by default: reminders every minute, occurrence generation every minute and outbox dispatch every
five seconds. Batch sizes and retry limits use the `ncasa.expense-plans.*` and `ncasa.outbox.*` properties.

Operational checks:

- Active plans with `next_occurrence_due_at` in the past indicate generation backlog.
- Pending outbox rows with old `available_at` indicate dispatch backlog.
- `FAILED` outbox rows require investigation and an explicit operational replay.
- `PAUSED` plans with the technical attention reason require the household to recreate or correct referenced
  members/categories before reactivation.

Never repair a plan by inserting expenses manually. The unique occurrence constraint and aggregate cursor must
remain aligned.
