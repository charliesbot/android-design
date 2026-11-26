#!/usr/bin/env python3
"""Measure the v6 speed gate on a running emulator and write raw results as JSON.

Each timed run edits the fixture first, so every `check` pays a real warm compile, D8, device
render, and host PNG encode. Scoped runs check one direction in the `light` view; full runs check
every direction in every phone view (3 x 5). Gate: scoped median <= 500 ms, full median <= 2800 ms.

Usage: python3 evals/bench_speed.py --out evals/results/v6/speed.json [--runs 7]
"""

from __future__ import annotations

import argparse
import json
import os
import pathlib
import platform
import re
import shutil
import statistics
import subprocess
import tempfile
import time

from preview_app import build_cli_env, preview_server_session

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
SCRIPT = REPO_ROOT / "plugin" / "skills" / "android-design-explore" / "scripts" / "android-design-preview"
FIXTURE = pathlib.Path(__file__).resolve().parent / "bench_fixture"
SCOPED_GATE_MS = 500.0
FULL_GATE_MS = 2800.0


def run_cli(args: list[str], env: dict[str, str], cwd: pathlib.Path) -> tuple[float, dict]:
    started = time.perf_counter()
    res = subprocess.run([str(SCRIPT), *args], env=env, cwd=cwd, capture_output=True, text=True)
    elapsed_ms = (time.perf_counter() - started) * 1000.0
    try:
        payload = json.loads(res.stdout.strip().splitlines()[-1]) if res.stdout.strip() else {}
    except json.JSONDecodeError:
        payload = {"raw": res.stdout[-2000:]}
    if res.returncode not in (0, 1):
        raise RuntimeError(f"{' '.join(args)} exited {res.returncode}: {res.stderr[-2000:]} {res.stdout[-2000:]}")
    return elapsed_ms, payload


def timed_check(args: list[str], env: dict[str, str], cwd: pathlib.Path, renders: int) -> tuple[float, dict]:
    """Times one check and rejects any sample that did not render [renders] valid targets."""
    ms, payload = run_cli(args, env, cwd)
    count = len(payload.get("renders", []))
    if payload.get("error") or payload.get("valid") is not True or count != renders:
        raise RuntimeError(f"Invalid sample for {' '.join(args)}: renders={count} payload={json.dumps(payload)[:2000]}")
    return ms, payload


def bump(source: pathlib.Path, value: int) -> None:
    text = source.read_text(encoding="utf-8")
    source.write_text(re.sub(r"const val EDIT_MARKER = \d+", f"const val EDIT_MARKER = {value}", text), encoding="utf-8")


def summarize(samples: list[float], gate: float) -> dict:
    return {
        "n": len(samples),
        "median_ms": round(statistics.median(samples), 1),
        "min_ms": round(min(samples), 1),
        "max_ms": round(max(samples), 1),
        "samples_ms": [round(s, 1) for s in samples],
        "gate_ms": gate,
        "pass": statistics.median(samples) <= gate,
    }


def device_name(env: dict[str, str]) -> str:
    adb = shutil.which("adb") or os.path.join(env.get("ANDROID_HOME", ""), "platform-tools", "adb")
    res = subprocess.run([adb, "emu", "avd", "name"], capture_output=True, text=True)
    return res.stdout.splitlines()[0].strip() if res.returncode == 0 and res.stdout else "unknown"


def _pid_rss_mb(pid: int | None) -> float | None:
    if pid is None or pid <= 0:
        return None
    res = subprocess.run(["ps", "-o", "rss=", "-p", str(pid)], capture_output=True, text=True)
    if res.returncode != 0:
        return None
    try:
        kb = int(res.stdout.strip().splitlines()[-1])
        return round(kb / 1024.0, 1)
    except (IndexError, ValueError):
        return None


