# ADR-0006: Keep current expense classification with an audit trail

- Status: Accepted
- Date: 2026-08-24

## Context

Users need to correct categories on historical expenses. Storing only an immutable category snapshot
would prevent corrections; rewriting the category without evidence would lose accountability.

## Decision

`Expense` keeps its current optional `ExpenseCategoryId`. Every effective reclassification appends an
immutable audit row with previous and new category, actor, timestamp and optional reason. Reports use
the current category. Category archives preserve referenced rows and do not affect accounting values.

## Consequences

- Historical category reports deliberately change after correction.
- Financial balances, debt and settlements remain invariant.
- Reclassification and audit persistence must share a transaction.
- An as-of-classification report would require replaying audit history and is not part of stage 2.
