# Cube receiver (Python)

Small WebSocket client that connects to the [cube](../cube/), logs face
changes in the terminal, and measures how long each face has been active.

Currently a pure debug/display tool — see the [backend README](../backend/README.md)
for the planned next step (persistence + API).

## Features

- Connects to the cube via WebSocket (`ws://<ip>:81`)
- **Auto-reconnect** every 5 s if the connection drops
- Handles the three message types `hello`, `face_change`, `heartbeat`
- Tracks the cumulative time per face (`FaceTracker`)
- Readable log output with timestamp and face name
- Configurable mapping `face number → display name` (`FACE_NAMES`)

## Requirements

- Python ≥ 3.10 (for the `int | None` syntax)
- Package `websockets`

```bash
pip install websockets
```

## Usage

1. Read the cube's IP from the ESP32's Serial Monitor
2. Update the `WUERFEL_URL` constant in
   [`wuerfel_receiver.py`](wuerfel_receiver.py):
   ```python
   WUERFEL_URL = "ws://192.168.178.113:81"
   ```
3. Optionally override `FACE_NAMES` with your own labels
   (e.g. `1: "Coding"`, `2: "Break"`, …)
4. Run:
   ```bash
   python3 wuerfel_receiver.py
   ```
5. Stop with `Ctrl-C`

## Example output

```
[14:32:01] Connecting to ws://192.168.178.113:81 ...
[14:32:01] Connected.
[14:32:01] Connected to cube (mode: station, haptics: yes, current face: 1)
[14:32:01] Switched to face 1 (Z up)
[14:35:42]   -> Face 1 (Z up): 3m 41s
[14:35:42] Switched to face 3 (X up)
[14:35:47] Heartbeat (current: face 3 (X up), uptime: 1m 12s, clients: 1)
```

## TODO

- [ ] **Don't hardcode the IP** — discovery via mDNS (`wuerfel.local`) or
      UDP broadcast, once the firmware supports it
- [ ] **Print a summary on Ctrl-C** (marked as TODO in the code —
      `tracker` currently lives inside the `async` function and isn't
      reachable from the `KeyboardInterrupt` handler)
- [ ] **Persistent logging** as CSV/JSONL/SQLite, so sessions survive
      restarts
- [ ] **Backend integration**: send sessions to the backend via HTTP/MQTT,
      once a backend exists (see [`../backend/README.md`](../backend/README.md))
- [ ] **`requirements.txt`** once more dependencies are added
- [ ] **Service file** (systemd) for continuous operation on a Raspberry Pi

## Related directories

- [`../cube/`](../cube/) — ESP32 firmware, source of the events
- [`../backend/`](../backend/) — planned backend for persistence and API
