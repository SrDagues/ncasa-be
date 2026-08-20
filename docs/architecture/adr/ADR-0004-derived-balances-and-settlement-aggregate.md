# ADR-0004 — Balances derivados y aggregate Settlement

Estado: aceptado.

Los balances mensual y acumulado son read models derivados bajo demanda mediante `FinancialLedgerReadPort`. No se persisten como entidades ni proyecciones, evitando sincronización y permitiendo introducir una proyección futura detrás del mismo puerto.

`Settlement` es un aggregate separado de `Expense`: representa una transferencia real, tiene identidad, autorización, auditoría, idempotencia, optimistic locking y transición irreversible `CONFIRMED → VOIDED`. La infraestructura depende de aplicación y dominio; los agregados no conocen Spring, JPA ni Household.
