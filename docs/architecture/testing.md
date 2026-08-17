# Testing and TDD

## Purpose

This document defines how tests are designed and how Test-Driven Development is applied in the project.

TDD is a development workflow, not another architectural layer.

The architecture remains:

```text
Feature
├── domain
├── application
└── infrastructure
```

Tests support that architecture by validating behaviour at the correct boundary.

---

## TDD cycle

New behaviour should normally be developed using:

```text
RED
 ↓
GREEN
 ↓
REFACTOR
```

### RED

Write a test describing the desired behaviour before implementing it.

Example:

```java
@Test
void shouldRejectExpenseWhenAmountIsNegative() {
    ...
}
```

The test must fail for the expected reason.

### GREEN

Implement the minimum code needed to make the test pass.

Avoid adding speculative behaviour.

### REFACTOR

Once the test passes:

- improve names;
- remove duplication;
- simplify code;
- move misplaced responsibilities;
- extract meaningful Value Objects;
- improve encapsulation;
- remove unnecessary abstractions.

Run the relevant tests after refactoring.

---

## Test behaviour, not implementation

Tests should describe observable behaviour.

Prefer:

```text
shouldRejectExpenseWhenAmountIsNegative
shouldSplitExpenseEquallyBetweenMembers
shouldNotAllowMemberOutsideHousehold
shouldCreateExpenseWhenRequestIsValid
```

Avoid names such as:

```text
testGetter
testSetter
testMethod1
testService
```

Do not create a test merely because a method exists.

Test rules, outcomes and contracts.

---

## Given / When / Then

Prefer the Given / When / Then structure when it improves readability.

```java
@Test
void shouldCreateExpenseWhenDataIsValid() {

    // Given

    // When

    // Then
}
```

Comments are optional. The code itself should remain expressive.

---

## Test pyramid

Use approximately this distribution:

```text
             E2E
             few
              ▲
             / \
            /   \
           /     \
      Integration
         some
            ▲
           / \
          /   \
         /     \
       Unit
       many
```

In this architecture:

```text
Domain
→ many unit tests

Application
→ many unit tests

Infrastructure
→ focused integration tests

Whole system
→ few end-to-end tests
```

---

## Domain tests

Domain tests validate business rules.

They should normally be plain Java tests using JUnit.

Example:

```java
class ExpenseTest {

    @Test
    void shouldRejectNegativeAmount() {
        ...
    }
}
```

Do not use Spring for domain tests.

Avoid:

```java
@SpringBootTest
```

for:

- Entities
- Value Objects
- Domain Services
- pure business rules

Domain tests should run quickly and without:

- database;
- network;
- HTTP server;
- Spring application context.

Examples:

```text
ExpenseTest
MoneyTest
ExpenseSplitTest
DebtCalculatorTest
```

---

## Application tests

Application tests validate use-case orchestration.

Example:

```text
CreateExpenseUseCaseTest
```

They may verify that the use case:

- loads the required aggregate;
- invokes the correct business behaviour;
- saves the resulting state;
- uses external ports correctly;
- returns the expected result;
- handles expected application failures.

Use mocks, stubs or fakes instead of real infrastructure where possible.

Do not start PostgreSQL merely to test application coordination.

---

## Fakes and mocks

Do not use Mockito automatically for every dependency.

### Mock

Use a mock when interaction itself matters.

Example:

```java
verify(expenseRepository).save(any(Expense.class));
```

### Fake

Use a fake when an in-memory implementation makes the test easier to understand.

Example:

```java
class FakeExpenseRepository implements ExpenseRepository {

    private final List<Expense> expenses = new ArrayList<>();

    @Override
    public Expense save(Expense expense) {
        expenses.add(expense);
        return expense;
    }
}
```

Prefer whichever makes the behaviour clearer.

Reusable test doubles may live in:

```text
src/test/java/com/ncasa/support/fake/
```

Examples:

```text
FakeExpenseRepository
FakeHouseholdRepository
```

Do not create a large fake infrastructure before repetition exists.

---

## Test builders and fixtures

When object creation becomes repetitive, introduce focused test builders.

Possible location:

```text
src/test/java/com/ncasa/support/builder/
```

Examples:

```text
ExpenseTestBuilder
HouseholdTestBuilder
MemberTestBuilder
```

Builders should improve readability, not hide important test data.

Prefer explicit values when those values matter to the scenario.

