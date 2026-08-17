# Package Structure

## Purpose

This document defines the default package organization for the project.

The project uses a hybrid structure:

1. organize primarily by feature or business capability;
2. inside each feature, separate domain, application and infrastructure;
3. inside application, organize use cases by intent when useful;
4. mirror the production package structure under `src/test`.

The structure should evolve with the domain. Do not create empty packages purely to satisfy a template.

---

## Root structure

Recommended high-level layout:

```text
src/
├── main/
│   ├── java/com/ncasa/
│   │   ├── expense/
│   │   ├── household/
│   │   ├── calendar/
│   │   ├── identity/
│   │   └── shared/
│   │
│   └── resources/
│
└── test/
    └── java/com/ncasa/
```

The first organizational decision is:

```text
Which feature owns this code?
```

not:

```text
Is this a controller or repository?
```

---

## Feature structure

A mature feature may use:

```text
expense/
├── domain/
├── application/
└── infrastructure/
```

Not every feature needs every subfolder from day one.

---

## Domain package

Example:

```text
expense/
└── domain/
    ├── Expense.java
    ├── ExpenseId.java
    ├── Money.java
    ├── ExpenseSplit.java
    ├── ExpenseRepository.java
    ├── service/
    │   └── DebtCalculator.java
    ├── event/
    │   └── ExpenseCreated.java
    └── exception/
        └── InvalidExpenseAmount.java
```

Possible responsibilities:

```text
domain/
├── entities and aggregates
├── value objects
├── domain services
├── domain events
├── business exceptions
└── domain-owned repository abstractions
```

Do not create all of these subfolders automatically.

If the feature contains only a few domain classes, keeping them directly under `domain/` is often clearer.

---

## Repository ports

Repository abstractions may live in the domain when they are part of the domain vocabulary and required to work with aggregates.

Example:

```text
expense/domain/ExpenseRepository.java
```

Alternatively, output ports that are primarily application orchestration concerns may live under:

```text
expense/application/port/out/
```

Choose one convention consistently based on ownership.

Do not duplicate the same abstraction in both layers.

---

## Application package

Recommended structure:

```text
expense/
└── application/
    ├── create/
    │   ├── CreateExpenseUseCase.java
    │   ├── CreateExpenseCommand.java
    │   └── CreateExpenseResult.java
    │
    ├── update/
    │   ├── UpdateExpenseUseCase.java
    │   └── UpdateExpenseCommand.java
    │
    ├── delete/
    │   └── DeleteExpenseUseCase.java
    │
    └── list/
        └── ListExpensesUseCase.java
```

The application layer is organized by use case or intent when that improves discoverability.

Prefer:

```text
create/
update/
delete/
list/
```

over a generic package containing a large `ExpenseService`.

---

## Ports inside application

When explicit ports are useful:

```text
application/
└── port/
    ├── in/
    └── out/
```

Example:

```text
application/port/in/CreateExpenseUseCase.java
application/port/out/NotificationSender.java
```

However, do not introduce `port/in` and `port/out` mechanically.

A use case class can itself be the input boundary when an additional interface adds no value.

Use explicit ports for meaningful boundaries.

---

## Infrastructure package

Example:

```text
expense/
└── infrastructure/
    ├── web/
    │   ├── ExpenseController.java
    │   ├── CreateExpenseRequest.java
    │   ├── ExpenseResponse.java
    │   └── ExpenseExceptionHandler.java
    │
    ├── persistence/
    │   ├── JpaExpenseEntity.java
    │   ├── SpringDataExpenseRepository.java
    │   ├── JpaExpenseRepositoryAdapter.java
    │   └── ExpensePersistenceMapper.java
    │
    └── config/
```

Infrastructure contains technology-specific details.

Typical categories:

```text
web
persistence
security
messaging
external
config
```

Only create the categories the feature actually needs.

---

## Web package

A feature-specific web structure may look like:

```text
expense/infrastructure/web/
├── ExpenseController.java
├── CreateExpenseRequest.java
├── UpdateExpenseRequest.java
├── ExpenseResponse.java
└── ExpenseWebMapper.java
```

Keep HTTP-specific contracts in the web adapter.

Do not place HTTP request/response models in the domain.

---

## Persistence package

Recommended example:

```text
expense/infrastructure/persistence/
├── JpaExpenseEntity.java
├── SpringDataExpenseRepository.java
├── JpaExpenseRepositoryAdapter.java
└── ExpensePersistenceMapper.java
```

Responsibilities:

```text
JpaExpenseEntity
→ database representation

SpringDataExpenseRepository
→ Spring Data technical repository

JpaExpenseRepositoryAdapter
→ implementation of the core repository port

ExpensePersistenceMapper
→ conversion between persistence and domain models
```

Do not require all four classes when the use case is simple enough to need fewer.

---

## Cross-feature communication

Avoid directly reaching into another feature's infrastructure.

Bad example:

```text
expense
   ↓
household.infrastructure.persistence
```

Prefer a stable application/domain boundary exposed by the owning feature.

Features should communicate through intentional APIs or abstractions rather than internal implementation details.

---

## Shared package

Possible structure:

```text
shared/
├── domain/
└── infrastructure/
```

