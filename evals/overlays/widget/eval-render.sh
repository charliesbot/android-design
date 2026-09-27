#!/usr/bin/env bash
# Renders the eval screen offscreen (no emulator) into ./renders/. Run from the project root.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf renders && mkdir -p renders
./gradlew --no-daemon -q :app:testDebugUnitTest --tests 'com.example.evalapp.EvalWidgetRender' --rerun
ls renders
