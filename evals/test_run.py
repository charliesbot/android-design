import os
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import run


class PrepareTest(unittest.TestCase):
    def test_project_points_at_the_sdk_without_android_home(self):
        # A runner started from a shell without ANDROID_HOME must still render: the re-render
        # cannot rely on the agent having written local.properties.
        with tempfile.TemporaryDirectory() as tmp:
            sdk = Path(tmp) / "sdk"
            sdk.mkdir()
            case = {"name": "music", "surface": "phone", "files": []}
            env = {k: v for k, v in os.environ.items() if k not in ("ANDROID_HOME", "ANDROID_SDK_ROOT")}
            with mock.patch.dict(os.environ, env, clear=True), mock.patch.object(run, "DEFAULT_SDK", sdk):
                project = run.prepare(case, Path(tmp) / "case")
            self.assertEqual((project / "local.properties").read_text().strip(), f"sdk.dir={sdk}")


if __name__ == "__main__":
    unittest.main()
