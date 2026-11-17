#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="$(python3 -c 'import json, sys; print(json.load(open(sys.argv[1]))["version"])' "$ROOT/plugin/plugin.json")"
JAR_SRC="$ROOT/app/app/build/libs/android-design-preview.jar"
CACHE_DIR="$HOME/.cache/android-design-preview/$VERSION"

(cd "$ROOT/app" && ./gradlew :app:workerJar --console=plain)
mkdir -p "$CACHE_DIR"
cp "$JAR_SRC" "$CACHE_DIR/android-design-preview.jar"

agy plugin validate "$ROOT/plugin"
agy plugin install "$ROOT/plugin"
