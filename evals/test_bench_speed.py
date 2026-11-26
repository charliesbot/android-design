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

    def test_bench_speed_runs_layoutlib_without_adb_or_headless(self):
        seen_cmds = []

        def fake_run_cli(args, env, cwd):
            seen_cmds.append(list(args))
            if args[0] == "start":
                return (50.0, {"ok": True})
            renders = [{"view": "light"}] if "--view" in args else [{"view": f"v{i}"} for i in range(15)]
            return (120.0, {"valid": True, "renders": renders, "images": [], "compileMs": 10, "renderMs": 20})

        with tempfile.TemporaryDirectory() as tmp:
            out_json = Path(tmp) / "speed.json"
            with mock.patch.object(bench_speed, "run_cli", side_effect=fake_run_cli), \
                    mock.patch.object(preview_app, "stop_preview_server", return_value=True), \
                    mock.patch("sys.argv", ["bench_speed.py", "--out", str(out_json), "--runs", "1"]):
                rc = bench_speed.main()
            self.assertEqual(rc, 0)
            self.assertEqual(seen_cmds[0], ["start"])
            self.assertFalse(hasattr(bench_speed, "device_name"))
            data = bench_speed.json.loads(out_json.read_text(encoding="utf-8"))
            self.assertEqual(data["backend"], "layoutlib")
            self.assertEqual(data["device"], "layoutlib")
            self.assertIn("cold_start_to_first_check_ms", data)
            self.assertIn("warm_memory_mb", data)


if __name__ == "__main__":
    unittest.main()
