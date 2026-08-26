# Pull request — bounded context de gastos

## Resumen

Este pull request incorpora el bounded context de gastos completo desarrollado durante las etapas 0, 1, 2, 3 y 4.

La solución sigue DDD, Clean Architecture, arquitectura hexagonal, vertical slices y TDD:

```text
Frontend
presentation → application → domain
infrastructure ────────────────▲

Backend
infrastructure → application → domain
```

Cadena habitual de una operación:

```text
Componente Angular
→ Store de presentación
→ Caso de uso frontend
→ Gateway HTTP
→ Controller backend
→ Caso de uso backend
→ Dominio
→ Puerto de persistencia
→ Adaptador JPA/JDBC
```

Ramas revisadas:

- Backend: `expenses`.
- Frontend: `codex/expenses-frontend`.

## Commits principales

### Backend

```text
feat(expenses): add recurring expense plans, forecasts, and transactional outbox
feat(expenses): add categories, percentage splits, drafts, and reclassification
feat(expenses): add balances, debt summaries and settlements
added mvp to expense
```

### Frontend

```text
feat(expenses): add expense plans, forecasts, installments, and reminders
feat(expenses): add categories, percentage splits, drafts, and reclassification
added expenses functions to frontend
added init functions to create, list and delete expenses
```

## 1. Gastos

### Listado

```text
ExpenseListComponent
→ ExpenseListStore.load
→ frontend ListExpensesUseCase.execute
→ HttpExpenseGateway.list
→ GET /api/households/{householdId}/expenses
→ ExpenseController.list
→ backend ListExpensesUseCase.execute
```

Filtros disponibles en el contrato:

- Estado.
- Fecha desde y hasta.
- Pagador.
- Participante.
- Categoría o sin categoría.
- Tipo de reparto.
- Origen manual o planificado.
- Plan de origen.
- Página y tamaño.

Límites:

- Se consulta un estado cada vez.
- Paginación de 20 elementos por defecto.
- No se calculan totales financieros sumando una página parcial.

### Detalle

```text
ExpenseDetailComponent
→ ExpenseDetailStore.load
→ frontend GetExpenseUseCase.execute
→ HttpExpenseGateway.get
→ GET /api/households/{householdId}/expenses/{expenseId}
→ ExpenseController.get
→ backend GetExpenseUseCase.execute
```

Muestra:

- Descripción, importe y fecha económica.
- Estado, pagador y creador.
- Categoría.
- Tipo de reparto y asignaciones materializadas.
- Origen manual o planificado.
- Auditoría y anulación.
- Plan y clave de ocurrencia cuando proceda.

### Creación

```text
ExpenseFormComponent
→ ExpenseFormStore.submit
→ frontend CreateExpenseUseCase.execute
→ HttpExpenseGateway.create
→ POST /api/households/{householdId}/expenses
→ ExpenseController.create
→ backend CreateExpenseUseCase.execute
```

La UI inicial utiliza EUR y permite reparto igual, exacto o porcentual.

### Anulación

```text
ExpenseDetailComponent
→ ExpenseDetailStore.void
→ frontend VoidExpenseUseCase.execute
→ HttpExpenseGateway.void
→ POST /api/households/{householdId}/expenses/{expenseId}/void
→ ExpenseController.voidExpense
→ backend VoidExpenseUseCase.execute
```

Límites:

- Motivo obligatorio entre 1 y 500 caracteres.
- Sólo creador o administrador.
- No existe eliminación física.
- No existe edición completa del gasto confirmado.

### Gastos recientes

```text
DashboardComponent
→ frontend ListRecentExpensesUseCase.execute
→ reutiliza ListExpensesUseCase
→ GET /expenses
```

No existe un endpoint específico. Se muestran como máximo cuatro gastos confirmados.

## 2. Dinero y repartos

### Dinero

- Backend: `Money` basado en `BigDecimal`.
- Frontend: `Money` basado en unidades menores con `bigint`.
- Las cantidades HTTP viajan como cadenas decimales.
- No se utiliza coma flotante para cálculos financieros.
- Cada moneda se mantiene separada.

### Reparto igual

- Frontend: `splitEqually`.
- Backend: split igual dentro del aggregate `Expense`.
- Los participantes se ordenan de forma determinista.
- Los céntimos sobrantes se asignan en ese orden.

