# RFC-0007: Shopping List bounded context

- Status: Implemented
- Created: 2026-09-11
- Authors: nCasa
- Related ADR: ADR-0010

## Summary

Introduce household-owned shopping lists with a deliberately fast capture flow, optional product details, stable
manual ordering, purchase history inside the list, trash lifecycle and an optional link to one calendar series.

## Domain language

- **Shopping List:** named household list in `ACTIVE` or `TRASHED` state.
- **Shopping Item:** separately versioned product in `PENDING` or `PURCHASED` state.
- **Responsible member:** optional active household member expected to buy an item.
- **Calendar link:** optional scalar reference to an active `calendar.seriesId` in the same household.
- **Content revision:** monotonic list counter used by conditional reads and reorder concurrency control.

## MVP scope and rules

Every active household member may create, read and mutate all lists and products in that household. Active list
names are unique after trimming, collapsing whitespace and case-folding. Duplicate product names are allowed.
List, product, note and custom-unit limits are respectively 80, 160, 500 and 30 characters.

Quantity is optional, positive and has at most three decimals. Unit is optional and is one of `UNIT`, `KILOGRAM`,
`GRAM`, `LITER`, `MILLILITER`, `PACKAGE` or `OTHER`; `OTHER` requires its custom label. New and reopened products
go to the end of pending items. Pending items can be reordered; purchased items are ordered by purchase time.

Lists use soft deletion. A trashed list is read-only except for restore and permanent deletion. Trashing removes
the calendar link. A conflicting restore uses the first available `_2`, `_3`, … suffix. Products use confirmed
physical deletion, individually or as a bulk removal of all purchased products.

## Use cases

- List, create, read, rename and configure lists.
- Trash, restore and permanently delete lists.
- Add, edit, delete, purchase and reopen products.
- Reorder the exact current set of pending products.
- Remove all purchased products.
- Reuse all purchased products to prepare the same list for a new shopping cycle.
- Unassign products when a responsible member becomes inactive.
- Unlink a list when the last active revision of its calendar series disappears.

## API

The collection is `/api/households/{householdId}/shopping-lists`. It exposes list CRUD/lifecycle plus nested item
create, update, delete, purchase, reopen, reorder and purchased-cleanup operations. Mutations carry aggregate
versions; reorder and purchased cleanup carry `contentRevision`. Validation is `400`, denied access `403`, an
inaccessible resource `404`, and uniqueness or concurrency conflict `409`.

`POST /{listId}/items/reuse-purchased` accepts `contentRevision` and returns the complete updated detail with an
ETag. Current pending products keep their order; purchased products are appended by `purchasedAt` ascending. Their
product data and active assignee remain, while purchase audit is cleared. An empty purchased section is a no-op.

Detail reads return an ETag composed from list version and content revision. Ordered collection reads also return
an ETag derived from every visible list ID, version and content revision. `If-None-Match` returns an empty `304`
when the corresponding representation did not change. The frontend polls detail every 15 seconds and the active
collection every 60 seconds only while its route and browser tab are visible, requests both immediately when
visibility returns, and cancels in-flight polling on route teardown. Item creation returns the created item and the
authoritative list summary, including its exact `contentRevision`.

## Persistence and integrations

`shopping_lists` and `shopping_items` are created by V13; PostgreSQL partial ordering indexes are added by V14.
Only the within-context item-to-list relationship has a foreign key and cascade. Household member IDs, identity
audit IDs and calendar series IDs are scalar references without cross-context foreign keys or JPA relationships.

Household access and active assignees are checked through a port. Calendar series validity is checked through a
port. Calendar lifecycle and Household membership lifecycle invoke Shopping List application ports synchronously.
Audit IDs remain after a person leaves even though their active assignment is removed. Position allocation for new
or reopened pending products locks the list row for the duration of allocation, preventing duplicate tail positions
without coupling independent item edits to the list's optimistic metadata version.

## Security and operations

Authorization always comes from authenticated account membership and the household path. Logs contain action,
result and identifiers only; product names and notes are excluded.

## Excluded and future work

Prices, expenses, budgets, categories, templates, notifications, offline mode, WebSocket/SSE and live cursors are
outside this MVP. Reuse is manual, does not create a copied list or historical shopping cycle, and is not triggered
by the linked calendar series. A later real-time adapter can replace polling without changing domain rules or REST mutations.
