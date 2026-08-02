# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is the backend for **TaskCube** — a physical time-tracking cube (ESP32) that emits `face_change` events over WebSocket. The backend will persist these events as timed sessions and expose an API for a future frontend. The backend is in early scaffolding phase; the data model and architecture are not yet finalized.

The full system: `Cube (ESP32) → Receiver (Python) → Backend (this) → Frontend`

## Tech stack

- **Language**: Kotlin
- **Framework**: Spring Boot 4.1.0 with Spring MVC (`spring-boot-starter-webmvc`)
- **Build tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Java toolchain**: Java 21
- **JSON**: Jackson with `jackson-module-kotlin`
- **Docker Compose**: `compose.yaml` (currently empty, add services as needed — Spring Boot auto-starts it in dev via `spring-boot-docker-compose`)

## Commands

```bash
# Run the application
./gradlew bootRun

# Build
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "de.shcreative.taskube.TaskubeApplicationTests"

# Run a single test method
./gradlew test --tests "de.shcreative.taskube.TaskubeApplicationTests.contextLoads"
```

## Package structure

All code lives under `de.shcreative.taskube` in `src/main/kotlin/`. The entry point is `TaskubeApplication.kt`.

## Planned data model

```
users          (id, name, ...)
cubes          (id, user_id, name, mac/secret)
face_mappings  (cube_id, face, label, project_id?)
sessions       (id, cube_id, face, start, end)
```

A `session` is one interval where a specific cube face was up. Face `0` means the cube is on an edge / in motion (behavior TBD: ignore or treat as idle).

## Open decisions (do not implement without asking)

- **Ingestion path**: Option A (Cube → Backend directly via WebSocket) vs. Option B (Cube → Receiver → Backend via HTTP/MQTT). Currently leaning toward B.
- **Database**: SQLite for single-user, Postgres for multi-user.
- **Auth**: Static token (single-user) vs. OAuth/magic link (multi-user).