### Reparto exacto

- Frontend: `validateExactSplit`.
- Backend: `ExactSplitCommand` y validación del aggregate.
- Participantes únicos.
- Importes positivos.
- Suma idéntica al total.

### Reparto porcentual

- Frontend: `Percentage` y `materializePercentageSplit`.
- Backend: `Percentage`, `PercentageSplitCommand` y materialización del reparto.
- Representación entera en puntos básicos.
- Máximo dos decimales.
- Suma exacta de 100 %.
- Reparto de céntimos por mayor resto y después por identificador.

## 3. Categorías

### Listar

```text
ListExpenseCategoriesUseCase
→ HttpExpenseCategoryGateway.list
→ GET /expense-categories?includeArchived=
→ ExpenseCategoryController.list
→ backend ListExpenseCategoriesUseCase
```

### Crear

```text
CreateExpenseCategoryUseCase
→ HttpExpenseCategoryGateway.create
→ POST /expense-categories
→ ExpenseCategoryController.create
→ backend CreateExpenseCategoryUseCase
```

### Renombrar

```text
RenameExpenseCategoryUseCase
→ HttpExpenseCategoryGateway.rename
→ PUT /expense-categories/{categoryId}
→ ExpenseCategoryController.rename
→ backend RenameExpenseCategoryUseCase
```

### Archivar

```text
ArchiveExpenseCategoryUseCase
→ HttpExpenseCategoryGateway.archive
→ POST /expense-categories/{categoryId}/archive
→ ExpenseCategoryController.archive
→ backend ArchiveExpenseCategoryUseCase
```

Límites:

- Gestión exclusiva para administradores.
- Nombre normalizado de 1 a 80 caracteres.
- Los nombres duplicados producen conflicto.
- Las categorías archivadas continúan visibles en datos históricos.
- No existe restauración.
- No hay categorías anidadas, iconos ni colores configurables.

## 4. Reclasificación

### Cambiar categoría

```text
ExpenseDetailComponent
→ ReclassifyExpenseUseCase
→ HttpExpenseGateway.reclassify
→ POST /expenses/{expenseId}/reclassify
→ ExpenseController.reclassify
→ backend ReclassifyExpenseUseCase
```

### Historial

```text
ListExpenseClassificationHistoryUseCase
→ HttpExpenseGateway.classificationHistory
→ GET /expenses/{expenseId}/classification-history
→ ExpenseController.classificationHistory
→ backend ListExpenseClassificationHistoryUseCase
```

La auditoría contiene categoría anterior, categoría nueva, actor, fecha y motivo.

Límites:

- Sólo se modifica la clasificación.
- No se reescribe el contenido económico del gasto.
- Los informes mensuales históricos utilizan la categoría actual.

## 5. Borradores

### Crear y guardar

```text
ExpenseFormComponent
→ ExpenseFormStore.saveDraft
→ frontend SaveExpenseDraftUseCase.execute
→ HttpExpenseDraftGateway.create/update
→ POST /expense-drafts o PUT /expense-drafts/{draftId}
→ ExpenseDraftController.create/update
→ CreateExpenseDraftUseCase / UpdateExpenseDraftUseCase
```

### Listar y consultar

```text
ListExpenseDraftsUseCase → GET /expense-drafts → ExpenseDraftController.list
GetExpenseDraftUseCase → GET /expense-drafts/{draftId} → ExpenseDraftController.get
```

### Confirmar

```text
SaveAndConfirmExpenseDraftUseCase
→ guarda el snapshot actual
→ HttpExpenseDraftGateway.confirm
→ POST /expense-drafts/{draftId}/confirm
→ ExpenseDraftController.confirm
→ ConfirmExpenseDraftUseCase
```

### Descartar

```text
DiscardExpenseDraftUseCase
→ POST /expense-drafts/{draftId}/discard
→ ExpenseDraftController.discard
→ backend DiscardExpenseDraftUseCase
```

Límites:

- Guardado explícito, sin autoguardado.
- El borrador puede estar incompleto.
- Optimistic locking mediante `version`.
- No se sobrescriben conflictos automáticamente.
- Un borrador confirmado o descartado no vuelve a estar abierto.
- No hay edición colaborativa.

## 6. Balance mensual

