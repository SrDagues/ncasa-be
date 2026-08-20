# ADR-0002: Household ownership and invitations

## Status

Accepted

## Decision

Use a single transferable household owner in addition to fixed `ADMIN` and `MEMBER` roles. Keep invitations as an independent aggregate and reference Identity & Access accounts only through scalar `AccountId` values.

The creator is recorded for audit but receives no permanent privilege. Ownership transfer promotes the recipient to `ADMIN`; the former owner remains an administrator. A household is archived rather than physically deleted.

## Rationale

This keeps authorization rules inside the Household domain, prevents Identity & Access from owning household-specific roles, preserves historical references, and avoids growing the Household aggregate with invitation lifecycle state.

## Consequences

Accepting an invitation requires a transaction spanning `HouseholdInvitation` and `Household`. Infrastructure must handle optimistic concurrency and enforce database uniqueness as a second line of defence.
