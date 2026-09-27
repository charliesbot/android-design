#!/usr/bin/env bash
# Renders the eval screen offscreen (no emulator) into ./renders/. Run from the project root.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf renders && mkdir -p renders
./gradlew --no-daemon -q :wear:updateDebugScreenshotTest
for f in wear/src/screenshotTestDebug/reference/com/example/evalapp/wear/EvalWearPreviewsKt/*.png; do
  name=$(basename "$f" | sed -E 's/^EvalWear([A-Za-z0-9]+)_.*/\1/' | tr '[:upper:]' '[:lower:]')
  cp "$f" "renders/$name.png"
done
ls renders
