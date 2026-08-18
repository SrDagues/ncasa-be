# ADR-0001: Transporte de tokens para autenticación web

- Status: Accepted
- Date: 2026-08-18
- Owners: nCasa

## Context

nCasa utiliza access tokens JWT de corta duración y sesiones persistentes con refresh tokens rotatorios. El cliente Angular necesita conservar la sesión al recargar la página sin exponer una credencial duradera a JavaScript. También queremos que un futuro cliente móvil pueda reutilizar el núcleo de autenticación sin depender del mecanismo elegido para el navegador.

## Decision

El cliente web mantendrá el access token únicamente en memoria y lo enviará mediante `Authorization: Bearer`. El backend transportará el refresh token exclusivamente en una cookie `HttpOnly`, limitada a `/api/auth`, con `SameSite=Lax` y `Secure=true` en producción.

Los casos de uso continuarán recibiendo y devolviendo valores de token neutrales. Leer, escribir y expirar cookies será responsabilidad exclusiva del adaptador HTTP web.

Cada refresh rotará la sesión y reemplazará la cookie. Logout revocará la sesión cuando exista y expirará siempre la cookie.

## Alternatives considered

### Guardar ambos tokens en almacenamiento web

Requiere menos cambios en el backend, pero permite que JavaScript lea el refresh token. Una vulnerabilidad XSS podría extraer una credencial válida durante 30 días.

### Guardar ambos tokens en cookies HttpOnly

También evita que JavaScript lea los tokens, pero obliga a diseñar protección CSRF para todas las peticiones autenticadas y cambia el modelo Bearer ya existente.

### Sesión tradicional o Backend for Frontend

Centraliza completamente la sesión en servidor, pero supone un rediseño mayor que no está justificado para la integración actual.

## Rationale

La decisión reduce la exposición del token duradero y conserva el modelo JWT ya implementado. Al aislar la cookie en infraestructura web, dominio y aplicación permanecen independientes del transporte. Un cliente móvil futuro podrá elegir almacenamiento protegido y otro contrato HTTP sin modificar los casos de uso.

## Consequences

### Positive

- JavaScript no puede leer el refresh token.
- El access token no persiste en `localStorage` ni `sessionStorage`.
- La sesión puede restaurarse tras recargar mediante `/api/auth/refresh`.
- La rotación y revocación existentes se reutilizan sin migraciones.
- Un futuro adaptador móvil no queda acoplado a cookies.

### Negative / trade-offs

- El frontend debe restaurar la sesión al arrancar.
- Las peticiones entre orígenes requieren credenciales CORS y una lista explícita de orígenes.
- Producción debe servir la cookie con `Secure=true` y HTTPS.

### Risks

- Un XSS todavía podría usar el access token en memoria mientras la página esté comprometida.
- Una configuración incorrecta de CORS o `Secure` puede impedir el envío de la cookie o ampliar indebidamente los orígenes admitidos.

## Implementation constraints

- Dominio y aplicación no importarán tipos HTTP, cookies ni Spring.
- La respuesta JSON de login y refresh no contendrá `refreshToken`.
- La cookie se configurará mediante `app.auth.refresh-cookie.*`.
- CORS con credenciales nunca utilizará el origen `*`.
- No se crearán endpoints móviles ni metadatos de dispositivo hasta que exista ese cliente.

## Validation

- Tests unitarios para configuración, construcción de cookies y logout.
- Tests HTTP para login, refresh, logout y CORS.
- Pruebas de integración para rotación y ciclo completo de autenticación.

## Follow-up

- Integrar Angular con access token en memoria, restauración de sesión y refresh de ejecución única.
- Definir el transporte y almacenamiento seguro del refresh token cuando se implemente un cliente móvil.
