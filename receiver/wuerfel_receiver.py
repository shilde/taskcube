#!/usr/bin/env python3
"""
Wuerfel-Tracker: WebSocket-Empfaenger fuer den ESP32-Wuerfel.

Empfaengt Seitenwechsel-Events und zeigt sie im Terminal an.
Misst dabei, wie lange jede Seite aktiv war.
"""

import asyncio
import json
import sys
from datetime import datetime

import websockets
from websockets.exceptions import ConnectionClosed, WebSocketException

# === KONFIGURATION ===
# IP des Wuerfels - aus dem Serial Monitor ablesen und hier eintragen
WUERFEL_URL = "ws://192.168.178.113:81"

# Optional: Namen fuer die Seiten - personalisier das spaeter nach Belieben
FACE_NAMES = {
    1: "Seite 1 (Z oben)",
    2: "Seite 2 (Z unten)",
    3: "Seite 3 (X oben)",
    4: "Seite 4 (X unten)",
    5: "Seite 5 (Y oben)",
    6: "Seite 6 (Y unten)",
}


def timestamp() -> str:
    """Aktuelle Uhrzeit als HH:MM:SS-String fuer Log-Ausgaben."""
    return datetime.now().strftime("%H:%M:%S")


def format_duration(seconds: float) -> str:
    """Sekundendauer menschenfreundlich formatieren."""
    if seconds < 60:
        return f"{seconds:.1f}s"
    minutes, seconds = divmod(seconds, 60)
    if minutes < 60:
        return f"{int(minutes)}m {int(seconds)}s"
    hours, minutes = divmod(minutes, 60)
    return f"{int(hours)}h {int(minutes)}m"


class FaceTracker:
    """Verfolgt, wann welche Seite wie lange aktiv war."""

    def __init__(self):
        self.current_face: int | None = None
        self.face_since: float | None = None  # Zeitstempel seit aktueller Seite
        self.totals: dict[int, float] = {}    # Face -> kumulierte Sekunden

    def face_changed(self, new_face: int) -> None:
        now = asyncio.get_event_loop().time()

        # Zeit auf der alten Seite aufsummieren
        if self.current_face is not None and self.face_since is not None:
            duration = now - self.face_since
            self.totals[self.current_face] = (
                self.totals.get(self.current_face, 0) + duration
            )
            old_name = FACE_NAMES.get(self.current_face, f"Seite {self.current_face}")
            print(f"[{timestamp()}]   -> {old_name}: {format_duration(duration)}")

        self.current_face = new_face
        self.face_since = now
        new_name = FACE_NAMES.get(new_face, f"Seite {new_face}")
        print(f"[{timestamp()}] Wechsel zu {new_name}")

    def print_summary(self) -> None:
        if not self.totals and self.current_face is None:
            print("Keine Daten erfasst.")
            return

        # Laufende Seite noch mit reinrechnen
        snapshot = dict(self.totals)
        if self.current_face is not None and self.face_since is not None:
            now = asyncio.get_event_loop().time()
            duration = now - self.face_since
            snapshot[self.current_face] = (
                snapshot.get(self.current_face, 0) + duration
            )

        total = sum(snapshot.values())
        print("\n=== Zusammenfassung ===")
        for face in sorted(snapshot.keys()):
            name = FACE_NAMES.get(face, f"Seite {face}")
            secs = snapshot[face]
            pct = (secs / total * 100) if total > 0 else 0
            print(f"  {name}: {format_duration(secs)} ({pct:.1f}%)")
        print(f"  Gesamt: {format_duration(total)}\n")


async def handle_message(tracker: FaceTracker, raw: str) -> None:
    try:
        data = json.loads(raw)
    except json.JSONDecodeError:
        print(f"[{timestamp()}] Unlesbare Nachricht: {raw}")
        return

    msg_type = data.get("type")

    if msg_type == "hello":
        face = data.get("face", 0)
        mode = data.get("mode", "?")
        haptic = data.get("haptic", False)
        print(f"[{timestamp()}] Verbunden mit Wuerfel "
              f"(Modus: {mode}, Haptik: {'ja' if haptic else 'nein'}, "
              f"aktuelle Seite: {face})")
        if face > 0:
            tracker.face_changed(face)

    elif msg_type == "face_change":
        face = data.get("face", 0)
        tracker.face_changed(face)

    elif msg_type == "heartbeat":
        face = data.get("face", 0)
        name = FACE_NAMES.get(face, f"Seite {face}") if face else "keine Seite"
        uptime_ms = data.get("timestamp", 0)
        uptime_s = uptime_ms / 1000
        clients = data.get("clients", "?")
        print(f"[{timestamp()}] Heartbeat "
              f"(aktuell: {name}, Uptime: {format_duration(uptime_s)}, "
              f"Clients: {clients})")

    else:
        print(f"[{timestamp()}] Unbekannter Typ: {raw}")


async def run():
    tracker = FaceTracker()

    while True:
        try:
            print(f"[{timestamp()}] Verbinde mit {WUERFEL_URL} ...")
            async with websockets.connect(WUERFEL_URL) as ws:
                print(f"[{timestamp()}] Verbindung steht.")
                async for raw in ws:
                    await handle_message(tracker, raw)

        except ConnectionRefusedError:
            print(f"[{timestamp()}] Verbindung abgelehnt. Wuerfel online? "
                  f"IP richtig? Warte 5s...")
        except (ConnectionClosed, WebSocketException, OSError) as e:
            print(f"[{timestamp()}] Verbindung verloren ({e}). Reconnect in 5s...")

        await asyncio.sleep(5)


def main():
    tracker = None
    try:
        asyncio.run(run())
    except KeyboardInterrupt:
        print("\n\nAbbruch durch Benutzer.")
        # Zusammenfassung ist schwierig, weil tracker in der async-Funktion lebt -
        # fuer eine erste Version lassen wir sie weg und kommen darauf zurueck,
        # wenn wir persistentes Logging einbauen.
        sys.exit(0)


if __name__ == "__main__":
    main()