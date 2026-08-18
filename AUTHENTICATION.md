# Autenticación de nCasa

## Arquitectura

Identity & Access es un bounded context independiente. Su responsabilidad es registrar cuentas, autenticar credenciales, mantener sesiones de refresh y exponer la identidad del usuario autenticado. No conoce hogares, gastos ni roles de hogar.

La estructura sigue DDD + Clean Architecture + Hexagonal + Vertical Slices:

```text
identityaccess/
├── domain/
├── application/
│   ├── register/
│   ├── login/
│   ├── refresh/
│   ├── logout/
│   ├── session/
│   └── port/out/
└── infrastructure/
    ├── config/
    ├── persistence/
    ├── security/
    └── web/
```

El dominio es Java puro. `UserAccount` representa la cuenta; `AuthSession` representa una sesión de refresh. `Email`, `UserId`, `PasswordHash` y `RefreshTokenHash` son value objects. Las clases JPA son representaciones de persistencia y no forman parte del dominio.

Los roles `ROLE_USER` y `ROLE_ADMIN` son roles globales de cuenta y se conservan por compatibilidad. Los futuros roles `OWNER`, `MEMBER`, etc. pertenecen al bounded context Household y no deben añadirse al modelo de Identity & Access.

## Persistencia

Se mantiene el esquema existente para evitar una migración de datos innecesaria:

- `users`: cuenta interna.
- `user_roles`: roles globales de cuenta.
- `auth_identities`: identidad local y contraseña hasheada; queda preparada para proveedores OAuth futuros.
- `refresh_tokens`: sesiones de refresh, expiración, revocación y trazabilidad de rotación.

Los adaptadores JPA implementan los puertos definidos por `application` y reconstruyen los agregados de dominio.

## Seguridad

La contraseña se almacena con BCrypt mediante `SpringPasswordHasher`; el dominio solo conoce `PasswordHash`.

Los access tokens son JWT HS256 de corta duración. El adaptador JWT incluye `sub`, `userId`, `email`, `roles`, `iat` y `exp`.

Los refresh tokens son valores aleatorios de 48 bytes. Nunca se almacenan en claro: se persiste SHA-256. En cada refresh se bloquea la fila, se valida la sesión, se emite una sesión nueva y se revoca la anterior apuntando a su sustituta.

Para el cliente web, el access token se devuelve en JSON y se mantiene únicamente en memoria. El refresh token se transporta en la cookie `ncasa_refresh`, marcada como `HttpOnly`, con `SameSite=Lax` y limitada a `/api/auth`. JavaScript no puede leer esta cookie.

## Endpoints

Los contratos HTTP se mantienen:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

`register`, `login` y `refresh` devuelven:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "roles": ["ROLE_USER"]
  }
}
```

Además, las tres respuestas incluyen:

```http
Set-Cookie: ncasa_refresh=...; Path=/api/auth; Max-Age=2592000; HttpOnly; SameSite=Lax
```

`refresh` no recibe body: lee la cookie, rota la sesión y reemplaza la cookie. La cookie anterior deja de ser válida inmediatamente.

`logout` tampoco necesita body. Revoca la sesión asociada cuando existe y devuelve siempre `204 No Content` con una cookie expirada, por lo que es idempotente:

```http
Set-Cookie: ncasa_refresh=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Lax
```

## Configuración

Variables principales:

```text
DB_URL=jdbc:postgresql://localhost:5432/ncasa
DB_USERNAME=ncasa_user
DB_PASSWORD=...
JWT_SECRET=...                 # mínimo 32 caracteres
CORS_ALLOWED_ORIGINS=http://localhost:4200
REFRESH_COOKIE_SECURE=false     # true en producción con HTTPS
```

Las duraciones siguen configurándose con `security.jwt.access-token-expiration` y `security.jwt.refresh-token-expiration`.

La configuración de la cookie está bajo `app.auth.refresh-cookie`: nombre, ruta, `SameSite`, indicador `Secure` y duración. CORS acepta credenciales únicamente para los orígenes explícitos de `CORS_ALLOWED_ORIGINS`; no debe combinarse con `*`.

## Flujo web con curl

Registrar una cuenta, si todavía no existe:

```bash
curl -c cookies.txt \
  -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Iniciar sesión y guardar la cookie fuera de JavaScript:

```bash
curl -c cookies.txt \
  -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Usar el `accessToken` recibido para acceder a un endpoint protegido:

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

Rotar la sesión y obtener un access token nuevo:

```bash
curl -b cookies.txt -c cookies.txt \
  -X POST http://localhost:8080/api/auth/refresh
```

Cerrar la sesión y eliminar la cookie:

```bash
curl -b cookies.txt -c cookies.txt \
  -X POST http://localhost:8080/api/auth/logout
```

## OAuth futuro

OAuth se añadirá como infraestructura y como nuevos casos de uso (`oauth-login`, `link-provider`, `unlink-provider`). El proveedor externo no se convertirá en una dependencia del dominio. `UserAccount` seguirá siendo la identidad interna estable de nCasa.

Un futuro cliente móvil reutilizará los mismos casos de uso y podrá incorporar otro adaptador de entrada con un transporte apropiado. No se crean ahora endpoints móviles, metadatos de dispositivo ni migraciones específicas.

## Pruebas

Los tests de dominio y aplicación deben ser unitarios y no levantar Spring. Las pruebas HTTP/persistencia verifican los adaptadores y conservan los escenarios de registro, login, JWT, refresh, rotación, revocación y endpoints protegidos.
