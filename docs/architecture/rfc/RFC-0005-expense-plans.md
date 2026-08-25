# RFC-0005: Future, recurring and installment expenses

## Semantics

An expense plan has a finite schedule (`ONCE`, `WEEKLY`, `MONTHLY` or `YEARLY`), an IANA time zone and either
an inclusive end date or an occurrence count. Monthly schedules retain their original day and clamp to the
month end. Yearly leap-day schedules clamp to February 28 and restore February 29 in leap years.

Pausing skips elapsed calendar dates. Skipped dates do not consume count-limited installments, so their
calendar extends until every installment is materialized. Reactivation advances the cursor to the first
date on or after the current local day. Plans ending by date complete if no valid occurrence remains.

## Materialization

The scheduler claims due active plans in bounded batches and processes each plan transactionally. It validates
the current household members and category, creates one expense, advances the plan and appends events. An
invalid template pauses the plan and emits `ExpensePlanAttentionRequired`. Transient failures roll back.

The occurrence key is the scheduled local date (`YYYY-MM-DD`). A unique database constraint makes retries and
multiple application nodes safe.

## Reminders and events

Reminder lead time is 0–30 days. The last installment emits `ExpensePlanLastInstallmentApproaching` instead of
the generic approaching event. Events contain identifiers, dates, currency, amount and installment counters;
they contain no description, email or HTTP body.

## Forecast

Forecasts include active plans only, accept at most a 12-month range, materialize allocations in memory and
return totals independently per currency. They are never persisted.
