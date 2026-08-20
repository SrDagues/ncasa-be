# RFC-0002: Expense bounded context MVP

- Status: Implemented
- Created: 2026-08-20
- Authors: nCasa
- Related ADRs: ADR-0003

## Summary

Introduce the `Expense` bounded context as the accounting core of nCasa. The first delivery records manual household expenses, materializes how much belongs to every participant, exposes detail and paginated history, and preserves voided expenses for audit.

## Motivation

nCasa needs a reliable base for later balances, debts, recurring plans, receipt capture and voice input. Those capabilities are only trustworthy if historical expenses keep their original payer, participants, monetary allocations and lifecycle.

## Goals

- Record a confirmed manual expense for an active household.
- Support equal and exact-amount splits.
- Preserve concrete monetary allocations instead of recalculating historical rules.
- List and retrieve household expenses.
- Void a confirmed expense without deleting its history.
- Keep Expense independent from Household and Identity & Access implementation details.

## Non-goals

- Drafts and categories.
- Balances, debt simplification and settlements.
- Recurring or future expense plans.
- Receipt, OCR or voice capture.
- Notifications, event budgets and multi-currency conversion.
- Expenses shared outside one household.

## Current state

Identity & Access authenticates accounts. Household owns household membership, stable member identifiers, administration and ownership. A user can belong to multiple households and inactive memberships are retained for historical references. No expense model or tables currently exist.

## Proposed design

```text
HTTP
  -> Expense application use case
      -> HouseholdExpenseAccessPort
          -> Household application query
      -> Expense aggregate
      -> ExpenseRepository
          -> JPA/PostgreSQL adapter
```

The package is organized as:

```text
ncasa.expense
├── domain
├── application
└── infrastructure
```

Input adapters own transaction boundaries for the MVP. Application and domain remain plain Java.

## Domain impact

### Aggregate root

`Expense` contains:

- `ExpenseId`
- `HouseholdRef`
- creator and payer `MemberRef`
- `Money` total
- `ExpenseDescription`
- economic `LocalDate`
- `ExpenseSplit` with concrete `ExpenseAllocation` values
- `ExpenseStatus`
- `ExpenseSource`
- audit timestamps and optimistic-lock version

### Invariants

- A confirmed expense total is strictly positive.
- Every allocation is strictly positive and uses the expense currency.
- A member occurs at most once in a split.
- A split contains at least one allocation.
- Allocation amounts sum exactly to the expense total.
- Creator, payer and participants are active members when the expense is created.
- The payer does not have to be one of the participants.
- Confirmed expenses are voided, never physically deleted.
- A voided expense cannot be voided again.

### Equal splitting

Equal splitting works in the currency's minor units. Participants are ordered by member identifier and remainder units are assigned from the beginning of that order. For example, EUR 10.00 shared between three members becomes 3.34, 3.33 and 3.33 deterministically.

### Lifecycle

The initial implemented lifecycle is:

```text
CONFIRMED -> VOIDED
```

`DRAFT` is reserved in the status vocabulary for the next delivery, but this RFC defines no draft API.

## Context boundaries

Expense defines scalar references instead of importing Household domain types. `HouseholdExpenseAccessPort` returns an Expense-owned context containing the actor member, whether the actor is an administrator and the active member identifiers.

An adapter in `expense.infrastructure.household` calls a public Household application query. Expense never accesses Household persistence.

## Authorization

- Every active member can create, retrieve and list expenses in the household.
- The creator can void their own expense.
- A household administrator can void any household expense.
- Creator identity is derived from the authenticated account and is never accepted from the request.
- Payer and participants supplied by clients must be active household members.

## API impact

### Create

```http
POST /api/households/{householdId}/expenses
```

Equal and exact splits use a discriminator named `type`. Monetary values are serialized as decimal strings.

### Detail

```http
GET /api/households/{householdId}/expenses/{expenseId}
```

### History

```http
GET /api/households/{householdId}/expenses?from=&to=&status=&page=&size=
```

The default status is `CONFIRMED`. Results are ordered by economic date, creation time and identifier in descending order.

### Void

```http
POST /api/households/{householdId}/expenses/{expenseId}/void
```

Voiding requires a non-empty reason and returns the updated representation.

## Persistence impact

Flyway creates:

```text
expenses
expense_allocations
```

PostgreSQL stores money as `NUMERIC(19,4)` plus a three-character currency code. Constraints protect positive totals and allocations, valid lifecycle values, unique participants and foreign-key references. Indexes support household history ordered and filtered by date and status.

JPA entities remain separate from domain objects. Optimistic locking protects concurrent changes.

Cross-context foreign keys to Household tables are accepted inside the modular monolith as a second line of data-integrity defence. They do not authorize code-level dependencies on Household infrastructure.

## Security and privacy

- All endpoints require an authenticated JWT.
- Household membership is checked for every operation.
- Logs may include expense and household identifiers, operation and outcome.
- Logs must not include full descriptions, request bodies, tokens, emails or receipt contents.

## Alternatives considered

### Store only the split rule

Rejected because later membership or rule changes could alter historical balances and rounding.

### Use Household domain objects directly

Rejected because it couples two bounded contexts and makes Expense dependent on Household internals.

### Physically delete expenses

Rejected because balances require an auditable historical record and deletion hides corrections.

### Generalize every expense to a polymorphic financial space

Deferred. It would anticipate separated-parent sharing before its rules are known and would weaken relational integrity for the household MVP.

## Trade-offs

Materialized allocations add rows and mapping code, but make accounting reproducible. Separate persistence models add ceremony, but keep the domain framework-free. The MVP does not yet calculate balances, but its data is sufficient for those future read models.

## Testing strategy

- Plain JUnit domain tests for money, splitting and lifecycle invariants.
- Application unit tests with fakes for authorization and orchestration.
- PostgreSQL Testcontainers integration tests for mapping, queries, constraints and optimistic locking.
- Web/integration tests for contracts and security.
- One end-to-end flow from authentication and household membership to expense creation, history and voiding.
- ArchUnit rules for inward dependencies and bounded-context isolation.

## Migration / rollout plan

The migration only creates new tables. No existing data changes. The API is additive.

## Observability and operations

Structured logs record identifiers and outcomes. Metrics for created, voided and failed operations are intentionally deferred until the project introduces an application metrics boundary.

## Open questions

None blocking the MVP. Drafts, categorization, debt settlement, expense plans and cross-household sharing require later proposals.

## Decision

Accepted. The implementation is recorded by ADR-0003.
