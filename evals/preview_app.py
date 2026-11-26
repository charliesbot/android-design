"""Locates preview CLI, Gradle bootstrap paths, and preview server lifecycle helpers."""
from contextlib import contextmanager
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import threading
import time

ROOT = Path(__file__).resolve().parent.parent
REPO_APP = ROOT / "app"
CLI_BIN = ROOT / "plugin/skills/android-design-explore/scripts/android-design-preview"
SUPERVISOR_BIN = CLI_BIN
PLUGIN_MANIFEST = ROOT / "plugin/plugin.json"

EXECUTABLE_ENV = "ANDROID_DESIGN_PREVIEW"
SRC_ENV = "ANDROID_DESIGN_PREVIEW_SRC"
BOOTSTRAP_DIR_ENV = "ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR"
SERVER_CP_ENV = "ANDROID_DESIGN_PREVIEW_SERVER_CP"
CACHE_DIR_ENV = "ANDROID_DESIGN_CACHE_DIR"
CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
JAVA_ENV = "ANDROID_DESIGN_PREVIEW_JAVA"
NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
SERVER_NAME = "android_design_preview"

DEFAULT_STOP_TIMEOUT = 60


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


def build_cli_env(*, src=None, bdir=None, cache_dir=None, base_env=None):
    """Build environment variables for invoking android-design-preview device-free."""
    env = dict(os.environ if base_env is None else base_env)
    env[NO_OPEN_ENV] = "1"
    if src is not None:
        env[SRC_ENV] = str(src)
    if bdir is not None:
        bpath = Path(bdir)
        env[BOOTSTRAP_DIR_ENV] = str(bpath)
        env.pop(SERVER_CP_ENV, None)
        if (bpath / "server.cp").is_file():
            env[SERVER_CP_ENV] = str(bpath / "server.cp")
    if cache_dir is not None:
        env[CACHE_DIR_ENV] = str(cache_dir)
    return env


def _active_record_path(cache_dir):
    root = Path(cache_dir) if cache_dir is not None else (Path.home() / ".cache" / "android-design-preview")
    return root / "servers" / "active.json"


def _read_active_server_pid(cache_dir):
    record_path = _active_record_path(cache_dir)
    if not record_path.is_file():
        return None
    try:
        data = json.loads(record_path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None
    if not isinstance(data, dict):
        return None
    pid = data.get("pid")
    if not isinstance(pid, int) or isinstance(pid, bool):
        return None
    if pid <= 0 or pid in (os.getpid(), os.getppid()):
        return None
    return pid


def _pid_alive(pid):
    try:
        os.kill(pid, 0)
        return True
    except ProcessLookupError:
        return False
    except PermissionError:
        return True


def _wait_pid_exit(pid, timeout_seconds, poll_interval):
    deadline = time.monotonic() + max(0.0, timeout_seconds)
    while True:
        if not _pid_alive(pid):
            return True
        if time.monotonic() >= deadline:
            return False
        time.sleep(min(poll_interval, max(0.0, deadline - time.monotonic())))


def _terminate_pid(pid, *, grace_seconds=10.0, kill_wait_seconds=2.0, poll_interval=0.1):
    """Send SIGTERM (for JVM shutdown hook cleanup) then SIGKILL if still alive."""
    if not isinstance(pid, int) or isinstance(pid, bool) or pid <= 0 or pid in (os.getpid(), os.getppid()):
        return False
    try:
        os.kill(pid, signal.SIGTERM)
    except ProcessLookupError:
        return True
    except PermissionError:
        return False
    if _wait_pid_exit(pid, grace_seconds, poll_interval):
        return True
    try:
        os.kill(pid, signal.SIGKILL)
    except ProcessLookupError:
        return True
    except PermissionError:
        return False
    return _wait_pid_exit(pid, kill_wait_seconds, poll_interval)


def stop_preview_server(
    work_dir,
    binary,
    *,
    src=None,
    bdir=None,
    cache_dir=None,
    timeout=DEFAULT_STOP_TIMEOUT,
    grace_seconds=10.0,
    kill_wait_seconds=2.0,
    poll_interval=0.1,
):
    """Stop the preview server for work_dir/cache_dir. Returns True when confirmed stopped."""
    env = build_cli_env(src=src, bdir=bdir, cache_dir=cache_dir)
    cli_ok = False
    try:
        done = subprocess.run(
            [str(binary), "stop", str(work_dir)],
            cwd=work_dir,
            env=env,
            capture_output=True,
            text=True,
            timeout=timeout,
            stdin=subprocess.DEVNULL,
        )
        cli_ok = done.returncode == 0
    except (OSError, subprocess.TimeoutExpired):
        cli_ok = False

    if cli_ok:
        return True

    pid = _read_active_server_pid(cache_dir)
    if pid is None:
        return False
    return _terminate_pid(
        pid,
        grace_seconds=grace_seconds,
        kill_wait_seconds=kill_wait_seconds,
        poll_interval=poll_interval,
    )


@contextmanager
def preview_server_session(
    work_dir,
    binary,
    *,
    src=None,
    bdir=None,
    cache_dir=None,
    cache_prefix="eval",
    stopper=None,
    environ=None,
):
    """Provide a cache dir and guarantee preview server shutdown on normal exit, exception, or signal."""
    env_map = os.environ if environ is None else environ
    explicit_cache = cache_dir if cache_dir is not None else env_map.get(CACHE_DIR_ENV)
    created_cache = not explicit_cache
    active_cache_dir = (
        Path(explicit_cache).resolve()
        if explicit_cache
        else (
            Path.home()
            / ".cache"
            / "android-design-preview"
            / f"{cache_prefix}-{os.getpid()}-{int(time.time())}"
        ).resolve()
    )
    active_cache_dir.mkdir(parents=True, exist_ok=True)

    previous_handlers = {}
    on_main_thread = threading.current_thread() is threading.main_thread()
    if on_main_thread:
        def _handle_termination(signum, _frame):
            raise SystemExit(128 + signum)

        for sig_name in ("SIGTERM", "SIGHUP"):
            sig = getattr(signal, sig_name, None)
            if sig is not None:
                previous_handlers[sig] = signal.getsignal(sig)
                signal.signal(sig, _handle_termination)

    stop_fn = stop_preview_server if stopper is None else stopper
    try:
        yield active_cache_dir
    finally:
        # Shield cleanup from repeated termination signals so stop can finish cleanly.
        cleanup_saved = {}
        if on_main_thread:
            for sig_name in ("SIGINT", "SIGTERM", "SIGHUP"):
                sig = getattr(signal, sig_name, None)
                if sig is not None:
                    cleanup_saved[sig] = signal.getsignal(sig)
                    signal.signal(sig, signal.SIG_IGN)
        try:
            stopped = bool(
                stop_fn(
                    work_dir,
                    binary,
                    src=src,
                    bdir=bdir,
                    cache_dir=active_cache_dir,
                )
            )
            if created_cache:
                if stopped:
                    shutil.rmtree(active_cache_dir, ignore_errors=True)
                else:
                    pid = _read_active_server_pid(active_cache_dir)
                    print(
                        f"Warning: preview server may still be running (pid={pid}); "
                        f"preserving cache dir {active_cache_dir}",
                        file=sys.stderr,
                    )
        finally:
            for sig, prev in cleanup_saved.items():
                signal.signal(sig, prev)
            for sig, prev in previous_handlers.items():
                signal.signal(sig, prev)
