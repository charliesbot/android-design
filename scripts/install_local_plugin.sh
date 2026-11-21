#!/usr/bin/env bash
# Installs this checkout's plugin (both skills and the CLI) into every harness found on PATH:
# Claude Code, Codex, and Antigravity. It builds the previewer JAR and caches it as this version's
# JAR, so every harness runs the local build instead of downloading a release. Rerun it after
# changing plugin/ or app/ to refresh each install.
#
# Usage: ./scripts/install_local_plugin.sh [--skip-build]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLUGIN="$ROOT/plugin"
MARKETPLACE="charliesbot-android-design"
VERSION="$(python3 -c 'import json, sys; print(json.load(open(sys.argv[1]))["version"])' "$PLUGIN/plugin.json")"
CACHE_DIR="${ANDROID_DESIGN_CACHE_DIR:-${ANDROID_DESIGN_PREVIEW_CACHE_DIR:-$HOME/.cache/android-design-preview}}/$VERSION"

if [ "${1:-}" != "--skip-build" ]; then
  (cd "$ROOT/app" && ./gradlew :app:workerJar --console=plain -q)
fi
mkdir -p "$CACHE_DIR"
cp "$ROOT/app/app/build/libs/android-design-preview.jar" "$CACHE_DIR/android-design-preview.jar"
echo "Cached the local JAR as $CACHE_DIR/android-design-preview.jar"

# Agents run the skill's bundled script directly; this link gives your own shell the bare command.
"$PLUGIN/skills/android-design-explore/scripts/android-design-preview" install >/dev/null
echo "Linked android-design-preview into ~/.local/bin for your shell"

installed=()
skipped=()

# Each harness copies the plugin when it installs, so remove any earlier install (local or from
# GitHub, under the same marketplace name) and add this checkout fresh.
if command -v claude >/dev/null 2>&1; then
  claude plugin uninstall "android-design@$MARKETPLACE" >/dev/null 2>&1 || true
  claude plugin marketplace remove "$MARKETPLACE" >/dev/null 2>&1 || true
  claude plugin marketplace add "$ROOT" >/dev/null
  claude plugin install "android-design@$MARKETPLACE" >/dev/null
  installed+=("Claude Code")
else
  skipped+=("Claude Code")
fi

if command -v codex >/dev/null 2>&1; then
  codex plugin remove "android-design@$MARKETPLACE" >/dev/null 2>&1 || true
  codex plugin marketplace remove "$MARKETPLACE" >/dev/null 2>&1 || true
  codex plugin marketplace add "$ROOT" >/dev/null
  codex plugin add "android-design@$MARKETPLACE" >/dev/null
  installed+=("Codex")
else
  skipped+=("Codex")
fi

if command -v agy >/dev/null 2>&1; then
  agy plugin validate "$PLUGIN" >/dev/null
  agy plugin uninstall android-design >/dev/null 2>&1 || true
  agy plugin install "$PLUGIN" >/dev/null
  installed+=("Antigravity")
else
  skipped+=("Antigravity")
fi

echo "Installed android-design $VERSION from $PLUGIN into: ${installed[*]:-nothing}"
if [ "${#skipped[@]}" -gt 0 ]; then
  echo "Not found on PATH, skipped: ${skipped[*]}"
fi
echo "Start a new session in each harness to load the refreshed plugin."
