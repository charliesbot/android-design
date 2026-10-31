#!/usr/bin/env python3
"""Promotes an eval run to the committed baseline. Usage: promote.py <run_dir>

Copies each case's primary render (the first screen at the surface's primary view), downscaled to JPEG, into
evals/baseline/<case>.jpg (uses macOS sips).
"""
import json
import subprocess
import sys
from pathlib import Path

from report import primary_first

HERE = Path(__file__).resolve().parent

run = Path(sys.argv[1])
results = json.loads((run / "results.json").read_text())["cases"]
if not results or any(not case.get("judge") or any(v.get("pass") is None for v in case["judge"].values()) for case in results):
    sys.exit("Cannot promote an incomplete visual assessment; finish judging first")
baseline = HERE / "baseline"
baseline.mkdir(exist_ok=True)
for case in results:
    primary = primary_first(case.get("renders", []), case["surface"])[:1]
    source = run / case["name"] / "renders" / primary[0]["png"] if primary else None
    if not source or not source.exists():
        print(f"skip {case['name']}: no primary render")
        continue
    target = baseline / f"{case['name']}.jpg"
    subprocess.run(["sips", "-Z", "1000", "-s", "format", "jpeg", "-s", "formatOptions", "82", str(source), "--out", str(target)], check=True, capture_output=True)
    print(f"baseline {case['name']} <- {source}")
