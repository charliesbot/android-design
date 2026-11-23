import json
import os
import stat
import sys
import tempfile
import unittest
from pathlib import Path

import preview_app


def make_file(root, relative, content=""):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    return path


class PreviewAppTests(unittest.TestCase):
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
            rcp = make_file(bdir, "robolectric.cp", "-cp\nb.jar\n")
            env = {preview_app.SRC_ENV: str(app_dir)}

            self.assertEqual(preview_app.bootstrap_dir(env), bdir)
            self.assertEqual(preview_app.server_cp(env), scp)
            self.assertEqual(preview_app.robolectric_cp(env), rcp)

            rcp.unlink()
            with self.assertRaisesRegex(RuntimeError, r"\./gradlew :host:exportBootstrap"):
                preview_app.robolectric_cp(env)

            custom_bdir = make_file(tmp, "custom-bdir/robolectric.cp", "-cp\nc.jar\n").parent
            self.assertEqual(preview_app.robolectric_cp({preview_app.BOOTSTRAP_DIR_ENV: str(custom_bdir)}), custom_bdir / "robolectric.cp")

    def test_robolectric_worker_reuses_persistent_process_and_recovers_after_exit(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            bdir = tmp_path / "bdir"
            rcp = make_file(bdir, "robolectric.cp", "-cp\nfake.jar\n")
            fake_java = make_file(
                tmp_path,
                "fake-java.py",
                f"""#!{sys.executable}
import json, os, sys
pid = os.getpid()
while True:
    line = sys.stdin.readline()
    if not line:
        break
    req = json.loads(line)
    src = req.get("sourcePath", "")
    if "crash" in src:
        sys.stderr.write("simulated crash\\n")
        sys.stderr.flush()
        sys.exit(2)
    out_dir = req.get("outDir")
    if out_dir:
        os.makedirs(out_dir, exist_ok=True)
        with open(os.path.join(out_dir, "report.json"), "w", encoding="utf-8") as fh:
            json.dump({{"valid": True, "errors": [], "renders": [], "theme": {{"seed": "#B5523B", "variant": "tonal-spot", "contrast": 0.0}}, "pass": True}}, fh)
    print(json.dumps({{"id": req["id"], "ok": True, "valid": True, "pass": True, "pid": pid}}), flush=True)
""",
            )
            fake_java.chmod(fake_java.stat().st_mode | stat.S_IXUSR)

            with preview_app.RobolectricWorker(
                robolectric_cp_path=rcp,
                bootstrap_dir_path=bdir,
                java_path=fake_java,
            ) as worker:
                r1 = worker.render_report(tmp_path / "a.explore.kt", tmp_path / "out1")
                r2 = worker.render_report(tmp_path / "b.explore.kt", tmp_path / "out2")
                self.assertEqual(r1["pid"], r2["pid"])
                self.assertTrue((tmp_path / "out1/report.json").is_file())

                with self.assertRaisesRegex(RuntimeError, "simulated crash"):
                    worker.render_report(tmp_path / "crash.explore.kt", tmp_path / "out-crash")

                r3 = worker.render_report(tmp_path / "c.explore.kt", tmp_path / "out3")
                self.assertNotEqual(r1["pid"], r3["pid"])

    def test_robolectric_worker_timeout_during_stderr_flood_kills_process_safely(self):
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            bdir = tmp_path / "bdir"
            rcp = make_file(bdir, "robolectric.cp", "-cp\nfake.jar\n")
            pid_file = tmp_path / "worker.pid"
            fake_java = make_file(
                tmp_path,
                "flood-java.py",
                f"""#!{sys.executable}
import os, sys, time
with open({str(pid_file)!r}, "w", encoding="utf-8") as fh:
    fh.write(str(os.getpid()))
sys.stdin.readline()
i = 0
while True:
    try:
        sys.stderr.write(f"flood-{{i}}\\n")
        if i % 64 == 0:
            sys.stderr.flush()
        i += 1
    except OSError:
        break
""",
            )
            fake_java.chmod(fake_java.stat().st_mode | stat.S_IXUSR)

            with preview_app.RobolectricWorker(
                robolectric_cp_path=rcp,
                bootstrap_dir_path=bdir,
                java_path=fake_java,
            ) as worker:
                # Capture any uncaught background reader-thread errors (e.g. ValueError on closed pipe)
                # and concurrently poll _stderr_summary() during live read_stderr appends.
                errors = []
                thread_errors = []
                done = []
                import threading

                orig_excepthook = threading.excepthook
                threading.excepthook = lambda args: thread_errors.append(args.exc_value)

                def poll_stderr():
                    while not done:
                        try:
                            worker._stderr_summary()
                        except Exception as err:
                            errors.append(err)
                            break

                poller = threading.Thread(target=poll_stderr, daemon=True)
                poller.start()
                try:
                    with self.assertRaisesRegex(RuntimeError, "timed out"):
                        worker.render_report(tmp_path / "hang.explore.kt", tmp_path / "out-hang", timeout=0.5)
                finally:
                    done.append(True)
                    poller.join(timeout=2)
                    threading.excepthook = orig_excepthook

                self.assertEqual(errors, [], f"Concurrent stderr summary raised: {errors}")
                self.assertEqual(thread_errors, [], f"Reader thread raised: {thread_errors}")
                self.assertIsNone(worker._proc)
                pid = int(pid_file.read_text(encoding="utf-8"))
                # Wait briefly for OS reap, verify process is dead.
                for _ in range(20):
                    try:
                        os.kill(pid, 0)
                    except ProcessLookupError:
                        break
                    import time
                    time.sleep(0.05)
                with self.assertRaises(ProcessLookupError):
                    os.kill(pid, 0)


if __name__ == "__main__":
    unittest.main()