def _read_server_and_worker_rss_mb(cache_dir: pathlib.Path) -> dict[str, float | int | None]:
    record_path = cache_dir / "servers" / "active.json"
    try:
        server_pid = json.loads(record_path.read_text(encoding="utf-8")).get("pid")
    except (OSError, ValueError):
        server_pid = None
    if not isinstance(server_pid, int) or isinstance(server_pid, bool) or server_pid <= 0:
        return {"server_pid": None, "worker_pid": None, "server_rss_mb": None, "worker_rss_mb": None, "total_rss_mb": None}

    worker_pid = None
    pgrep = subprocess.run(["pgrep", "-P", str(server_pid)], capture_output=True, text=True)
    if pgrep.returncode == 0:
        for line in pgrep.stdout.splitlines():
            try:
                cand = int(line.strip())
            except ValueError:
                continue
            ps_cmd = subprocess.run(["ps", "-o", "command=", "-p", str(cand)], capture_output=True, text=True)
            if "LayoutlibWorkerMain" in ps_cmd.stdout:
                worker_pid = cand
                break

    server_rss = _pid_rss_mb(server_pid)
    worker_rss = _pid_rss_mb(worker_pid)
    total = (
        round((server_rss or 0.0) + (worker_rss or 0.0), 1)
        if server_rss is not None or worker_rss is not None
        else None
    )
    return {
        "server_pid": server_pid,
        "worker_pid": worker_pid,
        "server_rss_mb": server_rss,
        "worker_rss_mb": worker_rss,
        "total_rss_mb": total,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", required=True, type=pathlib.Path)
    parser.add_argument("--runs", type=int, default=7)
    parser.add_argument(
        "--backend",
        choices=("android", "layoutlib"),
        default=os.environ.get("ANDROID_DESIGN_PREVIEW_BACKEND", "android").strip().lower() or "android",
    )
    args = parser.parse_args()

    src = pathlib.Path(os.environ.get("ANDROID_DESIGN_PREVIEW_SRC", str(REPO_ROOT / "app")))
    env = build_cli_env(src=src, backend=args.backend)

    with tempfile.TemporaryDirectory(prefix="adp-bench-") as tmp:
        repo = pathlib.Path(tmp) / "repo"
        shutil.copytree(FIXTURE, repo)
        (repo / ".git").mkdir()
        source = repo / "design" / "bench.explore.kt"

        with preview_server_session(
            repo,
            SCRIPT,
            src=src,
            backend=args.backend,
            cache_prefix="eval-bench",
        ) as active_cache_dir:
            env = build_cli_env(src=src, backend=args.backend, cache_dir=active_cache_dir)
            cold_started = time.perf_counter()
            if args.backend == "layoutlib":
                run_cli(["start"], env, repo)
            else:
                run_cli(["start", "--headless"], env, repo)
            marker = 1
            # First check completes the cold-start-to-first-check measurement and warms iteration 1 of 3.
            timed_check(["check", str(source)], env, repo, renders=15)
            cold_start_to_first_check_ms = (time.perf_counter() - cold_started) * 1000.0
            for _ in range(2):  # Warm the compiler and JIT before timing.
                bump(source, marker)
                marker += 1
                timed_check(["check", str(source)], env, repo, renders=15)

            scoped, full = [], []
            for _ in range(args.runs):
                bump(source, marker)
                marker += 1
                ms, last_scoped = timed_check(
                    ["check", str(source), "--direction", "List", "--view", "light"], env, repo, renders=1
                )
                scoped.append(ms)
            for _ in range(args.runs):
                bump(source, marker)
                marker += 1
                ms, last = timed_check(["check", str(source)], env, repo, renders=15)
                full.append(ms)
            warm_memory_mb = _read_server_and_worker_rss_mb(active_cache_dir)

    result = {
        "backend": args.backend,
        "device": "layoutlib" if args.backend == "layoutlib" else device_name(env),
        "host": f"{platform.system()} {platform.machine()}",
        "git_head": subprocess.run(
            ["git", "rev-parse", "--short", "HEAD"], cwd=REPO_ROOT, capture_output=True, text=True
        ).stdout.strip(),
        "cold_start_to_first_check_ms": round(cold_start_to_first_check_ms, 1),
        "warm_memory_mb": warm_memory_mb,
        "scoped_check": summarize(scoped, SCOPED_GATE_MS),
        "full_check_3x5": summarize(full, FULL_GATE_MS),
        "every_sample_validated": {"scoped_renders": 1, "full_renders": 15, "valid": True, "error": None},
        "last_full_image_count": len(last.get("images", [])),
        "last_full_compile_ms": last.get("compileMs"),
        "last_full_render_ms": last.get("renderMs"),
    }
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in result.items() if k not in ("scoped_check", "full_check_3x5")}, indent=2))
    for key in ("scoped_check", "full_check_3x5"):
        print(key, {k: result[key][k] for k in ("n", "median_ms", "min_ms", "max_ms", "pass")})
    return 0 if result["scoped_check"]["pass"] and result["full_check_3x5"]["pass"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
