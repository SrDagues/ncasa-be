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

## Notification consumption

The outbox dispatcher now feeds the persistent in-app inbox for approaching occurrences, last installments and
plans requiring attention. Delivery remains at least once, while the inbox unique key `(event_id,
recipient_account_id)` prevents user-visible duplicates.

Operational checks:

- A `PUBLISHED` supported event should have one inbox row per eligible active recipient at dispatch time.
- A supported event repeatedly returning to `PENDING` indicates malformed payload, missing source context or a
  persistence failure; inspect `notification_consumption_failed` without logging the payload.
- A supported event in `FAILED` is safe to replay after correcting the cause because consumers are idempotent.
- Do not backfill already published events by inserting inbox rows manually.
- Do not remove old inbox rows when membership changes; access control hides them while retaining audit history.
