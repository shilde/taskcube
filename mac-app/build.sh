#!/bin/bash
# Builds CubeMenuBar and wraps it into a double-clickable .app bundle.
set -euo pipefail
cd "$(dirname "$0")"

swift build -c release

APP="CubeMenuBar.app"
rm -rf "$APP"
mkdir -p "$APP/Contents/MacOS"
cp ".build/release/CubeMenuBar" "$APP/Contents/MacOS/CubeMenuBar"
cp "Info.plist" "$APP/Contents/Info.plist"

# Ad-hoc sign so macOS treats it as a stable identity across rebuilds
# (needed for the notification permission grant to stick).
codesign --force --deep --sign - "$APP"

echo "Built $APP — run with: open $APP"
