# Architecture

## Purpose

This document defines the architectural principles used by the project.

The project combines:

- Domain-Driven Design (DDD) for modelling business concepts and rules.
- Clean Architecture for dependency direction and separation of responsibilities.
- Hexagonal Architecture for isolating the core through ports and adapters.
- Vertical Slice Architecture for organizing code primarily by feature or business capability.
- Horizontal layers inside each feature: domain, application and infrastructure.

The goal is not to apply patterns mechanically. The goal is to keep the business model clear, dependencies controlled, infrastructure replaceable, and features easy to evolve and test.

---

## Core architectural model

The main organizational unit is a feature or business capability.

Example:

```text
com.ncasa
├── expense
│   ├── domain
│   ├── application
│   └── infrastructure
├── household
│   ├── domain
│   ├── application
│   └── infrastructure
├── calendar
│   ├── domain
│   ├── application
│   └── infrastructure
└── shared
```

This is a hybrid structure:

```text
                VERTICAL
                   ↓

              EXPENSE
        ┌─────────────────┐
        │ Infrastructure  │
        ├─────────────────┤
        │ Application     │
        ├─────────────────┤
        │ Domain          │
        └─────────────────┘
```

Features are separated vertically, while each feature keeps horizontal responsibility boundaries.

Avoid global structures such as:

```text
controller/
service/
repository/
entity/
dto/
```

when they mix unrelated business capabilities.

---

## Dependency rule

Dependencies must point toward the business core.

```text
Infrastructure
      ↓
Application
      ↓
Domain
```

The domain must not depend on infrastructure.

The domain should not know about:

- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- REST
- JWT
- Jackson
- external APIs

Whenever practical, domain code should be plain Java.

---

## Domain-Driven Design

The domain represents the business problem, not the technical implementation.

Prefer business language such as:

```text
Expense
Household
Member
Money
ExpenseSplit
Debt
CalendarEvent
```

Avoid generic names such as:

```text
Manager
Helper
Utils
CommonService
Processor
DataService
```

unless they describe a real and justified responsibility.

### Entities

Entities should contain business behaviour when appropriate.

Avoid anemic models where business rules are implemented entirely through setters:

```java
expense.setAmount(...);
expense.setPaidBy(...);
expense.setStatus(...);
```

Prefer expressive domain operations:

```java
expense.changeAmount(...);
expense.assignPayer(...);
expense.splitEqually(...);
expense.cancel();
```

Entities should protect their invariants.

### Value Objects

Use Value Objects when they represent meaningful domain concepts.

Examples:

```text
Money
Email
ExpenseId
HouseholdId
Percentage
DateRange
```

A Value Object should normally:

- represent a real domain concept;
- be immutable when practical;
- validate its own invariants;
- compare by value when appropriate.

Do not introduce Value Objects solely to make the architecture look more sophisticated.

### Domain Services

Use a Domain Service only when a business rule:

- does not naturally belong to an Entity;
- does not naturally belong to a Value Object;
- still represents business behaviour.

Example:

```text
DebtCalculator
```

Do not move every operation into `SomethingService`.

### Domain Events

Use Domain Events when something meaningful has happened in the domain and other parts of the system may need to react.

Examples:

```text
ExpenseCreated
MemberAddedToHousehold
ExpenseCancelled
```

Do not create events for every state change.

---

## Application layer

The application layer contains use cases.

Examples:

```text
CreateExpense
UpdateExpense
DeleteExpense
ListExpenses
AddMemberToHousehold
```

Its main responsibility is orchestration.

A use case may:

- load required domain objects;
- invoke domain behaviour;
- coordinate multiple ports;
- define a transaction boundary;
- save resulting state;
- return an application result.

Business rules that belong to the domain should not remain hidden inside application services.

Prefer small use cases over large generic services.

Instead of:

```text
ExpenseService
```

with many unrelated operations, prefer:

```text
CreateExpenseUseCase
UpdateExpenseUseCase
DeleteExpenseUseCase
GetExpenseUseCase
ListExpensesUseCase
```

---

## Hexagonal Architecture

The application communicates with the outside world through ports and adapters.

Conceptually:

```text
HTTP / External Input
        ↓
Input Adapter
        ↓
Application / Use Case
        ↓
Domain
        ↓
Output Port
        ↓
Output Adapter
        ↓
Database / External System
```

### Input ports

Input ports describe operations offered by the application.

Example:

```java
public interface CreateExpenseUseCase {
    CreateExpenseResult execute(CreateExpenseCommand command);
}
```

Do not create an interface for every use case automatically.

If a concrete application class is already a clear boundary and an interface adds no useful abstraction, prefer the simpler design.

### Output ports

Output ports describe capabilities the application needs from the outside world.

