# Request for Comments (RFC)

## Purpose

A Request for Comments (RFC) is a lightweight design proposal used to discuss a significant technical or architectural change **before committing to a solution**.

RFCs create a place to explain a problem, propose an approach, compare alternatives and collect feedback while the design is still changeable.

Use an RFC when a proposal:

- changes bounded-context boundaries;
- introduces a new major subsystem or integration;
- affects several features or teams;
- changes an important API or persistence model;
- introduces a new infrastructure dependency;
- has meaningful security, performance or operational consequences;
- requires migration or backwards-compatibility work;
- has several reasonable solutions and benefits from discussion;
- is expensive to reverse after implementation.

Do not require an RFC for small refactors, routine bug fixes or local implementation details.

---

## Relationship with ADRs

RFC and ADR serve different moments of a decision.

```text
RFC = proposal and discussion
ADR = final architectural decision and historical record
```

Typical lifecycle:

```text
Draft RFC
   ↓
Review / comments
   ↓
Accepted or rejected
   ↓
If architectural decision is accepted
   ↓
Create ADR
   ↓
Implementation
```

An accepted RFC does not replace an ADR when the result creates a long-lived architectural decision.

---

## Location and naming

Individual RFCs should be stored under:

```text
docs/architecture/rfc/
```

Recommended naming convention:

```text
RFC-0001-household-bounded-context.md
RFC-0002-password-recovery-flow.md
RFC-0003-google-oauth-integration.md
```

Numbers are sequential and never reused.

---

## Status

Use one of these statuses:

```text
Draft
In Review
Accepted
Rejected
Withdrawn
Implemented
```

Suggested lifecycle:

```text
Draft → In Review → Accepted → Implemented
                   ↘ Rejected
Draft → Withdrawn
```

---

## When an RFC is required

An RFC should normally be written before implementation if one or more of these questions cannot be answered trivially:

```text
Does this affect more than one bounded context?

Does this introduce a new architectural dependency?

Will existing clients or persisted data need migration?

Are there several reasonable design alternatives?

Would reversing the decision later be expensive?

Does the change materially affect security, privacy, reliability or performance?
```

If the answer is yes, prefer discussing the design through an RFC before writing production code.

---

## RFC template

Copy the following template into a new file under `docs/architecture/rfc/`.

```markdown
# RFC-XXXX: <Proposal title>

- Status: Draft | In Review | Accepted | Rejected | Withdrawn | Implemented
- Created: YYYY-MM-DD
- Authors: <person/team>
- Reviewers: <optional>
- Related ADRs: <optional>
- Related issues/PRs: <optional>

## Summary

Give a concise description of the proposal and its intended outcome.

A reader should understand the basic idea from this section alone.

## Motivation

Describe the problem to solve and why it matters now.

Include concrete limitations of the current system where possible.

## Goals

- ...
- ...

## Non-goals

Explicitly list things this RFC intentionally does not solve.

- ...
- ...

## Current state

Explain how the system works today.

Include relevant bounded contexts, flows, persistence, APIs or infrastructure.

## Proposed design

Describe the proposed architecture in enough detail to evaluate it before implementation.

Use diagrams or examples when they improve understanding.

Example:

```text
Input adapter
      ↓
Application use case
      ↓
Domain
      ↓
Output port
      ↓
Infrastructure adapter
```

## Domain impact

Describe changes to:

- bounded contexts;
- aggregates;
- entities;
- value objects;
- domain events;
- invariants.

Write `None` if the proposal is purely technical.

## API impact

Describe new, changed or removed endpoints/contracts.

Include backwards-compatibility expectations.

## Persistence impact

Describe:

- schema changes;
- Flyway migrations;
- data migrations;
- indexes;
- compatibility with existing data.

## Security and privacy

Consider at least:

- authentication;
- authorization;
- secrets;
- personal data;
- logging;
- abuse cases;
- token/session handling when applicable.

## Alternatives considered

### Option A: <name>

Advantages:

- ...

Disadvantages:

- ...

### Option B: <name>

Advantages:

- ...

Disadvantages:

- ...

## Trade-offs

Explain what the proposal improves and what complexity or limitations it introduces.

## Testing strategy

Describe how the proposal will be validated using the project's TDD/testing approach.

Possible levels:

```text
Domain unit tests
Application use-case tests
Persistence integration tests
Web/security integration tests
End-to-end tests where justified
```

## Migration / rollout plan

Describe how the change can be introduced safely.

If no migration is required, state that explicitly.

## Observability and operations

Describe relevant logs, metrics, alerts or operational considerations.

Write `None` when not applicable.

## Open questions

- ...
- ...

## Decision

Complete this section after review.

Record whether the RFC was accepted, rejected or superseded and summarize the reason.

If accepted and architectural, reference the ADR that records the final decision.
```

---

## Example RFCs for nCasa

Good RFC candidates include:

```text
Password recovery and email delivery strategy.

Google OAuth login and account-linking behaviour.

How Household roles and permissions should be modelled.

How expenses shared between separated parents and children should cross
Household boundaries.

OCR architecture for creating expenses from receipt images.

Notification architecture for recurring expenses and calendar reminders.

Whether a future subsystem should remain in the modular monolith or be
extracted into a separate service.
```

These topics have multiple valid approaches and can affect several parts of the system, so design discussion is valuable before implementation.

---

## RFC quality rules

A good RFC should:

- begin with the problem, not the preferred technology;
- clearly separate goals from non-goals;
- describe at least the meaningful alternatives;
- make trade-offs explicit;
- identify migration and compatibility concerns;
- include security implications when relevant;
- be concrete enough that reviewers can disagree with specific choices;
- avoid implementing the entire solution before the discussion happens.

The purpose of an RFC is not to prove that the author is right. Its purpose is to make the proposed change understandable enough for the project to make a good decision.

---

## After acceptance

Once an RFC is accepted:

1. create an ADR if the accepted proposal establishes a long-lived architectural decision;
2. reference the RFC from the ADR;
3. implement the change in focused slices/commits;
4. update the RFC status to `Implemented` when complete;
5. do not rewrite the original discussion to hide alternatives or objections.

This gives the project two useful records:

```text
RFC → how the team explored the problem
ADR → what the project finally decided and why
```
