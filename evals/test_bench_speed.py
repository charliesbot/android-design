import tempfile
import unittest
from pathlib import Path
from unittest import mock

import bench_speed
import preview_app


class BenchSpeedTests(unittest.TestCase):
    def test_bench_speed_stops_server_when_start_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            out_json = Path(tmp) / "speed.json"
            with mock.patch.object(bench_speed, "run_cli", side_effect=RuntimeError("start failed")), \
                    mock.patch.object(preview_app, "stop_preview_server", return_value=True) as stopper, \
                    mock.patch("sys.argv", ["bench_speed.py", "--out", str(out_json)]):
                with self.assertRaisesRegex(RuntimeError, "start failed"):
                    bench_speed.main()
            stopper.assert_called_once()

    def test_bench_speed_uses_isolated_cache_dir_by_default(self):
        seen_envs = []

        def fake_run_cli(args, env, cwd):
            seen_envs.append(dict(env))
            raise RuntimeError("stop after start")

        with tempfile.TemporaryDirectory() as tmp:
            out_json = Path(tmp) / "speed.json"
            with mock.patch.dict("os.environ", {}, clear=False), \
                    mock.patch.object(bench_speed, "run_cli", side_effect=fake_run_cli), \
                    mock.patch.object(preview_app, "stop_preview_server", return_value=True) as stopper, \
                    mock.patch("sys.argv", ["bench_speed.py", "--out", str(out_json)]):
                bench_speed.os.environ.pop(preview_app.CACHE_DIR_ENV, None)
                with self.assertRaisesRegex(RuntimeError, "stop after start"):
                    bench_speed.main()
            self.assertEqual(len(seen_envs), 1)
            cache_dir = seen_envs[0].get(preview_app.CACHE_DIR_ENV)
            self.assertIsNotNone(cache_dir)
            self.assertIn("eval-bench-", cache_dir)
            stopper.assert_called_once()


if __name__ == "__main__":
    unittest.main()
