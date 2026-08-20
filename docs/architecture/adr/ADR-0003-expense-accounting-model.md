# ADR-0003: Preserve materialized expense allocations

- Status: Accepted
- Date: 2026-08-20
- Owners: nCasa
- Related RFC: RFC-0002

## Context

nCasa must later calculate household balances, personal positions and debts from manual, planned, receipt and voice-created expenses. Recomputing an old expense from a split rule would make its result depend on later membership, rounding or rule changes.

## Decision

We will model `Expense` as the aggregate root for an occurred financial fact and persist one concrete monetary allocation per participant. Payer, creator and participants are stable member references. Confirmed expenses are preserved and may only transition to `VOIDED` in the MVP.

Expense remains independent from Household internals through an application port and an infrastructure adapter. JPA representations remain separate from domain objects.

Future recurring and scheduled behaviour will use a separate `ExpensePlan` aggregate. Balances and debt summaries will be read models, not entities inside `Expense`.

## Alternatives considered

### Persist only percentages or an equal-split marker

This stores less data but makes historical results depend on future calculation and rounding rules.

### Put recurrence, capture and balances inside Expense

This centralizes the feature but creates an aggregate with unrelated lifecycles and external responsibilities.

### Share Household entities with Expense

This reduces mapping but couples bounded contexts and their persistence models.

## Rationale

Materialized allocations provide reproducible accounting and make later query models straightforward. Separating plans, capture and projections keeps aggregate boundaries small. Scalar references and ports preserve the modular-monolith boundary while allowing synchronous validation.

## Consequences

### Positive

- Historical balances remain reproducible.
- Rounding happens once at creation.
- Members can leave without breaking expense history.
- OCR, voice and plans can create the same Expense model later.

### Negative / trade-offs

- Every expense requires allocation rows.
- Equal-split creation needs deterministic remainder handling.
- Cross-context identifiers require mapping at the Household boundary.

### Risks

- Future cross-household sharing may need a different scope model.
- Currency conversion cannot be inferred from stored currency codes and requires a future explicit policy.

## Implementation constraints

- Domain code has no Spring, JPA, Jackson or HTTP dependencies.
- Application does not depend on infrastructure.
- Expense does not depend on Household infrastructure or domain objects.
- Monetary amounts use decimal arithmetic and explicit currency.
- A confirmed expense is never physically deleted.
- Repository adapters rehydrate the complete aggregate, including allocations.

## Validation

- Domain and application behaviour tests.
- PostgreSQL Testcontainers adapter tests.
- Web security and contract tests.
- ArchUnit dependency rules.
- End-to-end household expense flow.

## Follow-up

- Drafts and categories.
- Balance, debt and settlement read models.
- `ExpensePlan` and reliable notification events.
- Receipt and voice capture.
- RFC for separated-parent expenses.
