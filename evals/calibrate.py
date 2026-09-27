#!/usr/bin/env python3
"""Checks the judge against labeled past renders. Usage: calibrate.py [--runs N]

Exits non-zero if any label is not reproduced, so the rubric wording can be fixed before trusting the judge.
"""
import argparse
import json
import sys
from pathlib import Path

from judge import judge

HERE = Path(__file__).resolve().parent

parser = argparse.ArgumentParser()
parser.add_argument("--runs", type=int, default=3)
args = parser.parse_args()

labels = json.loads((HERE / "calibration" / "labels.json").read_text())["images"]
misses = 0
for label in labels:
    image = HERE / "calibration" / label["image"]
    verdict = judge(label["case"], [str(image)], list(label["expect"]), args.runs)
    for item_id, expected in label["expect"].items():
        got = verdict[item_id]
        ok = got["pass"] == expected
        misses += not ok
        mark = "ok  " if ok else "MISS"
        print(f"{mark} {label['image']:40} {item_id:22} expected {'PASS' if expected else 'FAIL'}, got {'PASS' if got['pass'] else 'FAIL'} ({got['votes']}): {got['reason'][:90]}")
print(f"\n{'calibrated' if not misses else f'{misses} miss(es)'}")
sys.exit(1 if misses else 0)
