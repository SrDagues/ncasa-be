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
- Siempre que se vaya a realizar una modificación que no sea un fix y el proyecto esté en la rama `main`, crear una rama nueva a partir de `main` antes de hacer cambios.

For architectural decisions read:
- docs/architecture/architecture.md

For testing and TDD conventions read:
- docs/architecture/testing.md

For package organization read:
- docs/architecture/package-structure.md
