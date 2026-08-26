# Cálculo y simplificación de deudas en un hogar

## Enfoque general

Las deudas se calculan como posiciones globales dentro del hogar, no como deudas independientes entre parejas de miembros.

Esto permite simplificar cadenas de deuda y explica por qué una persona puede terminar pagando a otra con la que nunca compartió directamente un gasto.

## Ejemplo con tres personas

El hogar está formado por Ana, Bruno y Carla.

### Primer gasto

Ana paga 60 € para Ana y Bruno. El gasto se reparte a partes iguales:

| Miembro | Pagado | Asignado | Neto del gasto |
| --- | ---: | ---: | ---: |
| Ana | 60 € | 30 € | +30 € |
| Bruno | 0 € | 30 € | -30 € |
| Carla | 0 € | 0 € | 0 € |

Después de este gasto, Bruno debe 30 € a Ana.

### Segundo gasto

Bruno paga 60 € para Bruno y Carla. También se reparte a partes iguales:

| Miembro | Pagado | Asignado | Neto del gasto |
| --- | ---: | ---: | ---: |
| Ana | 0 € | 0 € | 0 € |
| Bruno | 60 € | 30 € | +30 € |
| Carla | 0 € | 30 € | -30 € |

Después de este gasto, Carla debe 30 € a Bruno.

### Posición acumulada

Al combinar ambos gastos:

| Miembro | Cálculo | Neto acumulado |
| --- | --- | ---: |
| Ana | +30 € | +30 € |
| Bruno | -30 € + 30 € | 0 € |
| Carla | -30 € | -30 € |

El significado de las posiciones es:

- Ana debe recibir 30 €.
- Bruno está saldado.
- Carla debe pagar 30 €.

## Simplificación de las transferencias

Sin simplificación serían necesarias dos transferencias:

```text
Carla → Bruno: 30 €
Bruno → Ana:   30 €
```

Como la posición final de Bruno es cero, ambas se sustituyen por una sola transferencia:

```text
Carla → Ana: 30 €
```

Ana nunca pagó directamente un gasto para Carla. Aun así, esta transferencia es correcta porque cancela la cadena de deuda sin cambiar la posición económica final de ningún miembro.

## Fórmula utilizada

La posición actual de cada miembro se calcula por moneda:

```text
expenseNet = paid - allocated
currentNet = paid - allocated + settledOut - settledIn
```

Donde:

- `paid`: gastos confirmados pagados por el miembro.
- `allocated`: parte materializada de los gastos que le corresponde.
- `settledOut`: dinero enviado mediante liquidaciones confirmadas.
- `settledIn`: dinero recibido mediante liquidaciones confirmadas.

Interpretación:

- `net > 0`: el miembro debe recibir dinero.
- `net < 0`: el miembro debe pagar dinero.
- `net = 0`: el miembro está saldado.

La suma de todas las posiciones de una moneda debe ser exactamente cero.

## Algoritmo de sugerencias

`DebtCalculator` calcula las transferencias sugeridas de la siguiente manera:

1. Separa las posiciones por moneda.
2. Separa los miembros acreedores de los deudores.
3. Ordena ambos grupos por importe absoluto y después por identificador de miembro.
4. Empareja deudores y acreedores mediante un algoritmo greedy.
5. En cada emparejamiento utiliza el mínimo entre la deuda pendiente y el crédito pendiente.
6. Continúa hasta que todas las posiciones quedan saldadas.

El orden determinista garantiza que los mismos datos producen siempre las mismas sugerencias.

El algoritmo busca simplificar las transferencias, pero no se presenta como una solución matemáticamente mínima para todos los casos posibles.

## Efecto de las liquidaciones

Cuando se registra una liquidación:

- El dinero enviado aumenta la posición del emisor, reduciendo lo que debe pagar.
- El dinero recibido reduce la posición del receptor, reduciendo lo que debe recibir.
- La suma total de posiciones continúa siendo cero.

Si Carla paga 30 € a Ana en el ejemplo anterior:

| Miembro | Neto anterior | Efecto de la liquidación | Neto final |
| --- | ---: | ---: | ---: |
| Ana | +30 € | -30 € recibidos | 0 € |
| Bruno | 0 € | 0 € | 0 € |
| Carla | -30 € | +30 € enviados | 0 € |

Si la liquidación se anula, deja de intervenir en el cálculo y se restauran las posiciones anteriores.

## Reglas adicionales

- Sólo intervienen gastos confirmados.
- Los gastos anulados no intervienen.
- Sólo intervienen liquidaciones confirmadas.
- Las liquidaciones anuladas no intervienen.
- La deuda actual incluye actividad con fecha menor o igual al día actual.
- Los importes se calculan de forma exacta, sin coma flotante.
- Las monedas se calculan de forma independiente.
- No se convierten ni se suman monedas diferentes.
- Los miembros inactivos pueden conservar deuda histórica y participar en una liquidación.

## Balance mensual frente a deuda actual

El balance mensual y la deuda actual tienen objetivos diferentes:

### Balance mensual

- Incluye únicamente gastos confirmados del mes consultado.
- No incluye liquidaciones.
- Muestra cuánto pagó y cuánto se asignó a cada miembro durante ese mes.

### Deuda actual

- Acumula todos los gastos confirmados hasta hoy.
- Incluye las liquidaciones confirmadas.
- Representa cuánto debe pagar o recibir actualmente cada miembro.
- Produce las transferencias sugeridas para saldar el hogar.

## Consecuencia del modelo

El sistema no conserva una obligación bilateral rígida del tipo “Carla debe exactamente a Bruno por esta compra”. Conserva la posición financiera global de cada miembro dentro del hogar.

Este enfoque:

- Reduce el número de transferencias necesarias.
- Permite compensar deudas transitivas.
- Mantiene exactamente la misma posición económica final.
- Puede sugerir un pago entre personas que nunca compartieron directamente un gasto.

Si se quisiera impedir ese comportamiento, sería necesario un modelo bilateral que mantuviera cada deuda entre parejas. Ese modelo produciría normalmente más transferencias y no permitiría cancelar cadenas de deuda como `Carla → Bruno → Ana`.
