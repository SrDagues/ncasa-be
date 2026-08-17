- Organizar por feature / vertical slice.
- Dentro de cada feature: domain, application, infrastructure.
- Aplicar DDD al dominio.
- Dependencias: Infrastructure → Application → Domain.
- Domain no depende de Spring/JPA/etc.
- Usar ports/adapters cuando exista una frontera externa.
- Evitar sobrearquitectura.
- Desarrollo preferentemente mediante TDD.
- Tests reflejan la estructura de src/main.
- Domain/Application → unit tests.
- Infrastructure → integration tests.
- PostgreSQL integration tests → Testcontainers.
- Tests orientados a comportamiento.

For architectural decisions read:
- docs/architecture/architecture.md

For testing and TDD conventions read:
- docs/architecture/testing.md

For package organization read:
- docs/architecture/package-structure.md