Use it only for genuinely shared concepts.

Examples that may eventually belong here:

```text
DomainEvent
Clock abstraction
common identifier base type
```

Do not move something to `shared` merely because two classes currently use it.

---

## Complete example

A larger feature may look like:

```text
src/main/java/com/ncasa/expense/
│
├── domain/
│   ├── Expense.java
│   ├── ExpenseId.java
│   ├── Money.java
│   ├── ExpenseSplit.java
│   ├── ExpenseRepository.java
│   ├── service/
│   │   └── DebtCalculator.java
│   ├── event/
│   │   └── ExpenseCreated.java
│   └── exception/
│       ├── InvalidExpenseAmount.java
│       └── MemberNotInHousehold.java
│
├── application/
│   ├── create/
│   │   ├── CreateExpenseUseCase.java
│   │   ├── CreateExpenseCommand.java
│   │   └── CreateExpenseResult.java
│   ├── update/
│   │   ├── UpdateExpenseUseCase.java
│   │   └── UpdateExpenseCommand.java
│   ├── delete/
│   │   └── DeleteExpenseUseCase.java
│   └── list/
│       ├── ListExpensesUseCase.java
│       └── ExpenseListItem.java
│
└── infrastructure/
    ├── web/
    │   ├── ExpenseController.java
    │   ├── CreateExpenseRequest.java
    │   ├── UpdateExpenseRequest.java
    │   ├── ExpenseResponse.java
    │   └── ExpenseWebMapper.java
    │
    └── persistence/
        ├── JpaExpenseEntity.java
        ├── SpringDataExpenseRepository.java
        ├── JpaExpenseRepositoryAdapter.java
        └── ExpensePersistenceMapper.java
```

---

## Test structure

Tests mirror production packages.

Example:

```text
src/test/java/com/ncasa/expense/
│
├── domain/
│   ├── ExpenseTest.java
│   ├── MoneyTest.java
│   ├── ExpenseSplitTest.java
│   └── service/
│       └── DebtCalculatorTest.java
│
├── application/
│   ├── create/
│   │   └── CreateExpenseUseCaseTest.java
│   ├── update/
│   │   └── UpdateExpenseUseCaseTest.java
│   └── list/
│       └── ListExpensesUseCaseTest.java
│
└── infrastructure/
    ├── web/
    │   └── ExpenseControllerTest.java
    └── persistence/
        └── JpaExpenseRepositoryAdapterIT.java
```

Reusable test support can live under:

```text
src/test/java/com/ncasa/support/
├── fake/
└── builder/
```

Examples:

```text
support/fake/FakeExpenseRepository.java
support/builder/ExpenseTestBuilder.java
```

Only introduce these packages when repeated test setup justifies them.

---

## New feature example

If a new capability called `receipt` is introduced, do not immediately create:

```text
receipt/
├── domain/
│   ├── model/
│   ├── service/
│   ├── event/
│   └── exception/
├── application/
│   ├── port/in/
│   └── port/out/
└── infrastructure/
    ├── web/
    ├── persistence/
    ├── config/
    └── external/
```

Start with the minimum structure required.

For example:

```text
receipt/
├── domain/
│   └── Receipt.java
├── application/
│   └── importreceipt/
│       └── ImportReceiptUseCase.java
└── infrastructure/
    └── web/
        └── ReceiptController.java
```

Add packages only as responsibilities appear.

---

## Naming

Use names from the business language.

Prefer:

```text
CreateExpenseUseCase
ExpenseRepository
DebtCalculator
HouseholdMember
```

Avoid vague names such as:

```text
ExpenseManager
ExpenseHelper
CommonUtils
DataProcessor
GeneralService
```

Package names should describe business capability or architectural responsibility.

---

## Placement decision tree

When creating a class, use this sequence:

```text
1. Which feature owns it?
        ↓
2. Is it a business concept or rule?
        ├── yes → domain
        ↓
3. Does it orchestrate a use case?
        ├── yes → application
        ↓
4. Does it interact with framework, HTTP, DB or external system?
        ├── yes → infrastructure
```

Then ask:

```text
Does this need a subpackage?
```

Only create one when it improves cohesion or discoverability.

---

## Anti-patterns

Avoid this global organization:

```text
controller/
├── ExpenseController
├── HouseholdController
└── CalendarController

service/
├── ExpenseService
├── HouseholdService
└── CalendarService

repository/
├── ExpenseRepository
├── HouseholdRepository
└── CalendarRepository
```

Avoid massive feature services:

```text
ExpenseService
├── create
├── update
├── delete
├── list
├── calculate
├── notify
├── import
└── export
```

Avoid unnecessary package depth such as:

```text
expense/domain/model/entity/core/aggregate/Expense.java
```

Package depth should communicate structure, not ceremony.

---

## Final rule

Prefer:

```text
feature
    ↓
domain / application / infrastructure
    ↓
use-case or technical subpackage only when useful
```

The structure should make it easy to answer:

- where a business rule lives;
- where a use case lives;
- where a technical adapter lives;
- where its tests live.

If finding a feature requires searching through global technical folders, the package structure is probably drifting away from the intended architecture.
