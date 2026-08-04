# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is the backend for **TaskCube** — a physical time-tracking cube (ESP32) that emits `face_change` events over WebSocket. The backend persists these events as timed sessions and exposes a REST API (documented via springdoc-openapi/Swagger) for a future frontend, including the macOS menu bar app. Persistence is currently an **in-memory placeholder** — the real database and architecture decisions below are still open.

There is only one physical cube (single-user, single-device), so the API is **not** multi-cube — there's no cube registration/id in any endpoint. If that ever changes, a `Cube` concept and `cubeId` on `Session`/`FaceMapping` would need to come back.

The full system: `Cube (ESP32) → Receiver (Python) → Backend (this) → Frontend / mac-app`

## Tech stack

- **Language**: Kotlin
- **Framework**: Spring Boot 4.1.0 with Spring MVC (`spring-boot-starter-webmvc`)
- **Build tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Java toolchain**: Java 21
- **JSON**: Jackson with `jackson-module-kotlin`
- **API docs**: springdoc-openapi 3.0.3 (`springdoc-openapi-starter-webmvc-ui`) — Swagger UI at `/swagger-ui.html`, raw OpenAPI spec at `/v3/api-docs`
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

- `model/` — `FaceMapping`, `Session` domain classes plus their request DTOs, all annotated with `@Schema` for OpenAPI
- `repository/` — `FaceMappingRepository`, `SessionRepository`: plain in-memory stores (`ConcurrentHashMap`), **not real persistence** — everything is lost on restart
- `controller/` — `FaceMappingController`, `SessionController`: REST endpoints, annotated with `@Tag`/`@Operation`/`@ApiResponse`
- `config/OpenApiConfig.kt` — OpenAPI `Info` bean (title/description/version) shown in Swagger UI

## Current API (in-memory, no persistence yet)

- `GET /api/face-mappings`, `PUT /api/face-mappings/{face}` — map a face (1-6) to a label/project
- `GET/POST /api/sessions`, `GET /api/sessions/{id}`, `POST /api/sessions/{id}/close`, `GET /api/sessions/open` — open/close/list sessions
- `GET /api/stats?range=today|week` — sum duration per face over a period (closed sessions only)

Explore interactively at `/swagger-ui.html` once the app is running (`./gradlew bootRun`).

## Planned data model

```
face_mappings  (face, label, project_id?)
sessions       (id, face, start, end)
```

A `session` is one interval where the cube's face was up. Face `0` means the cube is on an edge / in motion (behavior TBD: ignore or treat as idle). Single-cube, single-user by design — no `cubes` or `users` table. If multi-cube/multi-user support is ever needed, this is the first thing that would need to change (add `cube_id`/`user_id` back to both tables).

## Open decisions (do not implement without asking)

- **Ingestion path**: Option A (Cube → Backend directly via WebSocket) vs. Option B (Cube → Receiver → Backend via HTTP/MQTT). Currently leaning toward B.
- **Database**: SQLite for single-user, Postgres for multi-user.
- **Auth**: Static token (single-user) vs. OAuth/magic link (multi-user).
