# Shopping lists

Shopping lists are household resources under `/api/households/{householdId}/shopping-lists`. The API supports active
and trashed list queries, list lifecycle, nested item CRUD, purchase/reopen, pending reorder and removal of all
purchased items. See [RFC-0007](architecture/rfc/RFC-0007-shopping-list-bounded-context.md) for the complete contract.

Optional units are `UNIT`, `KILOGRAM`, `GRAM`, `LITER`, `MILLILITER`, `PACKAGE` and `OTHER`. `OTHER` requires a
custom label. Quantity is optional and accepts a positive value with up to three decimals.

Active names are compared case-insensitively after trimming and collapsing whitespace. Restoring a name conflict
adds the first free numeric suffix (`_2`, `_3`, …). Trashed lists are read-only and lose calendar links.

Clients should poll detail every 15 seconds and the active-list collection every 60 seconds while visible. Both
resources expose an ETag; clients send it through `If-None-Match`, preserve their current state on `304`, and issue
both requests immediately when visibility returns. Creating an item returns both the item and the authoritative
list summary so clients never infer `contentRevision`. The MVP has no prices, expense integration, notifications,
offline behavior or real-time transport.
