# RFC-0001: Household bounded context

## Status

Accepted

## Context

nCasa needs households with membership, administration, ownership transfer and invitations. Household authorization is business behaviour and must not be modelled as global Identity & Access roles.

## Decision

- `Household` is the aggregate root and owns `HouseholdMember` entities.
- Each active household has exactly one transferable owner. The owner is always an `ADMIN`.
- Household roles are the fixed values `ADMIN` and `MEMBER` for the MVP.
- Membership is retained with `ACTIVE`, `LEFT` or `REMOVED` status so historical references remain valid.
- `HouseholdInvitation` is a separate aggregate because it has its own lifecycle.
- Invitations expire after a configurable duration, defaulting to seven days, and persist only a token hash.
- `AccountId` is a Household value object. Household does not import Identity & Access domain types.
- A user may belong to multiple households.

## Consequences

The application layer coordinates changes involving both aggregates in one transaction. Persistence models remain separate from domain objects. Custom roles, dynamic permissions and a complete membership-period audit trail are deferred.

## Validation

Plain Java domain tests protect invariants, application unit tests verify orchestration, PostgreSQL Testcontainers tests validate persistence, and architecture tests enforce dependency direction.