Example:

```java
public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(ExpenseId id);
}
```

The core knows the abstraction, not the technology implementing it.

---

## Adapters

### Input adapters

Examples:

- REST controllers
- scheduled jobs
- message consumers
- CLI commands

A REST adapter should mainly perform:

```text
HTTP request
    ↓
request validation
    ↓
application command/query
    ↓
use case
    ↓
HTTP response
```

Controllers must not contain significant domain logic.

### Output adapters

Examples:

- JPA persistence
- external APIs
- email
- file storage
- messaging

A persistence adapter may look like:

```text
infrastructure/persistence/
├── JpaExpenseEntity.java
├── SpringDataExpenseRepository.java
├── ExpensePersistenceMapper.java
└── JpaExpenseRepositoryAdapter.java
```

The adapter implements an output port owned by the application or domain core.

---

## Persistence

Do not automatically use JPA entities as domain entities.

Prefer separating the models when that protects the domain and improves maintainability:

```text
Domain Entity
      ↕
Persistence Mapper
      ↕
JPA Entity
```

Example:

```text
Expense
```

is a business object.

```text
JpaExpenseEntity
```

is a persistence representation.

Avoid leaking annotations such as:

```text
@Entity
@ManyToOne
@Column
```

into the domain unless there is an explicit and documented reason.

---

## DTOs and boundaries

Do not expose domain entities directly as HTTP contracts.

Possible flow:

```text
HTTP Request
    ↓
Application Command
    ↓
Domain Model
    ↓
Application Result
    ↓
HTTP Response
```

However, avoid ceremonial duplication.

Do not create multiple identical objects merely because a pattern says they could exist.

Create a separate type when it protects a real boundary, meaning or contract.

---

## Security

Spring Security, JWT and OAuth2 are infrastructure concerns.

The domain should not depend directly on:

```text
Authentication
SecurityContext
GrantedAuthority
```

If business logic needs to know who performs an operation, pass domain-oriented concepts such as:

```text
UserId
MemberId
Actor
Permission
```

as appropriate.

---

## Exceptions

Keep business errors separate from technical errors.

Business examples:

```text
InvalidExpenseAmount
MemberNotInHousehold
ExpenseAlreadyCancelled
```

Technical examples:

```text
DatabaseConnectionException
ExternalServiceUnavailableException
```

The web adapter is responsible for translating appropriate failures into HTTP responses.

The domain must not know HTTP concepts such as:

```text
400
404
409
```

---

## Transactions

Transaction boundaries normally belong to the application or infrastructure layer.

Do not put technical transaction concepts inside domain entities.

A use case may define the transaction boundary when needed.

---

## Shared code

Use `shared` conservatively.

Do not allow it to become:

```text
shared/
├── Utils
├── Common
├── Helpers
└── Everything
```

Move code to `shared` only when it represents a genuinely shared and stable concept.

Prefer duplication over premature shared abstractions when the concepts are not truly the same.

---

## Avoid overarchitecture

Do not automatically create all of the following for every feature:

```text
interface
implementation
factory
mapper
adapter
command
query
response
service
facade
builder
```

Before adding an abstraction, ask:

1. Does it protect an architectural boundary?
2. Does it represent a real concept?
3. Does it improve testability?
4. Does it reduce meaningful coupling?
5. Is more than one implementation plausible?
6. Does it make the code easier to understand?

If not, choose the simpler design.

---

## Adding a new feature

For every new feature:

1. Identify the business behaviour.
2. Determine which feature or bounded context owns it.
3. Identify relevant entities, Value Objects and invariants.
4. Decide whether application orchestration is needed.
5. Identify external dependencies.
6. Add ports only where a real boundary exists.
7. Implement infrastructure as adapters.
8. Develop behaviour using TDD as described in `testing.md`.
9. Keep dependencies pointing inward.
10. Refactor while keeping tests green.

Do not create a new feature package when the behaviour clearly belongs to an existing feature.

---

## Modifying existing code

Before changing existing code:

1. inspect the current structure;
2. identify the affected feature;
3. locate existing tests;
4. preserve established conventions when sensible;
5. add characterization tests if existing behaviour is not covered;
6. make incremental changes;
7. avoid restructuring unrelated parts of the repository.

A small feature request should not trigger a repository-wide rewrite.

---

## Decision rule

Before implementing code, ask:

```text
Which feature owns this?
        ↓
Is this domain, application or infrastructure?
        ↓
Are dependencies pointing inward?
        ↓
Is a port actually useful here?
        ↓
Can the design be simpler?
```

Priorities:

```text
clear domain > framework convenience
behaviour > ceremony
feature cohesion > global technical folders
inward dependencies > coupling
simplicity > speculative abstraction
```
