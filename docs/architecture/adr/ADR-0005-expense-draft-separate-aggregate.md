# ADR-0005: Model expense drafts as a separate aggregate

- Status: Accepted
- Date: 2026-08-24

## Context

A confirmed `Expense` protects complete accounting invariants. A useful draft must allow missing
description, payer, amount, date or split. Adding nullable state to `Expense` would make every
financial operation understand incomplete objects and would risk drafts entering ledger queries.

## Decision

`ExpenseDraft` is a separate aggregate with `OPEN`, `CONFIRMED` and `DISCARDED` states. It stores a
partial snapshot and optimistic-lock version. Confirmation validates the complete command, creates a
normal `Expense`, and links the draft to it atomically. Repeating confirmation returns that expense.

## Consequences

- Confirmed expense invariants stay unchanged.
- Ledger SQL continues selecting only confirmed expenses.
- Draft persistence contains nullable fields by design.
- Confirmation coordinates two repositories inside one transaction.
- Later OCR or voice capture can populate a draft without changing `Expense`.
