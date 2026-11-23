"""Locates v6 preview CLI/bootstrap paths and manages a persistent Robolectric worker JVM."""
from collections import deque
import glob
import json
import os
from pathlib import Path
import queue
import shutil
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
JAR_ENV = "ANDROID_DESIGN_PREVIEW_JAR"
SRC_ENV = "ANDROID_DESIGN_PREVIEW_SRC"
BOOTSTRAP_DIR_ENV = "ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR"
SERVER_CP_ENV = "ANDROID_DESIGN_PREVIEW_SERVER_CP"
ROBOLECTRIC_CP_ENV = "ANDROID_DESIGN_PREVIEW_ROBOLECTRIC_CP"
BACKEND_ENV = "ANDROID_DESIGN_PREVIEW_BACKEND"
CACHE_DIR_ENV = "ANDROID_DESIGN_CACHE_DIR"
CLIENT_TIMEOUT_ENV = "ANDROID_DESIGN_CLIENT_TIMEOUT_MS"
JAVA_ENV = "ANDROID_DESIGN_PREVIEW_JAVA"
NO_OPEN_ENV = "ANDROID_DESIGN_NO_OPEN"
SERVER_NAME = "android_design_preview"

DEFAULT_EVAL_BACKEND = "robolectric"
ROBOLECTRIC_MAIN = "bot.charlies.androiddesign.preview.host.robolectric.RobolectricRunnerMain"


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


def robolectric_cp(environ=None, home=None):
    """The headless Robolectric @robolectric.cp argfile produced by `:host:exportBootstrap`."""
    return _cp_file("robolectric.cp", ROBOLECTRIC_CP_ENV, environ=environ, home=home)


def _java_major(java_path):
    try:
        done = subprocess.run(
            [str(java_path), "-version"],
            capture_output=True,
            text=True,
            stdin=subprocess.DEVNULL,
            timeout=10,
        )
    except (OSError, subprocess.TimeoutExpired):
        return 0
    out = (done.stderr or "") + "\n" + (done.stdout or "")
    for line in out.splitlines():
        if 'version "' in line:
            ver = line.split('version "', 1)[1].split('"', 1)[0]
            if ver.startswith("1."):
                ver = ver[2:]
            head = ver.split(".", 1)[0]
            try:
                return int(head)
            except ValueError:
                return 0
    return 0


def _is_java_17_plus(candidate):
    if not candidate:
        return False
    p = Path(candidate)
    return p.is_file() and os.access(p, os.X_OK) and _java_major(p) >= 17


def java_binary(environ=None):
    """Find Java 17+ for running the Robolectric worker JVM."""
    environ = os.environ if environ is None else environ
    override = environ.get(JAVA_ENV)
    if override:
        if not _is_java_17_plus(override):
            raise RuntimeError(f"{JAVA_ENV} points to {override}, which is not an executable JDK 17+")
        return Path(override)

    java_home = environ.get("JAVA_HOME")
    if java_home:
        cand = Path(java_home) / "bin/java"
        if _is_java_17_plus(cand):
            return cand

    if sys.platform == "darwin" and Path("/usr/libexec/java_home").is_file():
        try:
            done = subprocess.run(
                ["/usr/libexec/java_home", "-v", "17+"],
                capture_output=True,
                text=True,
                stdin=subprocess.DEVNULL,
                timeout=10,
            )
            if done.returncode == 0 and done.stdout.strip():
                cand = Path(done.stdout.strip()) / "bin/java"
                if _is_java_17_plus(cand):
                    return cand
        except (OSError, subprocess.TimeoutExpired):
            pass

    on_path = shutil.which("java", path=environ.get("PATH"))
    if on_path and _is_java_17_plus(on_path):
        return Path(on_path)

    patterns = [
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java",
        "/opt/homebrew/opt/openjdk*/libexec/openjdk.jdk/Contents/Home/bin/java",
        "/Library/Java/JavaVirtualMachines/*.jdk/Contents/Home/bin/java",
        "/usr/lib/jvm/java-*-openjdk*/bin/java",
        "/usr/lib/jvm/jdk-*/bin/java",
    ]
    for pattern in patterns:
        for match in sorted(glob.glob(pattern)):
            if _is_java_17_plus(match):
                return Path(match)

    raise RuntimeError(f"Java 17 or newer not found. Install JDK 17+ or set {JAVA_ENV} / JAVA_HOME.")


