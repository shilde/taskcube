# Backend

A Kotlin/Spring Boot scaffold now lives here (see `CLAUDE.md` for the
package layout). It exposes a REST API — documented with
springdoc-openapi/Swagger UI at `/swagger-ui.html` — for face mappings and
sessions. There's only one physical cube, so the API is scoped to that
single device (no cube registration/id anywhere). **Persistence is
currently an in-memory placeholder**: everything is lost on restart. The
architecture and database decisions below are still open; this README
captures what's decided, what's stubbed, and what's still pending.

## Purpose of the backend

The [cube](../cube/) emits `face_change` events over WebSocket. The
[receiver](../receiver/wuerfel_receiver.py) currently only displays them in
the terminal and forgets everything on exit. The backend is meant to close
that gap:

- **Persistence** of face changes as time intervals
  (`face`, `start`, `end`, `duration`)
- **Aggregation** per day / week / project
- **API** for a future frontend (dashboard, reports)
- optionally **mapping** of cube face → activity/project (configurable per user)

## Open architecture decisions

Before any code is written here, the following questions need answers:

### 1. Who talks to whom?

Two options are on the table:

**A) Cube → backend directly**
```
Cube ──WebSocket──▶ Backend ──HTTP──▶ Frontend
```
- Pro: one hop fewer, the receiver becomes redundant
- Con: cube needs internet access + an auth token; backend has to be a
  WebSocket server and handle unstable connections

**B) Cube → receiver → backend**
```
Cube ──WebSocket──▶ Receiver ──HTTP/MQTT──▶ Backend ──HTTP──▶ Frontend
```
- Pro: receiver buffers during backend outages, the cube stays "dumb"
  and LAN-only
- Con: an extra component that has to keep running (Raspberry Pi or similar)

→ **Decision pending.** Leaning towards B), because it keeps the cube
offline-capable and free of any cloud credentials.

### 2. Tech stack

- **Language/framework**: decided — Kotlin + Spring Boot (Spring MVC), see
  `build.gradle.kts` / `CLAUDE.md`
- **Database**: still open. SQLite is enough for single-user; Postgres if
  multi-user. The current API is backed by an in-memory store as a
  placeholder, not a real decision either way
- **API docs**: springdoc-openapi/Swagger — `/swagger-ui.html` and
  `/v3/api-docs`
- **Deployment**: locally on the same Pi as the receiver?
  Cloud (Fly.io / Railway)? Self-hosted (Docker Compose, see `compose.yaml`)?

### 3. Data model (draft)

```
face_mappings  (face, label, project_id?)   -- "face 3 = coding"
sessions       (id, face, start, end)       -- one interval
```

No `cubes`/`users` tables — single cube, single user, by design. If that
ever changes, `cube_id`/`user_id` would need to come back onto both tables.

Open details:
- Should sessions be written live (every `face_change` closes the previous
  session and opens a new one) or only on the next change?
- How to handle `face = 0` (cube on an edge / in motion)?
  Treat it as its own "idle" session, or simply ignore?

### 4. Auth

- Single-user (just me at home) → no auth needed, maybe a static token
- Not a concern unless this becomes multi-user later

## TODO

- [ ] Make the architecture decision A vs. B (see above)
- [x] Choose the tech stack (Kotlin + Spring Boot)
- [ ] Finalize the data model (current model has no `user_id` — single-user
      only)
- [x] First API skeleton — face mappings, sessions + stats, all
      documented via Swagger UI at `/swagger-ui.html` (single-cube, no
      cube registration endpoints)
- [ ] Real persistence (SQLite or Postgres — currently in-memory only,
      lost on restart)
- [ ] Adapt the receiver to push sessions to the backend
      (or the cube firmware, depending on the decision)
- [ ] Adapt the mac-app to read stats from the backend via REST (see
      [`../mac-app/README.md`](../mac-app/README.md))
- [ ] Plan the frontend — once persistence is in place

## Related directories

- [`../cube/`](../cube/) — ESP32 firmware, source of the events
- [`../receiver/`](../receiver/) — current WebSocket client (terminal logger)
