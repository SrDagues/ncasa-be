# Architecture Decision Records (ADR)

## Purpose

An Architecture Decision Record (ADR) documents an architectural decision that has been made and the reasons behind it.

ADRs are the historical memory of the architecture. They explain not only **what** the project does, but **why** a particular option was chosen over the alternatives available at the time.

Use an ADR for decisions that materially affect one or more of the following:

- bounded-context boundaries;
- aggregate design or important domain modelling choices;
- dependency direction;
- persistence strategy;
- authentication and authorization architecture;
- external integrations;
- messaging or asynchronous processing;
- API contracts with long-term impact;
- testing strategy;
- deployment or infrastructure architecture;
- technology choices that are difficult or expensive to reverse.

Do not create an ADR for routine implementation details that can be understood directly from the code.

---

## Relationship with RFCs

An RFC is used **before** a significant or debatable change is accepted.

An ADR is used to record the **decision that was finally taken**.

Typical flow:

```text
Problem / proposal
       ↓
      RFC
       ↓
Discussion and alternatives
       ↓
Decision
       ↓
      ADR
       ↓
Implementation
```

An RFC is not mandatory for every ADR. Small decisions can go directly to an ADR when the trade-offs are already clear.

---

## Location and naming

Individual ADRs should be stored under:

```text
docs/architecture/adr/
```

Recommended naming convention:

```text
ADR-0001-use-modular-monolith.md
ADR-0002-use-jwt-with-refresh-sessions.md
ADR-0003-separate-jpa-model-from-domain.md
```

Numbers are sequential and never reused.

Once an ADR has been committed, preserve it as historical documentation. If a decision changes, create a new ADR that supersedes the old one instead of rewriting history.

---

## Status

Use one of these statuses:

```text
Proposed
Accepted
Deprecated
Superseded by ADR-XXXX
Rejected
```

Most committed architectural decisions will normally be `Accepted`.

---

## ADR template

Copy the following template into a new file under `docs/architecture/adr/`.

```markdown
# ADR-XXXX: <Decision title>

- Status: Proposed | Accepted | Deprecated | Superseded by ADR-XXXX | Rejected
- Date: YYYY-MM-DD
- Owners: <person/team>
- Related RFC: RFC-XXXX (optional)

## Context

Describe the problem, constraints and forces that make this an architectural decision.

Explain the situation before the decision. Avoid describing only the chosen solution.

## Decision

State the decision clearly and concretely.

Use language such as:

> We will ...

The reader should be able to understand the architectural rule without reading the rest of the document.

## Alternatives considered

### Option A: <name>

Describe the option and its relevant advantages and disadvantages.

### Option B: <name>

Describe the option and its relevant advantages and disadvantages.

## Rationale

Explain why the selected option is preferable for the current project and constraints.

Focus on trade-offs rather than claiming that one solution is universally better.

## Consequences

### Positive

- ...

### Negative / trade-offs

- ...

### Risks

- ...

## Implementation constraints

Document rules that implementation must respect.

Examples:

- dependency direction;
- package boundaries;
- required ports/adapters;
- migration constraints;
- compatibility requirements.

## Validation

Explain how the decision will be validated.

Examples:

- unit tests;
- integration tests;
- architecture tests;
- performance measurements;
- production metrics.

## Follow-up

List any known follow-up work or decisions that are intentionally deferred.
```

---

## Example decisions for nCasa

Decisions that are good candidates for ADRs include:

```text
Use a modular monolith before considering microservices.

Organize code by bounded context / feature and use
Domain → Application → Infrastructure dependency direction.

Separate JPA persistence entities from DDD domain objects.

Use JWT access tokens with persistent rotating refresh sessions.

Keep Household roles outside Identity & Access.

Use PostgreSQL and Flyway for persistence and schema evolution.
```

Each of these represents an architectural constraint that future contributors should understand before changing the code.

---

## ADR quality rules

A good ADR should:

- be short enough to read quickly;
- explain the context, not only the solution;
- explicitly mention meaningful alternatives;
- document trade-offs and consequences;
- use project-specific reasoning;
- describe a decision that can be enforced or observed in the code;
- remain useful months or years later.

Avoid ADRs that merely say:

```text
We chose X because X is better.
```

Prefer reasoning such as:

```text
We chose X because it keeps the domain independent from framework-specific
persistence concerns. This adds mapping code, but allows domain tests to remain
plain Java and prevents JPA constraints from shaping aggregate behaviour.
```

---

## Changing an existing decision

Do not silently edit an accepted ADR to make history look consistent with the current architecture.

Instead:

```text
ADR-0002: Accepted
        ↓
new requirements appear
        ↓
ADR-0007: Accepted — supersedes ADR-0002
        ↓
ADR-0002 status becomes:
Superseded by ADR-0007
```

This preserves the reasoning that led to both decisions.
