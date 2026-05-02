# Cube tracker (ESP32 firmware)

Firmware for the time-tracking cube. An ESP32 uses an accelerometer to detect
which side of the cube is currently facing up and broadcasts every face
change over WebSocket to connected clients (e.g. the
[receiver](../receiver/wuerfel_receiver.py) or, later, the backend).

## Hardware

- **ESP32** (any board with Wi-Fi)
- **MPU-6050** accelerometer/gyro sensor (required)
  - I²C on pin **SDA = 21**, **SCL = 22**
  - Default address `0x68`
- **DRV2605L** haptic driver (optional, for vibration on face change)
  - I²C address `0x5A`
  - If absent: firmware still runs, just without vibration

## Firmware features

- **Face detection** via the axis with the largest acceleration
  (threshold `FACE_THRESHOLD = 7.0 m/s²`)
- **Debouncing**: a new face is only accepted after `STABLE_TIME_MS = 500 ms`
  of stable orientation — prevents flicker while turning
- **Haptic feedback** on every accepted face change
  (effect `1` = Strong Click; alternatives in the code comment)
- **Wi-Fi fallback**: tries the configured home network first, on timeout
  (`WIFI_TIMEOUT_MS = 15 s`) the cube starts its own access point
  `WuerfelTracker` (password `wuerfel1234`)
- **WebSocket server** on port **81**
- **Heartbeat** every 5 s with the current face and number of connected clients

## WebSocket protocol

All messages are JSON, sent from the cube to the clients.

| Type          | When                                  | Fields |
|---------------|---------------------------------------|--------|
| `hello`       | When a new client connects            | `face`, `mode` (`station`/`ap`), `haptic` |
| `face_change` | When a new face is stably detected    | `face` (1–6), `timestamp` (ms since boot) |
| `heartbeat`   | Every 5 s                             | `face`, `timestamp`, `clients` |

Face mapping (axis → face):

| Face | Axis up |
|------|---------|
| 1    | +Z      |
| 2    | -Z      |
| 3    | +X      |
| 4    | -X      |
| 5    | +Y      |
| 6    | -Y      |

## Build & flash

Using the **Arduino IDE** (or `arduino-cli`):

1. Select board: ESP32
2. Install libraries (Library Manager):
   - `Adafruit MPU6050`
   - `Adafruit DRV2605 Library`
   - `Adafruit Unified Sensor`
   - `WebSockets` by Markus Sattler
3. In [`timetracker/timetracker.ino`](timetracker/timetracker.ino), enter your
   Wi-Fi credentials:
   ```cpp
   const char* HOME_SSID     = "...";
   const char* HOME_PASSWORD = "...";
   ```
4. Compile and flash to the ESP32
5. Open the Serial Monitor (115200 baud) — it shows the IP address the
   receiver needs to connect to

## Interaction with the rest of the project

```
┌──────────┐  WebSocket (port 81)  ┌─────────────┐    ?    ┌──────────┐
│   Cube   │ ────────────────────▶ │  Receiver   │ ──────▶ │ Backend  │
│  (ESP32) │                       │  (Python)   │         │ (TBD)    │
└──────────┘                       └─────────────┘         └──────────┘
```

- [`../receiver/wuerfel_receiver.py`](../receiver/wuerfel_receiver.py) connects
  to the cube and logs face changes plus durations to the terminal
- `../backend/` is still empty — see TODOs

## TODO / open items

### Firmware (`timetracker.ino`)
- [ ] **No Wi-Fi credentials in plaintext in the code** — either via
      `secrets.h` (excluded via `.gitignore`) or via an onboarding portal
      in AP mode
- [ ] **Captive portal in AP mode**, so home Wi-Fi credentials can be set
      without re-flashing
- [ ] **Persistence** of the Wi-Fi config in NVS / Preferences
- [ ] **OTA updates** (Arduino OTA or ESP HTTP Update) — currently every
      update requires a USB flash
- [ ] **Sensor calibration**: thresholds depend on the cube's mechanics,
      possibly calibrate per device and store in NVS
- [ ] **Edge orientation handling**: currently `0` (= no face) is reported
      when the cube rests on an edge — that's fine, but it shouldn't be lost
      as a "pause"
- [ ] **mDNS/Bonjour** (`wuerfel.local`) instead of an IP address, so the
      receiver finds the cube automatically
- [ ] **Battery monitoring** (if battery-powered) and low-power mode

### Receiver (`../receiver/wuerfel_receiver.py`)
- [ ] **Don't hardcode the cube's IP** — discovery via mDNS or UDP broadcast
- [ ] **Print a summary on Ctrl-C** (marked as TODO in the code)
- [ ] **Persistent logging** (CSV/SQLite/JSONL), so sessions survive restarts

### Backend (`../backend/`)
- [ ] No backend yet — decisions to make:
      - Should the cube talk to the backend directly, or stay with the
        receiver as a relay?
      - What data/aggregates does the frontend need?
- [ ] Auth concept (multiple cubes per user? shared cubes?)

### Enclosure / hardware
- [ ] 3D-printable model for the cube enclosure
- [ ] Mount/slot for ESP32 + MPU + DRV2605L + battery
- [ ] Accessible charging port
