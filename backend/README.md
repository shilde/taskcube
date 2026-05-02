# Backend (TBD)

This directory is still empty — there is no backend for the cube tracker
yet. This README captures **what is meant to live here** and which
decisions need to be made first.

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

- **Language/framework**: Python (FastAPI), Node (Fastify/Hono), Go (?) — open
- **Database**: SQLite is enough for single-user; Postgres if multi-user
- **Deployment**: locally on the same Pi as the receiver?
  Cloud (Fly.io / Railway)? Self-hosted (Docker Compose)?

### 3. Data model (draft)

```
users          (id, name, ...)
cubes          (id, user_id, name, mac/secret)
face_mappings  (cube_id, face, label, project_id?)   -- "face 3 = coding"
sessions       (id, cube_id, face, start, end)       -- one interval
```

Open details:
- How is a cube assigned to a user (pairing)?
- Should sessions be written live (every `face_change` closes the previous
  session and opens a new one) or only on the next change?
- How to handle `face = 0` (cube on an edge / in motion)?
  Treat it as its own "idle" session, or simply ignore?

### 4. Auth

- Single-user (just me at home) → no auth needed, maybe a static token
- Multi-user → OAuth / magic link / classic login

## TODO

- [ ] Make the architecture decision A vs. B (see above)
- [ ] Choose the tech stack
- [ ] Finalize the data model
- [ ] First API skeleton (health check + `POST /sessions`)
- [ ] Adapt the receiver to push sessions to the backend
      (or the cube firmware, depending on the decision)
- [ ] Plan the frontend — once the API is in place

## Related directories

- [`../cube/`](../cube/) — ESP32 firmware, source of the events
- [`../receiver/`](../receiver/) — current WebSocket client (terminal logger)
