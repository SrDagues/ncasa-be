# RFC-0006: In-app notification bounded context

- Status: Implemented
- Created: 2026-09-04
- Authors: nCasa
- Related ADRs: ADR-0008, ADR-0009

## Summary

Introduce an account-owned, persistent notification inbox. Its first producer is the Expense Plan outbox and its
first supported messages are occurrence reminders, last-installment reminders and plans requiring attention.

## Motivation

Expense plans already calculate reminder instants and publish durable events, but the Spring publisher had no
consumer. Users therefore had no visible notification, history or unread state. The application shell already
contains a notification entry point, so the missing backend capability is a persistent, authorized inbox.

## Goals

- Deliver plan reminders to active affected members.
- Deliver attention messages to the active creator and active household administrators.
- Provide an account-scoped paginated inbox, unread count and idempotent read operations.
- Preserve at-least-once delivery without duplicate user notifications.
- Keep Notification independent from Expense, Household and Identity & Access domain models.

## Non-goals

- Email, mobile push, WebSocket/SSE delivery or notification preferences.
- Notification deletion, retention policies or historical-event backfill.
- Notifications for manually created expenses, settlements, invitations or calendar events.

## Design

```text
Expense transactional outbox
  -> Spring integration-event adapter
    -> Notification input adapter
      -> ConsumeExpensePlanNotificationUseCase
        -> NotificationRecipientPolicy
        -> NotificationRepository
```

Notification defines scalar references for account, household, plan and integration event. Infrastructure adapters
invoke public application queries in Expense and Household to obtain a plan snapshot and active membership
directory. Notification never imports their domain or persistence types.

The consumer supports `ExpensePlanOccurrenceApproaching`, `ExpensePlanLastInstallmentApproaching` and
`ExpensePlanAttentionRequired`. Other integration events are acknowledged without work. Recognized malformed
events fail so the existing outbox retry and terminal `FAILED` behaviour remains effective.

Reminder recipients are the union of payer and participants, restricted to active members and deduplicated by
account. Attention recipients are the active plan creator and active administrators. An inactive or archived
household produces no inbox entries.

## API

```http
GET  /api/notifications?unreadOnly=false&page=0&size=20
GET  /api/notifications/unread-count
POST /api/notifications/{notificationId}/read
POST /api/notifications/read-all
```

The authenticated account is always derived from the JWT. Results span all currently accessible households.
Resources belonging to another account or an inaccessible household are reported as not found.

## Persistence and delivery

Migration V9 creates `in_app_notifications`. `(event_id, recipient_account_id)` is unique. The table stores a
snapshot needed to render a message without coupling reads to the source aggregate. Cross-context references are
scalar and intentionally have no foreign keys or JPA relations, allowing the inbox record to outlive source access.

Delivery is atomic per integration event. If creation fails for any recipient, the listener transaction rolls back
and the outbox retries. Messages already `PUBLISHED` before rollout are not replayed.

## Security, privacy and operations

Inbox queries intersect recipient ownership with the account's active households. Losing membership makes an old
notification inaccessible without deleting its audit row. Logs contain identifiers, type, result and counts only;
they exclude subject, attention reason, email, token and serialized payload.

## Testing strategy

- Plain unit tests for notification invariants and recipient selection.
- Application tests with fakes for consumption, deduplication, authorization and read operations.
- PostgreSQL Testcontainers tests for mapping, constraints, ordering and uniqueness.
- HTTP/security tests for account isolation and input bounds.
- An end-to-end job/outbox/listener test for reminders and retry idempotency.

## Decision

Implemented. Email and real-time transport remain separate future adapters and do not alter this domain model.
