# RFC-0003 — Posiciones financieras y liquidaciones

Estado: implementado.

Los importes se conservan por moneda y con precisión decimal. Para cada miembro, `expenseNet = paid - allocated` y `currentNet = paid - allocated + settledOut - settledIn`. Un neto positivo representa crédito y uno negativo deuda. Los gastos y liquidaciones anulados quedan excluidos, y cada moneda debe sumar exactamente cero.

El resumen mensual contiene sólo gastos confirmados por `expenseDate`. La deuda actual incluye actividad confirmada hasta la fecha proporcionada por `Clock`. Las sugerencias son deterministas: separan acreedores y deudores, ordenan por importe absoluto e identificador y los emparejan de forma greedy.

Una liquidación es una transferencia confirmada y auditable entre dos miembros históricos del mismo hogar. Debe reducir deuda existente, no puede sobrepagar y sólo puede anularse. Su creación usa aislamiento serializable.

`Idempotency-Key` es un UUID obligatorio, acotado por hogar y creador. Repetir payload devuelve el recurso original; reutilizar la clave con otro payload produce conflicto. La anulación posterior no libera la clave.
