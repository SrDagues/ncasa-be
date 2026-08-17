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
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "roles": ["ROLE_USER"]
  }
}
```

## Configuración

Variables principales:

```text
DB_URL=jdbc:postgresql://localhost:5432/ncasa
DB_USERNAME=ncasa_user
DB_PASSWORD=...
JWT_SECRET=...                 # mínimo 32 caracteres
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Las duraciones siguen configurándose con `security.jwt.access-token-expiration` y `security.jwt.refresh-token-expiration`.

## OAuth futuro

OAuth se añadirá como infraestructura y como nuevos casos de uso (`oauth-login`, `link-provider`, `unlink-provider`). El proveedor externo no se convertirá en una dependencia del dominio. `UserAccount` seguirá siendo la identidad interna estable de nCasa.

## Pruebas

Los tests de dominio y aplicación deben ser unitarios y no levantar Spring. Las pruebas HTTP/persistencia verifican los adaptadores y conservan los escenarios de registro, login, JWT, refresh, rotación, revocación y endpoints protegidos.