```text
FinancialSummaryComponent
→ FinancialSummaryStore
→ frontend GetMonthlyFinancialSummaryUseCase.execute
→ HttpFinancialGateway.getMonthly
→ GET /api/households/{householdId}/financial-summary/monthly?month=YYYY-MM
→ FinancialSummaryController.monthly
→ backend GetMonthlyFinancialSummaryUseCase.execute
```

Incluye, por moneda:

- Total de gastos del hogar.
- Importe pagado por miembro.
- Importe asignado por miembro.
- Neto mensual.
- Desglose por categoría.

Límites:

- Sólo gastos confirmados del mes.
- No incluye liquidaciones.
- No persiste proyecciones.
- No convierte monedas.

## 7. Deuda actual

```text
FinancialSummaryComponent
→ FinancialSummaryStore
→ frontend GetDebtSummaryUseCase.execute
→ HttpFinancialGateway.getDebt
→ GET /api/households/{householdId}/debt-summary
→ FinancialSummaryController.debt
→ backend GetDebtSummaryUseCase.execute
→ DebtCalculator
```

Fórmula:

```text
expenseNet = paid - allocated
currentNet = paid - allocated + settledOut - settledIn
```

Interpretación:

- Neto positivo: debe recibir.
- Neto negativo: debe pagar.
- Neto cero: está saldado.

`DebtCalculator` simplifica transitivamente las obligaciones globales del hogar. Puede sugerir un pago entre miembros que nunca compartieron directamente un gasto.

Límites:

- Calculada hasta el día actual.
- Excluye gastos y liquidaciones anulados.
- Las monedas permanecen separadas.
- El algoritmo greedy es determinista, pero no se presenta como mínimo matemático universal.

La explicación detallada está disponible en [expense-debt-calculation.md](expense-debt-calculation.md).

## 8. Liquidaciones

### Listar y consultar

```text
ListSettlementsUseCase → HttpFinancialGateway.list
→ GET /settlements
→ SettlementController.list
→ backend ListSettlementsUseCase

GetSettlementUseCase → HttpFinancialGateway.get
→ GET /settlements/{settlementId}
→ SettlementController.get
→ backend GetSettlementUseCase
```

### Registrar

```text
SettlementFormComponent
→ SettlementFormStore.create
→ frontend CreateSettlementUseCase.execute
→ HttpFinancialGateway.create
→ POST /settlements + Idempotency-Key
→ SettlementController.create
→ backend CreateSettlementUseCase.execute
```

Reglas:

- Emisor y receptor diferentes.
- Importe positivo.
- La transferencia debe reducir deuda existente.
- No puede superar la deuda del emisor ni el crédito del receptor.
- La creación se ejecuta en aislamiento serializable.
- La clave de idempotencia se acota por hogar y creador.
- Misma clave y payload devuelve la liquidación original.
- Misma clave con payload diferente devuelve `409`.

### Anular

```text
SettlementDetailComponent
→ SettlementDetailStore.void
→ frontend VoidSettlementUseCase.execute
→ HttpFinancialGateway.void
→ POST /settlements/{settlementId}/void
→ SettlementController.voidSettlement
→ backend VoidSettlementUseCase.execute
```

Límites:

- Confirmación inmediata, sin aceptación del receptor.
- No hay edición ni eliminación física.
- No hay reembolsos, ingresos ni pagos externos.
- Sólo creador o administrador puede anular.

## 9. Planes de gastos

### Listar

```text
ExpensePlansComponent
→ ExpensePlansStore.loadList
→ frontend ListExpensePlansUseCase.execute
→ HttpExpensePlanGateway.list
→ GET /expense-plans
→ ExpensePlanController.list
→ backend ListExpensePlansUseCase.execute
```

El contrato soporta filtros por estado, frecuencia, pagador, participante, próxima ocurrencia y página.

La vista actual expone estado, frecuencia y pagador. Participante y rango de próxima ocurrencia están soportados por modelos y gateway, pero no tienen controles visibles.

### Crear

```text
ExpensePlanFormComponent
→ ExpensePlanFormStore.submit
→ frontend CreateExpensePlanUseCase.execute
→ HttpExpensePlanGateway.create
→ POST /expense-plans
→ ExpensePlanController.create
→ backend CreateExpensePlanUseCase.execute
```

Soporta:

