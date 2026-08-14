# Autenticación de nCasa

## Diseño

La cuenta interna (`User`) está separada de sus formas de autenticación (`AuthIdentity`). La primera versión crea una identidad `LOCAL` con contraseña BCrypt; posteriormente se podrá asociar `GOOGLE`, `APPLE` o `MICROSOFT` al mismo usuario sin cambiar los tokens ni la autorización de nCasa. Los roles viven siempre en `User` y nunca se aceptan desde el cliente.

Las piezas principales son:

- `AuthController` expone registro, login, refresh, logout y usuario actual.
- `AuthService` coordina autenticación y emisión de tokens.
- `RefreshTokenService` genera tokens aleatorios, almacena solo SHA-256, rota y revoca bajo transacción.
- `JwtService` emite y valida JWT HS256 con el soporte Nimbus integrado en Spring Security.
- `JwtAuthenticationFilter` valida el Bearer token una vez por petición y carga el usuario vigente.
- `SecurityConfig` configura BCrypt, `AuthenticationManager`, API stateless, CORS, errores 401/403 y method security.
- `GlobalExceptionHandler` normaliza errores de validación, credenciales, refresh token y conflictos.
- `V1__create_auth_schema.sql` crea usuarios, identidades, roles y refresh tokens con sus índices y restricciones.

Se usa la implementación JOSE/Nimbus oficial de Spring Security porque está mantenida junto con la versión de Security del proyecto y evita incorporar otra API JWT. CSRF está desactivado porque esta API no autentica mediante cookies enviadas automáticamente: cada petición protegida requiere `Authorization: Bearer`. CORS acepta únicamente los orígenes configurados.

## Configuración

Variables obligatorias o configurables (véase `.env.example`):

```text
DB_URL=jdbc:postgresql://localhost:5432/ncasa
DB_USERNAME=ncasa_user
DB_PASSWORD=...
JWT_SECRET=...                 # obligatorio, mínimo 32 caracteres
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Las duraciones están en `application.properties`: access token de 15 minutos y refresh token de 30 días. La aplicación falla al arrancar si `JWT_SECRET` falta o es demasiado corto. En producción debe generarse un valor aleatorio independiente (por ejemplo, `openssl rand -base64 48`).

## Flujos

`POST /api/auth/register` normaliza el email, comprueba unicidad, crea `User` con `ROLE_USER`, guarda una identidad local con hash BCrypt y devuelve ambos tokens.

`POST /api/auth/login` delega email y contraseña en `AuthenticationManager`/`UserDetailsService`; si son válidos devuelve un JWT corto y un refresh token aleatorio. El JWT contiene `sub`, `userId`, `email`, `roles`, `iat` y `exp`.

`POST /api/auth/refresh` calcula SHA-256 del valor recibido, bloquea la fila en PostgreSQL, comprueba vigencia, revoca el token anterior y devuelve un access token y refresh token nuevos. `replaced_by_token_id` deja preparada la trazabilidad de la rotación.

`POST /api/auth/logout` requiere Bearer token y revoca el refresh token enviado si pertenece al usuario autenticado. El access token deja de servir al expirar; no se mantiene una blacklist.

## Prueba rápida

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"usuario@example.com","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"usuario@example.com","password":"password123"}'

curl http://localhost:8080/api/auth/me \
  -H 'Authorization: Bearer ACCESS_TOKEN'

curl -X POST http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"REFRESH_TOKEN"}'

curl -X POST http://localhost:8080/api/auth/logout \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"REFRESH_TOKEN"}'
```

## Proteger código nuevo

Todo endpoint fuera de los tres endpoints públicos ya requiere autenticación. Para roles específicos se puede usar:

```java
@PreAuthorize("hasRole('ADMIN')")
```

El principal se obtiene sin aceptar un identificador del cliente:

```java
public UserResponse endpoint(@AuthenticationPrincipal CustomUserDetails user) {
    Long userId = user.id();
    // ...
}
```

En un servicio puede consultarse `SecurityContextHolder`, aunque es preferible pasar desde el controlador solo el identificador obtenido del principal cuando sea posible.