class RobolectricWorker:
    """Thread-safe persistent Robolectric Native Graphics JVM speaking JSON lines over stdio."""

    def __init__(self, *, robolectric_cp_path=None, bootstrap_dir_path=None, java_path=None, environ=None, home=None):
        self._explicit_rcp = Path(robolectric_cp_path) if robolectric_cp_path else None
        self._explicit_bdir = Path(bootstrap_dir_path) if bootstrap_dir_path else None
        self._explicit_java = Path(java_path) if java_path else None
        self._environ = environ
        self._home = home
        self._lock = threading.Lock()
        self._stderr_lock = threading.Lock()
        self._proc = None
        self._stdout_q = None
        self._stdout_thread = None
        self._stderr_thread = None
        self._stderr_tail = deque(maxlen=64)
        self._req_counter = 0

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        self.close()

    def _resolve_command(self):
        rcp = self._explicit_rcp or robolectric_cp(self._environ, self._home)
        bdir = self._explicit_bdir or bootstrap_dir(self._environ, self._home)
        java_bin = self._explicit_java or java_binary(self._environ)
        return [
            str(java_bin),
            "-Xmx3072m",
            "-Djava.awt.headless=true",
            f"-Dadp.bootstrap.dir={Path(bdir).resolve()}",
            f"@{Path(rcp).resolve()}",
            ROBOLECTRIC_MAIN,
        ]

    def _ensure_started_locked(self):
        if self._proc is not None and self._proc.poll() is None:
            return
        self._kill_locked()
        cmd = self._resolve_command()
        with self._stderr_lock:
            self._stderr_tail.clear()
        out_q = queue.SimpleQueue()
        proc = subprocess.Popen(
            cmd,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding="utf-8",
            bufsize=1,
        )

        def read_stdout():
            try:
                assert proc.stdout is not None
                for line in proc.stdout:
                    out_q.put(line)
            except (OSError, ValueError):
                pass
            finally:
                out_q.put(None)

        def read_stderr():
            try:
                assert proc.stderr is not None
                for line in proc.stderr:
                    line_text = line.rstrip("\r\n")
                    with self._stderr_lock:
                        self._stderr_tail.append(line_text)
            except (OSError, ValueError):
                pass

        stdout_t = threading.Thread(target=read_stdout, name="robolectric-stdout", daemon=True)
        stderr_t = threading.Thread(target=read_stderr, name="robolectric-stderr", daemon=True)
        stdout_t.start()
        stderr_t.start()
        self._proc = proc
        self._stdout_q = out_q
        self._stdout_thread = stdout_t
        self._stderr_thread = stderr_t

    def _kill_locked(self):
        proc = self._proc
        stdout_t = self._stdout_thread
        stderr_t = self._stderr_thread
        self._proc = None
        self._stdout_q = None
        self._stdout_thread = None
        self._stderr_thread = None
        if proc is None:
            return
        try:
            if proc.stdin:
                proc.stdin.close()
        except OSError:
            pass
        if proc.poll() is None:
            proc.terminate()
            try:
                proc.wait(timeout=2)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait(timeout=2)
        for thread in (stdout_t, stderr_t):
            if thread is not None and thread.is_alive():
                thread.join(timeout=1.0)
        for stream in (proc.stdout, proc.stderr):
            try:
                if stream:
                    stream.close()
            except OSError:
                pass

    def _stderr_summary(self):
        with self._stderr_lock:
            return "\n".join(self._stderr_tail)

    def _raise_worker_error_locked(self, message, cause=None):
        self._kill_locked()
        stderr = self._stderr_summary()
        full = f"{message}\n{stderr}".strip()
        if cause is not None:
            raise RuntimeError(full) from cause
        raise RuntimeError(full)

    def render_report(self, source_path, out_dir, *, design_path=None, timeout=900):
        """Render a complete report.json + PNGs + trees via Robolectric mode='report'."""
        with self._lock:
            self._ensure_started_locked()
            self._req_counter += 1
            payload = {
                "id": f"eval-{self._req_counter}",
                "sourcePath": str(Path(source_path).resolve()),
                "outDir": str(Path(out_dir).resolve()),
                "mode": "report",
                "includeOverlays": True,
                "writeReport": True,
                "density": 2.0,
                "encodePng": True,
            }
            if design_path is not None:
                payload["designPath"] = str(Path(design_path).resolve())

            proc = self._proc
            out_q = self._stdout_q
            assert proc is not None and proc.stdin is not None and out_q is not None
            try:
                proc.stdin.write(json.dumps(payload, separators=(",", ":")) + "\n")
                proc.stdin.flush()
            except OSError as err:
                self._raise_worker_error_locked(f"failed writing to Robolectric worker: {err}", cause=err)

            deadline = time.monotonic() + timeout
            while True:
                remaining = deadline - time.monotonic()
                if remaining <= 0:
                    self._raise_worker_error_locked(f"Robolectric render timed out after {timeout}s")
                try:
                    raw_line = out_q.get(timeout=remaining)
                except queue.Empty:
                    self._raise_worker_error_locked(f"Robolectric render timed out after {timeout}s")
                if raw_line is None:
                    exit_code = proc.poll()
                    if exit_code is None:
                        try:
                            exit_code = proc.wait(timeout=1)
                        except subprocess.TimeoutExpired:
                            exit_code = None
                    self._raise_worker_error_locked(f"Robolectric worker exited ({exit_code}) before answering")
                line = raw_line.strip()
                if not line:
                    continue
                try:
                    resp = json.loads(line)
                except ValueError as err:
                    self._raise_worker_error_locked(
                        f"Robolectric worker emitted invalid JSON: {line[:300]}",
                        cause=err,
                    )
                if not isinstance(resp, dict):
                    self._raise_worker_error_locked(f"Robolectric worker returned non-object JSON: {line[:300]}")
                return resp

    def close(self):
        with self._lock:
            self._kill_locked()
