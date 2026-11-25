import io
import json
import os
import signal
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import preview_app


def make_file(root, relative, content=""):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    return path


class PreviewAppTests(unittest.TestCase):
    def test_default_eval_backend_is_android(self):
        self.assertEqual(preview_app.DEFAULT_EVAL_BACKEND, "android")

    def test_env_override_wins_for_supervisor_binary(self):
        with tempfile.TemporaryDirectory() as tmp:
            custom = make_file(tmp, "custom/preview.js")
            found = preview_app.app_binary({preview_app.EXECUTABLE_ENV: str(custom)})
            self.assertEqual(found, custom)

    def test_repo_cli_bin_is_used_by_default(self):
        with tempfile.TemporaryDirectory() as tmp:
            cli = make_file(tmp, "plugin/skills/android-design-explore/scripts/android-design-preview")
            original = preview_app.CLI_BIN
            preview_app.CLI_BIN = cli
            try:
                self.assertEqual(preview_app.app_binary({}), cli)
            finally:
                preview_app.CLI_BIN = original

    def test_broken_supervisor_env_override_is_an_error(self):
        with self.assertRaisesRegex(RuntimeError, preview_app.EXECUTABLE_ENV):
            preview_app.app_binary({preview_app.EXECUTABLE_ENV: "/nowhere/preview.js"})

    def test_preview_src_prefers_env_then_repo_then_cache(self):
        with tempfile.TemporaryDirectory() as tmp:
            home = Path(tmp) / "home"
            version = json.loads(preview_app.PLUGIN_MANIFEST.read_text())["version"]
            cached = make_file(home / ".cache/android-design-preview" / version / "src/app", "settings.gradle.kts").parent
            repo_app = make_file(tmp, "repo/app/settings.gradle.kts").parent
            env_root = make_file(tmp, "env-root/app/settings.gradle.kts").parent.parent
            env_app = make_file(tmp, "env-app/settings.gradle.kts").parent

            self.assertEqual(preview_app.preview_src({preview_app.SRC_ENV: str(env_root)}, home), env_root / "app")
            self.assertEqual(preview_app.preview_src({preview_app.SRC_ENV: str(env_app)}, home), env_app)
            with self.assertRaisesRegex(RuntimeError, preview_app.SRC_ENV):
                preview_app.preview_src({preview_app.SRC_ENV: str(Path(tmp) / "bad-src")}, home)

            original_repo_app = preview_app.REPO_APP
            preview_app.REPO_APP = repo_app
            try:
                self.assertEqual(preview_app.preview_src({}, home), repo_app)
                preview_app.REPO_APP = Path(tmp) / "missing-app"
                self.assertEqual(preview_app.preview_src({}, home), cached)
            finally:
                preview_app.REPO_APP = original_repo_app

    def test_bootstrap_dir_and_classpath_argfiles_and_missing_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            app_dir = make_file(tmp, "app/settings.gradle.kts").parent
            bdir = app_dir / "host/build/bootstrap"
            scp = make_file(bdir, "server.cp", "-cp\na.jar\n")
            env = {preview_app.SRC_ENV: str(app_dir)}

            self.assertEqual(preview_app.bootstrap_dir(env), bdir)
            self.assertEqual(preview_app.server_cp(env), scp)

            scp.unlink()
            with self.assertRaisesRegex(RuntimeError, r"\./gradlew :host:exportBootstrap"):
                preview_app.server_cp(env)

            custom_bdir = make_file(tmp, "custom-bdir/server.cp", "-cp\nc.jar\n").parent
            self.assertEqual(preview_app.server_cp({preview_app.BOOTSTRAP_DIR_ENV: str(custom_bdir)}), custom_bdir / "server.cp")

    def test_stop_preview_server_returns_true_on_zero_exit(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            cache = Path(tmp) / "cache"
            with mock.patch.object(
                preview_app.subprocess,
                "run",
                return_value=subprocess.CompletedProcess([], 0, "", ""),
            ) as run_call:
                stopped = preview_app.stop_preview_server(work, Path("/bin/preview"), cache_dir=cache)
            self.assertTrue(stopped)
            self.assertEqual(run_call.call_args.kwargs["timeout"], preview_app.DEFAULT_STOP_TIMEOUT)
            self.assertGreaterEqual(preview_app.DEFAULT_STOP_TIMEOUT, 45)

    def test_stop_preview_server_falls_back_to_active_json_pid_on_timeout(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            cache = Path(tmp) / "cache"
            make_file(cache, "servers/active.json", json.dumps({"pid": 999999, "port": 49210}))
            signals = []

            def fake_kill(pid, sig):
                signals.append((pid, sig))
                if sig == signal.SIGKILL:
                    return None
                if sig == 0 and any(s == signal.SIGKILL for _, s in signals):
                    raise ProcessLookupError
                return None

            with mock.patch.object(
                preview_app.subprocess,
                "run",
                side_effect=subprocess.TimeoutExpired(["preview", "stop"], 60),
            ), mock.patch.object(preview_app.os, "kill", side_effect=fake_kill):
                stopped = preview_app.stop_preview_server(
                    work,
                    Path("/bin/preview"),
                    cache_dir=cache,
                    grace_seconds=0.02,
                    kill_wait_seconds=0.05,
                    poll_interval=0.005,
                )
            self.assertTrue(stopped)
            self.assertEqual(signals[0], (999999, signal.SIGTERM))
            self.assertIn((999999, signal.SIGKILL), signals)

    def test_stop_preview_server_rejects_invalid_and_own_pid(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            for bad_pid in (0, -5, True, "1234", os.getpid()):
                cache = Path(tmp) / f"cache-{bad_pid}"
                make_file(cache, "servers/active.json", json.dumps({"pid": bad_pid}))
                with mock.patch.object(
                    preview_app.subprocess,
                    "run",
                    return_value=subprocess.CompletedProcess([], 1, "", "err"),
                ), mock.patch.object(preview_app.os, "kill") as kill_mock:
                    stopped = preview_app.stop_preview_server(work, Path("/bin/preview"), cache_dir=cache)
                self.assertFalse(stopped)
                kill_mock.assert_not_called()

    def test_stop_preview_server_handles_process_lookup_and_permission_errors(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            cache = Path(tmp) / "cache"
            make_file(cache, "servers/active.json", json.dumps({"pid": 888888}))
            with mock.patch.object(
                preview_app.subprocess,
                "run",
                return_value=subprocess.CompletedProcess([], 1, "", "err"),
            ), mock.patch.object(preview_app.os, "kill", side_effect=ProcessLookupError):
                self.assertTrue(preview_app.stop_preview_server(work, Path("/bin/preview"), cache_dir=cache))

            with mock.patch.object(
                preview_app.subprocess,
                "run",
                return_value=subprocess.CompletedProcess([], 1, "", "err"),
            ), mock.patch.object(preview_app.os, "kill", side_effect=PermissionError):
                self.assertFalse(preview_app.stop_preview_server(work, Path("/bin/preview"), cache_dir=cache))

    def test_preview_server_session_stops_and_cleans_on_normal_and_exception_exit(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            stopper = mock.Mock(return_value=True)
            with preview_app.preview_server_session(
                work, Path("/bin/preview"), stopper=stopper, environ={}
            ) as cache_dir:
                self.assertTrue(cache_dir.is_dir())
            stopper.assert_called_once()
            self.assertFalse(cache_dir.exists())

            stopper.reset_mock()
            created = None
            with self.assertRaisesRegex(RuntimeError, "boom"):
                with preview_app.preview_server_session(
                    work, Path("/bin/preview"), stopper=stopper, environ={}
                ) as cache_dir:
                    created = cache_dir
                    raise RuntimeError("boom")
            stopper.assert_called_once()
            self.assertFalse(created.exists())

    def test_preview_server_session_stops_and_cleans_on_sigterm(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            stopper = mock.Mock(return_value=True)
            created = None
            with self.assertRaises(SystemExit) as ctx:
                with preview_app.preview_server_session(
                    work, Path("/bin/preview"), stopper=stopper, environ={}
                ) as cache_dir:
                    created = cache_dir
                    os.kill(os.getpid(), signal.SIGTERM)
            self.assertEqual(ctx.exception.code, 128 + signal.SIGTERM)
            stopper.assert_called_once()
            self.assertFalse(created.exists())

    def test_preview_server_session_preserves_created_cache_when_stop_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp) / "work"
            work.mkdir()
            stopper = mock.Mock(return_value=False)
            stderr = io.StringIO()
            with mock.patch("sys.stderr", stderr):
                with preview_app.preview_server_session(
                    work, Path("/bin/preview"), stopper=stopper, environ={}
                ) as cache_dir:
                    make_file(cache_dir, "servers/active.json", json.dumps({"pid": 424242}))
            try:
                stopper.assert_called_once()
                self.assertTrue(cache_dir.exists())
                self.assertIn("424242", stderr.getvalue())
                self.assertIn(str(cache_dir), stderr.getvalue())
            finally:
                if cache_dir.exists():
                    import shutil
                    shutil.rmtree(cache_dir, ignore_errors=True)
