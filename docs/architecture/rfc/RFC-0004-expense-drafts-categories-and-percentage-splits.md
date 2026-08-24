# RFC-0004: Expense drafts, categories and percentage splits

- Status: Implemented
- Date: 2026-08-24
- Related ADRs: ADR-0005, ADR-0006

## Summary

Stage 2 adds private, incomplete expense drafts; household-scoped categories; auditable expense
reclassification; deterministic percentage splits; category filters; and a monthly category breakdown.

## Domain model

`Expense` remains an occurred accounting fact. `ExpenseDraft` and `ExpenseCategory` are separate
aggregates with independent lifecycles. Drafts never enter the financial ledger. Confirming a draft
creates one confirmed expense and materializes its monetary allocations in the same transaction.

Percentage values use hundredths of a percent. They must add up to exactly `100.00`. Monetary units
are assigned with the largest-remainder method, ordered by fractional remainder and then member id.
The resulting allocations, rather than percentages, are persisted on the confirmed expense.

## Draft lifecycle

```text
OPEN -> CONFIRMED
OPEN -> DISCARDED
```

Drafts belong to their creator and remain private even from administrators. They may be incomplete,
but every value that is present is validated. Confirmation revalidates active members and category,
uses optimistic locking, and replays the previously created expense when called again.

## Categories and classification

Only active household administrators create, rename or archive categories. Active members can list
and use them. Existing expenses may remain uncategorized. Archived categories remain attached to
history but cannot be selected for new expenses or drafts.

The creator of an expense or an administrator may reclassify it. Each effective change records the
actor, previous category, new category, optional reason and timestamp. Category reporting uses the
current classification, so correcting an old expense intentionally changes its historical category
breakdown without changing financial balances.

## API

- `GET|POST|PUT /api/households/{householdId}/expense-categories`
- `POST /api/households/{householdId}/expense-categories/{categoryId}/archive`
- `GET|POST|PUT /api/households/{householdId}/expense-drafts`
- `POST /api/households/{householdId}/expense-drafts/{draftId}/confirm`
- `POST /api/households/{householdId}/expense-drafts/{draftId}/discard`
- `POST /api/households/{householdId}/expenses/{expenseId}/reclassify`
- `GET /api/households/{householdId}/expenses/{expenseId}/classification-history`

Expense creation accepts optional `categoryId` and split type `PERCENTAGE`. Expense history accepts
`categoryId`, `uncategorized` and `splitType`. The monthly financial summary exposes category totals
per currency, including an explicit uncategorized group.

## Persistence and rollout

Migration V6 is additive. Existing expenses receive a null category and remain valid. New tables hold
categories, drafts, draft allocations and classification changes. No financial backfill is required.

## Non-goals

Recurring plans, OCR, voice entry, budgets, percentage persistence on confirmed expenses, physical
deletion, category conversion between households and multi-currency conversion remain out of scope.
