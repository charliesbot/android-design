import json
import tempfile
import unittest
from pathlib import Path

import preview_app


def make_file(root, relative):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("")
    return path


class PreviewAppTests(unittest.TestCase):
    def test_env_override_wins_for_supervisor_binary(self):
        with tempfile.TemporaryDirectory() as tmp:
            custom = make_file(tmp, "custom/preview.js")
            found = preview_app.app_binary({preview_app.EXECUTABLE_ENV: str(custom)})
            self.assertEqual(found, custom)

    def test_repo_cli_bin_is_used_by_default(self):
        with tempfile.TemporaryDirectory() as tmp:
            cli = make_file(tmp, "plugin/bin/android-design-preview")
            original = preview_app.CLI_BIN
            preview_app.CLI_BIN = cli
            try:
                self.assertEqual(preview_app.app_binary({}), cli)
            finally:
                preview_app.CLI_BIN = original

    def test_broken_supervisor_env_override_is_an_error(self):
        with self.assertRaisesRegex(RuntimeError, preview_app.EXECUTABLE_ENV):
            preview_app.app_binary({preview_app.EXECUTABLE_ENV: "/nowhere/preview.js"})

    def test_worker_jar_prefers_env_then_repo_then_cache(self):
        with tempfile.TemporaryDirectory() as tmp:
            home = Path(tmp) / "home"
            version = json.loads(preview_app.PLUGIN_MANIFEST.read_text())["version"]
            cached = make_file(home / ".cache/android-design-preview" / version, "android-design-preview.jar")
            repo = make_file(tmp, "repo/android-design-preview.jar")
            custom = make_file(tmp, "custom/worker.jar")

            self.assertEqual(preview_app.worker_jar({preview_app.JAR_ENV: str(custom)}, home), custom)

            original_repo = preview_app.REPO_JAR
            preview_app.REPO_JAR = repo
            try:
                self.assertEqual(preview_app.worker_jar({}, home), repo)
                preview_app.REPO_JAR = Path(tmp) / "missing.jar"
                self.assertEqual(preview_app.worker_jar({}, home), cached)
            finally:
                preview_app.REPO_JAR = original_repo

    def test_missing_worker_jar_names_gradle_task(self):
        with tempfile.TemporaryDirectory() as tmp:
            original_repo = preview_app.REPO_JAR
            preview_app.REPO_JAR = Path(tmp) / "missing.jar"
            try:
                with self.assertRaisesRegex(RuntimeError, r"\./gradlew :app:workerJar"):
                    preview_app.worker_jar({}, Path(tmp) / "empty-home")
            finally:
                preview_app.REPO_JAR = original_repo


if __name__ == "__main__":
    unittest.main()
