import tempfile
import unittest
from pathlib import Path

import preview_app


def make_executable(root, relative):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("")
    return path


class AppBinaryTests(unittest.TestCase):
    def test_env_override_wins_over_the_installed_app(self):
        with tempfile.TemporaryDirectory() as tmp:
            installed = make_executable(Path(tmp) / "home" / "Applications", preview_app.EXECUTABLE)
            custom = make_executable(tmp, "custom/preview")
            found = preview_app.app_binary({"ANDROID_DESIGN_PREVIEW": str(custom)}, Path(tmp) / "home")
            self.assertEqual(found, custom)
            self.assertNotEqual(found, installed)

    def test_installed_app_is_found_in_the_applications_folder(self):
        with tempfile.TemporaryDirectory() as tmp:
            installed = make_executable(Path(tmp) / "Applications", preview_app.EXECUTABLE)
            self.assertEqual(preview_app.app_binary({}, tmp), installed)

    def test_repo_build_output_is_the_last_resort(self):
        with tempfile.TemporaryDirectory() as tmp:
            build = make_executable(tmp, "build/AndroidDesignPreview")
            original = preview_app.REPO_BUILD
            preview_app.REPO_BUILD = build
            try:
                self.assertEqual(preview_app.app_binary({}, Path(tmp) / "empty-home"), build)
            finally:
                preview_app.REPO_BUILD = original

    def test_a_broken_env_override_is_an_error_not_a_fallthrough(self):
        with tempfile.TemporaryDirectory() as tmp:
            make_executable(Path(tmp) / "Applications", preview_app.EXECUTABLE)
            with self.assertRaisesRegex(RuntimeError, "ANDROID_DESIGN_PREVIEW"):
                preview_app.app_binary({"ANDROID_DESIGN_PREVIEW": "/nowhere/preview"}, tmp)

    def test_missing_app_names_the_install_command(self):
        with tempfile.TemporaryDirectory() as tmp:
            original = preview_app.REPO_BUILD
            preview_app.REPO_BUILD = Path(tmp) / "missing"
            try:
                with self.assertRaisesRegex(RuntimeError, r"\./gradlew :app:installApp"):
                    preview_app.app_binary({}, tmp)
            finally:
                preview_app.REPO_BUILD = original


if __name__ == "__main__":
    unittest.main()
