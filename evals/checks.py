#!/usr/bin/env python3
"""Deterministic checks for one eval case. Usage: checks.py <case_dir> [phone]

Reads renders/report.json, which the runner leaves in the case directory (the previewer app's `render` writes it).
Prints JSON: {"C-id": {"pass": bool, "detail": str}}.
"""
import json
import sys
from pathlib import Path

SURFACES = {"phone"}
BAD_VARIANTS = {"expressive", "vibrant"}
MAX_ERRORS = 3
MAX_DETAIL = 600


def result(ok, detail=""):
    return {"pass": ok, "detail": detail}


def read_report(case_dir: Path):
    try:
        report = json.loads((case_dir / "renders" / "report.json").read_text())
    except (OSError, ValueError):
        return None
    return report if isinstance(report, dict) else None


def missing_report(case_dir: Path):
    log = case_dir / "render.log"
    why = f": {log.read_text()[-300:].strip()}" if log.is_file() else ""
    return result(False, f"no renders/report.json: the proposal was not rendered{why}")


def check_valid(case_dir: Path, report):
    if report is None:
        return missing_report(case_dir)
    if report.get("valid") is True:
        return result(True)
    errors = report.get("errors") or []
    detail = "; ".join(f"{e.get('path')}: {e.get('message')}" for e in errors[:MAX_ERRORS] if isinstance(e, dict))
    return result(False, (detail or "proposal is invalid")[:MAX_DETAIL])


def failed_rules(render):
    """The rule names of one failed render, `touch-target` from `touch-target: open_plate 40x40`."""
    rules = [str(f).split(":", 1)[0].strip() for f in render.get("failures") or []]
    return rules or ["failed"]


def check_review(case_dir: Path, report):
    if report is None:
        return missing_report(case_dir)
    renders = report.get("renders")
    if not isinstance(renders, list) or not renders:
        return result(False, "no renders: nothing was reviewed")
    failed = [f"{r.get('screen')}/{r.get('view')}: {rule}"
              for r in renders if r.get("pass") is not True for rule in failed_rules(r)]
    return result(not failed, "; ".join(failed)[:MAX_DETAIL])


def check_palette(case_dir: Path, report):
    if report is None:
        return missing_report(case_dir)
    theme = report.get("theme")
    variant = theme.get("variant") if isinstance(theme, dict) else None
    if not isinstance(variant, str):
        return result(False, "report.json has no theme variant")
    bad = variant.lower() in BAD_VARIANTS
    return result(not bad, f"theme variant {variant}" if bad else "")


def run(case_dir: Path, surface: str = "phone"):
    """The deterministic checks every case gets."""
    case_dir = Path(case_dir)
    report = read_report(case_dir)
    return {
        "C-valid": check_valid(case_dir, report),
        "C-review": check_review(case_dir, report),
        "C-palette": check_palette(case_dir, report),
    }


if __name__ == "__main__":
    if len(sys.argv) not in (2, 3) or (len(sys.argv) == 3 and sys.argv[2] not in SURFACES):
        sys.exit("usage: checks.py <case_dir> [phone]")
    print(json.dumps(run(Path(sys.argv[1])), indent=2))
