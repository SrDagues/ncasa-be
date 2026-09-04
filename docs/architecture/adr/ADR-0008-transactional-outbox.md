# ADR-0008: Transactional outbox for expense-plan events

## Status

Accepted.

## Decision

Domain events produced by `ExpensePlan` are stored in `outbox_messages` in the same transaction as the
aggregate and any generated expense. A dispatcher claims messages in batches with `FOR UPDATE SKIP LOCKED`
and publishes them through an application port. The first adapter emits Spring application events.

Delivery is at least once. Consumers must deduplicate by event ID. Stale `PROCESSING` messages are reclaimed,
failures use exponential backoff, and messages become `FAILED` after the configured attempt limit.

## Consequences

- A process crash cannot lose a committed reminder or occurrence event.
- Publishing may be repeated after a crash and consumers must be idempotent.
- A broker and notification delivery can be introduced later behind the publisher port.
