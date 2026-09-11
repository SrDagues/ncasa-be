# ADR-0010: Separate shopping-list aggregates and scalar cross-context links

- Status: Accepted
- Date: 2026-09-11
- Owners: nCasa
- Related RFC: RFC-0007

## Context

Several household members can update a list at nearly the same time. Treating a complete list and every product as
one versioned aggregate would make edits to unrelated products contend on one version. The feature also collaborates
with Household membership and recurring Calendar data without transferring ownership of either model.

## Decision

`ShoppingList` and `ShoppingItem` are separate aggregates with independent optimistic versions. List metadata uses
the list version. Product edits and state transitions use the item version. A monotonic `contentRevision`, advanced
atomically without incrementing the metadata version for ordinary item mutations, invalidates detail ETags.
Reorder atomically compares and advances that revision before changing positions, so one concurrent reorder wins.
Appending a new or reopened pending item takes a short pessimistic lock on the owning list while calculating the
next position. This serializes only tail allocation and prevents equal positions under concurrent inserts.

Cross-context references are scalar IDs. Shopping List validates membership and calendar references through
application ports and has no domain dependency or foreign key to Household, Calendar or Identity. Calendar links
target `calendar.seriesId`, remain while any active revision exists, and are removed after the last active revision.
Trashing a list also removes the link; neither calendar nor list restoration recreates it.

Household membership removal synchronously invokes a cleanup port that clears active responsibility while retaining
`addedByMemberId` and `purchasedByMemberId`. Calendar lifecycle synchronously invokes a port that removes stale
series links. The adapters live in Shopping List infrastructure.

The frontend uses Angular CDK Drag & Drop for pointer and touch ordering and native buttons for the equivalent
accessible “move up” and “move down” actions.

## Alternatives considered

### One aggregate for list and products

Rejected because unrelated product edits would share one optimistic version and create avoidable conflicts.

### Cross-context database foreign keys or JPA relationships

Rejected because they couple lifecycle and persistence ownership across bounded contexts and cannot preserve the
required historical audit references cleanly.

### Link a concrete calendar occurrence or revision

Rejected because recurrence edits create revisions; product intent belongs to the stable series identity.

### WebSocket/SSE for the MVP

Deferred. Conditional 15-second polling meets the initial collaboration requirement with lower operational cost.

## Consequences

- Different products can be updated concurrently.
- Metadata and content concurrency remain explicit and independently testable.
- Cross-context cleanup is immediate inside the modular monolith but replaceable by events later.
- Restored links require an explicit user action, avoiding accidental associations.
- Reorder requests must include every current pending item ID.
- Item creation responses include the authoritative list revision; clients do not calculate it locally.

## Validation

Domain/application tests cover invariants, transitions, restore naming and access. HTTP integration covers the
critical journey and ETag/304. ArchUnit enforces inward dependencies. Flyway constraints and repository integration
are exercised on H2 in the main suite and PostgreSQL through the project Testcontainers verification profile.
