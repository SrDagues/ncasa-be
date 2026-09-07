# ADR-0009: Build the in-app inbox as an idempotent outbox consumer

- Status: Accepted
- Date: 2026-09-04
- Owners: nCasa
- Related RFC: RFC-0006

## Context

Expense Plan events are durably committed through the transactional outbox and delivered at least once. A user
inbox needs account ownership, read state and authorization rules that do not belong to the Expense aggregate.
Directly adding inbox rows to Expense would couple accounting transactions to a presentation concern, while a
consumer without deduplication would expose duplicate notifications after a process crash.

## Decision

Notification is a separate bounded context. A synchronous Spring input adapter consumes published outbox events
and invokes a framework-free application use case. The consumer stores one notification per integration event and
recipient, protected by a database unique constraint. All recipients are stored in one transaction and any failure
is propagated to the existing outbox retry mechanism.

Notification owns its aggregate and persistence model. It resolves source and recipient data through output ports
implemented by adapters that call Expense and Household application queries. Cross-context identifiers are scalar;
there are no domain imports, cross-context JPA relationships or source-table foreign keys.

## Alternatives considered

### Store notifications inside Expense

Rejected because inbox lifecycle, read state and account-wide queries are not accounting responsibilities.

### Deliver directly from scheduled jobs

Rejected because a crash between plan update and delivery could lose a reminder and would bypass ADR-0008.

### Introduce a message broker now

Deferred. The publisher port already permits a future broker adapter; the current modular monolith does not need
the operational cost.

## Consequences

- Notification Domain and Application remain independent of Spring, JPA and source bounded contexts.
- At-least-once publication becomes exactly-once-visible per recipient.
- Access is evaluated at read time, so users who leave a household lose inbox access immediately.
- Published historical events are not backfilled.
- Email and push can later consume the same application-level capability without changing Expense.

## Validation

Domain/application unit tests, ArchUnit, PostgreSQL Testcontainers integration tests and HTTP security tests enforce
the decision. Operational logs expose event IDs, types, recipient counts and failures without message content.