---

## Infrastructure tests

Infrastructure adapters should be tested against the technology they integrate with when practical.

Examples:

```text
JpaExpenseRepositoryAdapterIT
ExternalPaymentClientIT
```

### Persistence

For PostgreSQL persistence tests, prefer Testcontainers with PostgreSQL.

Conceptually:

```text
Test
 ↓
Persistence Adapter
 ↓
JPA / Hibernate
 ↓
PostgreSQL Testcontainer
```

This gives more confidence than replacing PostgreSQL with a database that has different behaviour.

A persistence integration test should validate concerns such as:

- mappings;
- queries;
- constraints;
- conversions between persistence and domain models;
- expected repository behaviour.

Do not repeat every domain rule in persistence tests.

---

## Controller tests

REST controllers can often be tested using:

```text
@WebMvcTest
MockMvc
```

Controller tests should focus on:

- routes;
- HTTP methods;
- status codes;
- request validation;
- serialization;
- deserialization;
- request/response contracts;
- security boundaries when relevant;
- invocation of the application use case.

Example:

```text
ExpenseControllerTest
```

Do not retest all domain behaviour through the controller.

A rule already covered in `ExpenseTest` does not need to be exhaustively repeated at HTTP level.

---

## End-to-end tests

Keep E2E tests limited to critical user flows.

An E2E test may exercise:

```text
HTTP
 ↓
Controller
 ↓
Application
 ↓
Domain
 ↓
Persistence Adapter
 ↓
Database
```

Good E2E candidates include:

- critical authentication flow;
- creating a core business resource;
- a high-value workflow involving several components.

Do not use E2E tests as a substitute for unit tests.

---

## Naming conventions

Unit tests:

```text
ExpenseTest
MoneyTest
CreateExpenseUseCaseTest
```

Integration tests:

```text
JpaExpenseRepositoryAdapterIT
ExpenseControllerIT
```

When Maven is configured accordingly:

```text
mvn test
```

should execute unit tests.

```text
mvn verify
```

should include integration tests.

Do not assume this separation exists until the Maven configuration explicitly supports it.

---

## Test structure

The test source tree should mirror production structure.

Production:

```text
src/main/java/com/ncasa/
├── expense/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── household/
    ├── domain/
    ├── application/
    └── infrastructure/
```

Tests:

```text
src/test/java/com/ncasa/
├── expense/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── household/
    ├── domain/
    ├── application/
    └── infrastructure/
```

This keeps feature tests close conceptually to the production code they validate.

---

## Typical development order

When implementing a business feature:

### 1. Start with the domain when a business rule exists

```text
domain test
    ↓
domain implementation
```

Example:

```text
ExpenseTest
    ↓
Expense
```

### 2. Add the application behaviour

```text
use-case test
    ↓
use-case implementation
```

Example:

```text
CreateExpenseUseCaseTest
    ↓
CreateExpenseUseCase
```

### 3. Add infrastructure only when needed

```text
adapter integration test
    ↓
adapter implementation
```

Example:

```text
JpaExpenseRepositoryAdapterIT
    ↓
JpaExpenseRepositoryAdapter
```

### 4. Add web tests where appropriate

```text
controller test
    ↓
controller implementation
```

This often results in development progressing from the business core outward.

---

## Characterization tests

When changing legacy or insufficiently tested code, do not immediately rewrite it.

First add tests that capture current behaviour when that behaviour must be preserved.

Then refactor incrementally.

This is especially important when:

- the code has unclear responsibilities;
- no existing tests describe its behaviour;
- the requested change touches risky legacy logic.

---

## What not to test

Avoid tests with little behavioural value, such as:

- trivial getters;
- trivial setters;
- framework code already tested by the framework;
- private implementation details;
- exact internal method call sequences unless interaction is the behaviour;
- generated boilerplate.

Tests should protect decisions and behaviour that matter.

---

## Completion criteria

Before considering a change complete:

1. relevant new behaviour has automated coverage;
2. existing relevant tests still pass;
3. domain tests do not require Spring;
4. application tests avoid real infrastructure unless specifically needed;
5. infrastructure integration tests validate real adapter behaviour;
6. no business rule is tested only through a slow E2E path;
7. tests remain readable and behaviour-oriented.

The goal is not maximum test count.

The goal is fast feedback, clear executable specifications and confidence to refactor.
