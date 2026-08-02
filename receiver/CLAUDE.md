# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

This directory (`receiver/`) is one component of **TaskCube**, a physical
time-tracking cube: an ESP32-based cube reports which face is up over
WebSocket, and this Python receiver logs face changes and measures how long
each face was active.

```
┌──────────┐  WebSocket (port 81)  ┌─────────────┐    ?    ┌──────────┐
│   Cube   │ ────────────────────▶ │  Receiver   │ ──────▶ │ Backend  │
│  (ESP32) │                       │  (Python)   │         │ (TBD)    │
└──────────┘                       └─────────────┘         └──────────┘
```

- `../cube/` — ESP32 firmware (Arduino), the event source (not part of this
  directory's day-to-day work, but see its README for the WebSocket
  protocol this receiver depends on).
- `../backend/` — planned persistence/API layer; does not exist yet.

The entire receiver is currently a **single file**: [wuerfel_receiver.py](wuerfel_receiver.py).
Code comments and log output are in German ("Wuerfel" = "cube"); this is
intentional (author's native language), keep new comments/strings
consistent with that unless asked otherwise.

## Running

```bash
pip install websockets
python3 wuerfel_receiver.py
```

Requires Python ≥ 3.10 (uses `int | None` union syntax). There is no
`requirements.txt` yet — `websockets` is the only dependency.

There are no tests, linter, or build step in this directory currently.

Before running against a real cube, `WUERFEL_URL` (currently hardcoded to
`ws://192.168.178.113:81`) must point at the cube's actual IP, read from the
ESP32's Serial Monitor. There is no discovery mechanism (mDNS/UDP) yet.

## Architecture

- **`FaceTracker`** — the only stateful piece. Tracks `current_face`,
  the timestamp the current face started (`face_since`, from
  `asyncio.get_event_loop().time()`), and cumulative seconds per face
  (`totals`). `face_changed()` closes out the previous face's duration and
  opens the new one; `print_summary()` produces a session summary
  (including time on the currently-active face, computed on demand).
- **`handle_message()`** — dispatches on the WebSocket protocol's `type`
  field (`hello`, `face_change`, `heartbeat`; unknown types are logged
  as-is). This must stay in sync with the protocol the cube firmware emits
  — see `../cube/README.md` for the authoritative message/field
  definitions if the protocol ever changes.
- **`run()`** — owns the reconnect loop: connects, iterates messages via
  `async for raw in ws`, and on `ConnectionRefusedError` /
  `ConnectionClosed` / `WebSocketException` / `OSError` waits 5s and
  retries indefinitely. This is the intended behavior (cube may be
  power-cycled or out of Wi-Fi range) — don't turn transient connection
  errors into fatal exits.
- **`FACE_NAMES`** — maps the protocol's numeric face (1–6, matching the
  cube's axis-based face detection: 1/2 = ±Z, 3/4 = ±X, 5/6 = ±Y) to a
  human-readable label. Meant to be edited per-user/deployment.

### Known structural gap (see TODOs below)

`main()` creates no `tracker`; the real `FaceTracker` instance lives inside
`run()`/`handle_message()`'s scope and is unreachable from the
`KeyboardInterrupt` handler in `main()`. Printing a summary on Ctrl-C
therefore requires restructuring so the tracker (or its final state) is
accessible after `asyncio.run(run())` returns/raises — it isn't a matter of
just calling `print_summary()` in `main()` today.

## Known TODOs (from README, still open)

- No IP discovery (mDNS `wuerfel.local` or UDP broadcast) — IP is hardcoded
- No summary-on-Ctrl-C (see structural gap above)
- No persistent logging (CSV/JSONL/SQLite) — everything is lost on exit
- No backend integration — `../backend/` doesn't exist yet
- No `requirements.txt`
- No systemd service file for continuous operation (e.g. on a Raspberry Pi)
