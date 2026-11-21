"""Finds the preview CLI executable and worker JAR used by the runner and agents under test."""
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CLI_BIN = ROOT / "plugin/skills/android-design-explore/scripts/android-design-preview"
SUPERVISOR_BIN = CLI_BIN
PLUGIN_MANIFEST = ROOT / "plugin/plugin.json"
REPO_JAR = ROOT / "app/app/build/libs/android-design-preview.jar"
EXECUTABLE_ENV = "ANDROID_DESIGN_PREVIEW"
JAR_ENV = "ANDROID_DESIGN_PREVIEW_JAR"
NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
SERVER_NAME = "android_design_preview"


def _plugin_version():
    try:
        return json.loads(PLUGIN_MANIFEST.read_text(encoding="utf-8")).get("version") or "0.5.0"
    except (OSError, ValueError):
        return "0.5.0"


def app_binary(environ=None, home=None):
    """The previewer CLI executable: $ANDROID_DESIGN_PREVIEW, then the explore skill's bundled scripts/android-design-preview."""
    environ = os.environ if environ is None else environ
    override = environ.get(EXECUTABLE_ENV)
    if override:
        if not Path(override).is_file():
            raise RuntimeError(f"{EXECUTABLE_ENV} points to {override}, which is not a file")
        return Path(override)
    if CLI_BIN.is_file():
        return CLI_BIN
    raise RuntimeError(
        f"The preview CLI is missing at {CLI_BIN}. "
        f"Verify the checkout or set {EXECUTABLE_ENV}."
    )


def worker_jar(environ=None, home=None):
    """The preview JAR: $ANDROID_DESIGN_PREVIEW_JAR, repo build output, then ~/.cache."""
    environ = os.environ if environ is None else environ
    home = Path.home() if home is None else Path(home)
    override = environ.get(JAR_ENV)
    if override:
        if not Path(override).is_file():
            raise RuntimeError(f"{JAR_ENV} points to {override}, which is not a file")
        return Path(override)
    cached = home / ".cache/android-design-preview" / _plugin_version() / "android-design-preview.jar"
    candidates = [REPO_JAR, cached]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    searched = "\n".join(f"  {c}" for c in candidates)
    raise RuntimeError(
        "The preview worker JAR is not built. Run `./gradlew :app:workerJar` from app/, or set "
        f"{JAR_ENV} to its path. Looked in:\n{searched}"
    )