- Gasto futuro único.
- Frecuencia semanal.
- Frecuencia mensual.
- Frecuencia anual.
- Fin por fecha inclusiva.
- Fin por número total de ocurrencias.
- Reparto igual, exacto o porcentual.
- Categoría opcional.
- Zona horaria IANA.
- Aviso entre 0 y 30 días.

### Consultar

```text
ExpensePlanDetailComponent
→ ExpensePlanDetailStore.load
→ frontend GetExpensePlanUseCase.execute
→ HttpExpensePlanGateway.get
→ GET /expense-plans/{planId}
→ ExpensePlanController.get
→ backend GetExpensePlanUseCase.execute
```

### Pausar, reactivar y cancelar

```text
PauseExpensePlanUseCase
→ POST /expense-plans/{planId}/pause
→ ExpensePlanController.pause
→ ExpensePlanLifecycleUseCase

ReactivateExpensePlanUseCase
→ POST /expense-plans/{planId}/reactivate
→ ExpensePlanController.reactivate
→ ExpensePlanLifecycleUseCase

CancelExpensePlanUseCase
→ POST /expense-plans/{planId}/cancel
→ ExpensePlanController.cancel
→ ExpensePlanLifecycleUseCase
```

Reglas:

- Sólo creador o administrador gestiona el ciclo de vida.
- `ACTIVE` puede pasar a `PAUSED`, `CANCELLED` o `COMPLETED`.
- `PAUSED` puede pasar a `ACTIVE` o `CANCELLED`.
- `CANCELLED` y `COMPLETED` son terminales.
- Las fechas pasadas durante una pausa se omiten.
- En planes por número, las fechas omitidas no consumen cuotas.

Límites:

- Todos los planes son finitos.
- No existe edición.
- No existen borradores de planes.
- No hay recurrencia cron ni intervalos cada N semanas.
- No se crean gastos anticipadamente.
- El formulario inicial trabaja en EUR.
- Al reutilizar un gasto porcentual, el frontend lo convierte en reparto exacto porque el gasto materializado no devuelve los porcentajes originales.

## 10. Previsión

```text
ExpensePlansComponent
→ ExpensePlansStore.loadForecast
→ frontend ForecastExpensePlansUseCase.execute
→ HttpExpensePlanGateway.forecast
→ GET /expense-plans/forecast?from=&to=
→ ExpensePlanController.forecast
→ backend ForecastExpensePlansUseCase.execute
```

Características:

- Sólo planes activos.
- Totales separados por moneda.
- Ocurrencias ordenadas por fecha y plan.
- Asignaciones materializadas en la respuesta.
- Identifica la última cuota.
- No persiste resultados.

Límites:

- Rango por defecto de 90 días.
- Rango máximo de 12 meses.
- Sin gráficos basados en coma flotante.
- Sin creación anticipada de gastos.

## 11. Materialización automática

No existe llamada frontend directa.

```text
ExpensePlanScheduledJobs.materialize
→ ExpensePlanJobRunner.materialize
→ MaterializeDueExpensePlanUseCase.execute
```

Proceso:

1. Reclama un plan vencido.
2. Comprueba la clave de ocurrencia.
3. Revalida miembros, categoría y reparto.
4. Crea el gasto con origen `PLAN`.
5. Avanza cursor y contador.
6. Completa el plan si es la última ocurrencia.
7. Persiste eventos en el outbox.

Idempotencia:

```text
sourcePlanId + occurrenceKey
```

Una plantilla inválida pausa automáticamente el plan y produce un evento de atención.

## 12. Recordatorios y outbox

### Avisos

```text
ExpensePlanScheduledJobs.reminders
→ ExpensePlanJobRunner.remind
→ PublishDueExpensePlanReminderUseCase.execute
```

Eventos:

- Próxima ocurrencia.
- Última cuota.
- Plan que requiere atención.

### Despacho

```text
ExpensePlanScheduledJobs.dispatch
→ DispatchOutboxMessagesUseCase.execute
```

Características:

- Escritura transaccional junto al aggregate.
- Deduplicación de mensajes.
- Entrega al menos una vez.
- Procesamiento por lotes.
- `FOR UPDATE SKIP LOCKED`.
- Backoff y reintentos.
- Estado final fallido.

Límites:

