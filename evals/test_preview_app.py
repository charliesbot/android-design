import json
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
