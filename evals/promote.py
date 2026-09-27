#!/usr/bin/env python3
"""Promotes an eval run to the committed baseline. Usage: promote.py <run_dir>

Copies each case's primary render, downscaled to JPEG, into evals/baseline/<case>.jpg (uses macOS sips).
"""
import json
import subprocess
import sys
from pathlib import Path

from report import PRIMARY

HERE = Path(__file__).resolve().parent

run = Path(sys.argv[1])
baseline = HERE / "baseline"
baseline.mkdir(exist_ok=True)
for case in json.loads((run / "results.json").read_text())["cases"]:
    source = run / case["name"] / "renders" / f"{PRIMARY[case['surface']]}.png"
    if not source.exists():
        print(f"skip {case['name']}: no {source.name}")
        continue
    target = baseline / f"{case['name']}.jpg"
    subprocess.run(["sips", "-Z", "1000", "-s", "format", "jpeg", "-s", "formatOptions", "82", str(source), "--out", str(target)], check=True, capture_output=True)
    print(f"baseline {case['name']} <- {source}")
