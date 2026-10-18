# AISafe — Project Structure

This repository is a **Maven multi-module** Java project. 

## Top-level layout

```
/
├── aisafe.core/                   # Domain + application layer 
├── aisafe.persistence.impl/       # JPA repositories + persistence wiring
├── aisafe.bootstrappers/          # Seed data 
├── aisafe.app.bootstrap/          # Standalone bootstrap runner 
├── aisafe.app.backoffice.console/ # Console UI 
├── db/                            # Local H2 database files 
├── docs/                          # Documentation and diagrams
├── Scripts/                       # Helper scripts
├── pom.xml                        # Root Maven aggregator
└── readme.md                      # Project overview / getting started
```

## Modules (what goes where)

### `aisafe.core`
**Business logic** and use cases, independent from persistence/UI.

- **Domain model**: entities/aggregates and value objects 
- **Repository interfaces** 
- **Application controllers/services** 

**Rule**: this module should not depend on persistence/UI modules.

### `aisafe.persistence.impl`
**Infrastructure persistence** implementations for the repository interfaces in `aisafe.core`.

- JPA repositories 
- Repository factories (JPA vs in-memory)
- `META-INF/persistence.xml` with the JPA provider + DB connection (H2 in development)

### `aisafe.bootstrappers`
**Seed/demo data** used to initialize a fresh database in development.

- Bootstrappers per area
- Usually idempotent logic 

### `aisafe.app.bootstrap`
Executable module used to **run bootstrappers**.

- Main entry point: `aiSafe.app.bootstrap.AisafeBootstrap`
- Configuration file: `src/main/resources/application.properties`

### `aisafe.app.backoffice.console`
Console UI for administrative/backoffice operations.

- Main entry point: `aiSafe.Main`
- Menus/UI: under `aiSafe.app.backoffice.console.presentation.*`
- Configuration file: `src/main/resources/application.properties`

## Persistence Pattern

All the persistence-related classes follow a clear structure:

![Persistence Diagram](UserStories/persistenceDiagram/persistenceDiagram.png)
