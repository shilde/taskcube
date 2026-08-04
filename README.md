# TaskCube

A physical time-tracking cube: turn the cube to the side that matches your
current activity — and the change is logged automatically. Instead of an app
that needs constant attention, the cube sits on your desk and a single flip
is enough to switch tasks.

## Idea

```
┌──────────┐  WebSocket (port 81)  ┌─────────────┐    ?    ┌──────────┐
│   Cube   │ ────────────────────▶ │  Receiver   │ ──────▶ │ Backend  │
│  (ESP32) │                       │  (Python)   │         │ (TBD)    │
└──────────┘                       └─────────────┘         └──────────┘
```

- The **cube** uses an accelerometer to detect which side is up and emits
  every face change as a JSON event over WebSocket.
- The **receiver** connects to the cube, logs face changes in the terminal,
  and measures how long each side has been active.
- The **backend** (not implemented yet) will persist these events, aggregate
  them, and expose an API for a future frontend.

## Current status

| Component | Status |
|-----------|--------|
| Cube firmware (ESP32) | Working — face detection, debouncing, haptics, WebSocket server, Wi-Fi fallback to AP mode |
| Receiver (Python)     | Working as a debug/display tool — no persistence |
| macOS menu bar app    | Working — notifies on face change, no duration tracking |
| Backend               | Early scaffold — Kotlin/Spring Boot REST API (face mappings, sessions + stats) with Swagger docs, backed by an in-memory placeholder store; single-cube scope, real persistence and ingestion path still open |

## Components

### [`cube/`](cube/) — ESP32 firmware
Arduino firmware for the cube itself (MPU-6050 + optional DRV2605L).
Detects face changes with a configurable threshold and debounce time and
exposes a WebSocket server on port 81. Details, hardware, pinout, and
WebSocket protocol: see [cube/README.md](cube/README.md).

### [`receiver/`](receiver/) — Python WebSocket client
Connects to the cube, translates events into readable log lines, and tracks
the cumulative time per face. Auto-reconnect, mapping
"face number → human-readable name" (`FACE_NAMES`) configurable. Details and
example output: see [receiver/README.md](receiver/README.md).

### [`mac-app/`](mac-app/) — macOS menu bar app
Native Swift app that connects directly to the cube over WebSocket and
posts a system notification on every face change. No Dock icon, no
per-face duration tracking (see [receiver](receiver/) for that). Details:
see [mac-app/README.md](mac-app/README.md).

### [`backend/`](backend/) — persistence + API (early scaffold)
Kotlin/Spring Boot REST API for face mappings and sessions (`face`,
`start`, `end`, `duration`), with per-day/week stats and Swagger/OpenAPI
docs at `/swagger-ui.html`. Scoped to the single physical cube — no cube
registration/id in the API. Storage is currently an in-memory
placeholder — nothing survives a restart yet. Open questions (real
database, whether the cube talks to the backend directly or via the
receiver, auth): see [backend/README.md](backend/README.md).

## Quickstart

1. **Flash the cube** — see [cube/README.md](cube/README.md) (libraries,
   Wi-Fi credentials in `timetracker.ino`, board ESP32). The IP address
   shows up in the Serial Monitor.
2. **Start the receiver** — enter the IP in [receiver/wuerfel_receiver.py](receiver/wuerfel_receiver.py),
   then `pip install websockets && python3 wuerfel_receiver.py`.
3. Turn the cube to a different side — the change is logged in the terminal.

## Roadmap

- [ ] Don't hardcode Wi-Fi credentials in the firmware (onboarding portal in
      AP mode or `secrets.h`)
- [ ] mDNS (`wuerfel.local`) instead of a hardcoded IP
- [ ] Persistent logging in the receiver (CSV/JSONL/SQLite)
- [ ] Decide on the backend architecture (cube → backend directly vs.
      cube → receiver → backend)
- [ ] Backend MVP: API + persistence
- [ ] Frontend / dashboard
- [ ] 3D-printed enclosure for the cube
