# ADR-0007: Expense plans are a separate aggregate

## Status

Accepted.

## Decision

Future, recurring and installment expenses are represented by `ExpensePlan`, not by partially-created
`Expense` aggregates. A plan owns its template, finite schedule, time zone, reminder policy, cursor and
lifecycle. An `Expense` exists only when an occurrence is due.

Generated expenses carry `source=PLAN`, `sourcePlanId` and the scheduled local date as `occurrenceKey`.
The database uniqueness constraint on `(source_plan_id, occurrence_key)` is the final idempotency guard.

## Consequences

- Hundreds of future expenses are not persisted in advance.
- Plans can be paused and cancelled without mutating confirmed expenses.
- Forecasts remain derived read models.
- Templates and schedules are immutable; changing either requires cancelling and recreating a plan.
