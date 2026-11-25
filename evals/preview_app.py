"""Locates v6 preview CLI and Gradle bootstrap paths."""
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO_APP = ROOT / "app"
CLI_BIN = ROOT / "plugin/skills/android-design-explore/scripts/android-design-preview"
SUPERVISOR_BIN = CLI_BIN
PLUGIN_MANIFEST = ROOT / "plugin/plugin.json"

EXECUTABLE_ENV = "ANDROID_DESIGN_PREVIEW"
SRC_ENV = "ANDROID_DESIGN_PREVIEW_SRC"
BOOTSTRAP_DIR_ENV = "ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR"
SERVER_CP_ENV = "ANDROID_DESIGN_PREVIEW_SERVER_CP"
BACKEND_ENV = "ANDROID_DESIGN_PREVIEW_BACKEND"
CACHE_DIR_ENV = "ANDROID_DESIGN_CACHE_DIR"
CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
JAVA_ENV = "ANDROID_DESIGN_PREVIEW_JAVA"
NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
SERVER_NAME = "android_design_preview"

DEFAULT_EVAL_BACKEND = "android"


def _plugin_version():
    try:
        return json.loads(PLUGIN_MANIFEST.read_text(encoding="utf-8")).get("version") or "0.5.0"
    except (OSError, ValueError):
        return "0.5.0"


def app_binary(environ=None, home=None):
    """The previewer CLI executable: $ANDROID_DESIGN_PREVIEW, then the explore skill's bundled script."""
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


def preview_src(environ=None, home=None):
    """The v6 Gradle app directory: $ANDROID_DESIGN_PREVIEW_SRC, repo app/, then ~/.cache sparse checkout."""
    environ = os.environ if environ is None else environ
    home = Path.home() if home is None else Path(home)
    override = environ.get(SRC_ENV)
    if override:
        cand = Path(override)
        if (cand / "settings.gradle.kts").is_file():
            return cand
        if (cand / "app" / "settings.gradle.kts").is_file():
            return cand / "app"
        raise RuntimeError(
            f"{SRC_ENV} points to {override}, which does not contain settings.gradle.kts or app/settings.gradle.kts"
        )
    if (REPO_APP / "settings.gradle.kts").is_file():
        return REPO_APP
    cached = home / ".cache/android-design-preview" / _plugin_version() / "src/app"
    if (cached / "settings.gradle.kts").is_file():
        return cached
    raise RuntimeError(
        "The preview app sources were not found. Verify the checkout or set "
        f"{SRC_ENV}."
    )


def bootstrap_dir(environ=None, home=None):
    """The :host:exportBootstrap output directory: $ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR or <src>/host/build/bootstrap."""
    environ = os.environ if environ is None else environ
    override = environ.get(BOOTSTRAP_DIR_ENV)
    if override:
        path = Path(override)
        if not path.is_dir():
            raise RuntimeError(f"{BOOTSTRAP_DIR_ENV} points to {override}, which is not a directory")
        return path
    return preview_src(environ, home) / "host/build/bootstrap"


def _cp_file(filename, env_key, environ=None, home=None):
    environ = os.environ if environ is None else environ
    override = environ.get(env_key)
    if override:
        path = Path(override)
        if not path.is_file() or path.stat().st_size == 0:
            raise RuntimeError(
                f"{env_key} points to {override}, which is missing or empty. "
                "Run `./gradlew :host:exportBootstrap` from app/."
            )
        return path
    bdir = bootstrap_dir(environ, home)
    path = bdir / filename
    if not path.is_file() or path.stat().st_size == 0:
        raise RuntimeError(
            f"Bootstrap classpath file missing at {path}. Run `./gradlew :host:exportBootstrap` from app/, "
            f"or set {SRC_ENV} / {BOOTSTRAP_DIR_ENV}."
        )
    return path


def server_cp(environ=None, home=None):
    """The server @server.cp argfile produced by `:host:exportBootstrap`."""
    return _cp_file("server.cp", SERVER_CP_ENV, environ=environ, home=home)
