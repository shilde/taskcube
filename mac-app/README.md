# Cube menu bar app (macOS)

Native macOS menu bar app that connects directly to the [cube](../cube/)
over WebSocket and, whenever the active face changes, shows both a system
notification and a small animated HUD pop-up (like the volume/brightness
overlay) in the top-right corner. Same protocol and reconnect behavior as
the [Python receiver](../receiver/wuerfel_receiver.py), reimplemented in
Swift so it can run as a proper background app with no Python/Terminal
needed.

## Requirements

- macOS 13+ (for the SF Symbols die-face glyphs used in the HUD)
- Swift toolchain (Xcode Command Line Tools: `xcode-select --install`)

## Build & run

1. Read the cube's IP from the ESP32's Serial Monitor and set it in
   [`Sources/CubeMenuBar/Config.swift`](Sources/CubeMenuBar/Config.swift):
   ```swift
   static let cubeURL = URL(string: "ws://192.168.178.113:81")!
   ```
2. Build the `.app` bundle:
   ```bash
   ./build.sh
   ```
3. Launch it:
   ```bash
   open CubeMenuBar.app
   ```
   A 🎲 icon appears in the menu bar (no Dock icon). On first launch, macOS
   asks for **notification permission** and **local network permission**
   (needed to reach the cube on your LAN) — allow both, or you won't see
   face-change alerts and the WebSocket connection will silently fail with
   "Local network prohibited".

The app isn't code-signed with a Developer ID, only ad-hoc signed by
`build.sh` (needed so macOS keeps treating it as the same app across
rebuilds and doesn't re-prompt for notification permission every time). If
Gatekeeper complains on first launch, right-click the app → *Open*.

## What it does

- Connects to `ws://<cube-ip>:81`, same `hello` / `face_change` /
  `heartbeat` protocol as the Python receiver (see
  [`../cube/README.md`](../cube/README.md#websocket-protocol))
- Auto-reconnects every 5 s if the connection drops
- Menu bar item shows the current face; the dropdown menu repeats it and
  has a Quit item
- On every `face_change` (not on the initial `hello`, since that's just the
  state on connect, not a user-triggered change), both:
  - posts a macOS notification, and
  - shows a translucent HUD pop-up (`HUDController`/`HUDPanel`) with a
    colored die-face icon (SF Symbols `die.face.1.fill` … `die.face.6.fill`,
    one accent color per face — see `FaceStyle` in `Config.swift`) that
    springs in near the top-right corner and auto-dismisses after ~1.6 s

## Not included (yet)

- No per-face duration tracking (the Python receiver's `FaceTracker` — see
  [`../receiver/README.md`](../receiver/README.md)) — this app only alerts
  on change, it doesn't measure or log time spent per face
- No IP auto-discovery (mDNS/UDP) — same limitation as the receiver
- No persistence/backend integration

## Related directories

- [`../cube/`](../cube/) — ESP32 firmware, source of the events
- [`../receiver/`](../receiver/) — terminal-based Python client with
  duration tracking
- [`../backend/`](../backend/) — planned backend for persistence and API