- No hay Kafka, RabbitMQ ni broker externo.
- No se envían correos ni notificaciones push.
- No se expone el outbox por API.
- El frontend no mantiene leído o descartado.
- La UI sólo muestra avisos contextuales mediante `nextReminderAt`.

## 13. Dashboard

### Resumen financiero

```text
DashboardComponent
→ GetDashboardFinancialSnapshotUseCase
→ GetMonthlyFinancialSummaryUseCase + GetDebtSummaryUseCase
```

Muestra gasto mensual del hogar y saldo del miembro actual, separados por moneda.

### Próximo gasto planificado

```text
DashboardComponent
→ GetDashboardUpcomingExpenseUseCase
→ ForecastExpensePlansUseCase
→ GetExpensePlanUseCase
```

Muestra la primera ocurrencia de los próximos 90 días, su importe, fecha, última cuota y estado contextual del aviso.

## 14. Rutas frontend

```text
/app/expenses
/app/expenses/new
/app/expenses/:expenseId

/app/expenses/drafts
/app/expenses/drafts/:draftId

/app/expenses/categories

/app/expenses/balances

/app/expenses/settlements
/app/expenses/settlements/new
/app/expenses/settlements/:settlementId

/app/expenses/plans
/app/expenses/plans/new
/app/expenses/plans/:planId
```

Navegación local:

```text
Gastos | Borradores | Planes | Balances | Liquidaciones | Categorías
```

`Categorías` sólo aparece para administradores.

## 15. Persistencia y migraciones

```text
V4  esquema inicial de gastos
V5  liquidaciones
V6  categorías, porcentajes y borradores
V7  planes de gasto, ocurrencias y outbox
```

Se mantienen separados:

- Aggregates de dominio.
- Entidades JPA.
- Read models financieros y de previsión.

## 16. Límites globales

No se implementa:

- Edición completa de gastos confirmados.
- Eliminación física.
- Restauración de categorías.
- Conversión o consolidación de monedas.
- Presupuestos.
- OCR o voz.
- Reembolsos e ingresos.
- Aceptación de liquidaciones.
- Edición o recurrencia infinita de planes.
- Entrega real de correos o push.
- Estado leído/descartado de avisos.
- Caché o proyecciones financieras persistidas.

## 17. Verificación

Verificación realizada el 26 de agosto de 2026.

### Frontend

```text
npm test -- --watch=false
```

- 38 archivos de tests superados.
- 128 tests superados.
- Test de arquitectura superado.
- Paridad de traducciones superada.

```text
npm run build
```

- Build de producción correcto.
- Bundle inicial: aproximadamente 411,09 kB.
- Dentro de los presupuestos configurados.

### Backend

```text
./mvnw test
```

- 116 tests superados.
- ArchUnit superado.
- Sin fallos ni errores.

```text
./mvnw verify
```

- Build final correcto.
- Migraciones validadas hasta V7.
- Artefacto Spring Boot generado.

### Reserva de validación

Los cinco tests PostgreSQL/Testcontainers fueron omitidos porque el entorno local no dispone de Docker:

```text
Tests run: 5
Failures: 0
Errors: 0
Skipped: 5
```

La aprobación definitiva debe comprobar que esos tests se ejecutan y quedan verdes en CI con Docker disponible. Cubren repositorios PostgreSQL, constraints y comportamiento dependiente de la base de datos real.

## 18. Checklist de aprobación

- [x] Backend compila.
- [x] Frontend compila en producción.
- [x] Tests unitarios backend verdes.
- [x] Tests frontend verdes.
- [x] Tests de arquitectura verdes.
- [x] Traducciones ES/EN equivalentes.
- [x] Migraciones Flyway validadas.
- [x] Ramas limpias y sincronizadas con sus remotos antes de generar esta documentación.
- [ ] Testcontainers PostgreSQL ejecutados en un entorno con Docker.
- [ ] Revisión manual de los flujos críticos en un entorno integrado.

## Recomendación

El pull request está funcionalmente preparado para revisión. Puede aprobarse cuando CI confirme los tests PostgreSQL/Testcontainers y, preferiblemente, se complete una comprobación manual del flujo:

```text
crear gasto
→ consultar balance
→ registrar liquidación
→ guardar y confirmar borrador
→ reclasificar gasto
→ crear plan recurrente
→ consultar previsión
→ materializar una ocurrencia
→ verificar el gasto generado
```
