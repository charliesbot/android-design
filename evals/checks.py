#!/usr/bin/env python3
"""Deterministic checks for one eval case. Usage: checks.py <case_dir> [phone]

Reads the files the runner leaves in the case directory: lint.json (the lint result of `cli.mjs render`),
renders/checks.json (the in-page runtimeChecks of every render), and the proposal's index.html (named by
case.json). Prints JSON: {"C-id": {"pass": bool, "detail": str}}.
"""
import json
import re
import sys
from pathlib import Path

SURFACES = {"phone"}
BAD_VARIANTS = {"expressive", "vibrant"}
THEME_TAG = re.compile(r"<m3e-theme\b[^>]*>", re.I)
STATES_META = re.compile(r"""<meta\b[^>]*\bname\s*=\s*["']p-states["'][^>]*>""", re.I)
STATES_CONTENT = re.compile(r"""\bcontent\s*=\s*(?:"([^"]*)"|'([^']*)')""", re.I)
MIN_STATES = 3
VARIANT = re.compile(r"""\bvariant\s*=\s*["']([^"']*)["']""", re.I)
# Top-level runtimeChecks keys that carry a failure flag, with how to read each one.
FAILING = {
    "pageOverflow": lambda v: v.get("fails"),
    "touchTargets": lambda v: v.get("under48"),
    "fonts": lambda v: v.get("offenders"),
    "overflow": lambda v: v,
    "midWordBreaks": lambda v: v,
    "overlaps": lambda v: v,
    "primary": lambda v: v.get("status") == "fail",
    "fabs": lambda v: v.get("ok") is False,
    "contrast": lambda v: v.get("fails"),
}


def result(ok, detail=""):
    return {"pass": ok, "detail": detail}


def read_json(path: Path):
    try:
        return json.loads(path.read_text())
    except (OSError, ValueError):
        return None


def proposal_dir(case_dir: Path):
    case = read_json(case_dir / "case.json") or {}
    exploration = case.get("exploration")
    if not exploration:
        return None
    return Path(exploration) / "project" / "proposals" / case.get("proposal", "a")


def check_lint(case_dir: Path):
    lint = read_json(case_dir / "lint.json")
    if lint is None:
        return result(False, "lint did not run")
    if lint.get("ok"):
        return result(True)
    return result(False, str(lint.get("output", "lint failed")).strip()[-400:])


def failing_keys(outcome):
    return [key for key, bad in FAILING.items() if key in outcome and bad(outcome[key])]


def check_review(case_dir: Path):
    review = read_json(case_dir / "renders" / "checks.json")
    if not review or not isinstance(review.get("renders"), list) or not review["renders"]:
        log = case_dir / "render.log"
        why = f": {log.read_text()[-300:].strip()}" if log.is_file() else ""
        return result(False, f"no renders/checks.json: the proposal was not rendered{why}")
    failed, unverified = [], []
    for render in review["renders"]:
        outcome = render.get("result") or {}
        if outcome.get("pass") is not True:
            keys = failing_keys(outcome)
            reason = outcome.get("error") or (",".join(keys) if keys else "failed")
            failed.append(f"{render.get('name')}: {reason}")
        elif outcome.get("complete") is not True:
            unverified.append(str(render.get("name")))
    if failed:
        return result(False, "; ".join(failed)[:600])
    if unverified:
        return result(False, f"unverified (checks incomplete): {', '.join(unverified)}")
    return result(True)


def check_palette(case_dir: Path):
    proposal = proposal_dir(case_dir)
    index = proposal / "index.html" if proposal else None
    if not index or not index.is_file():
        return result(False, "proposal index.html not found")
    tags = THEME_TAG.findall(index.read_text(errors="ignore"))
    if not tags:
        return result(False, "no <m3e-theme> in index.html")
    bad = [m.group(1) for tag in tags for m in [VARIANT.search(tag)] if m and m.group(1).lower() in BAD_VARIANTS]
    return result(not bad, f"<m3e-theme> variant {bad[0]}" if bad else "")


def check_states(case_dir: Path):
    """The brief opens overlays over the screen: the proposal must declare them as p-states and render each one."""
    proposal = proposal_dir(case_dir)
    index = proposal / "index.html" if proposal else None
    if not index or not index.is_file():
        return result(False, "proposal index.html not found")
    meta = STATES_META.search(re.sub(r"<!--.*?-->", "", index.read_text(errors="ignore"), flags=re.S))
    content = STATES_CONTENT.search(meta.group(0)) if meta else None
    names = (next(g for g in content.groups() if g is not None).split() if content else [])
    if not meta:
        return result(False, 'index.html declares no <meta name="p-states">: declare each overlay as a state')
    if len(names) < MIN_STATES:
        return result(False, f"p-states lists {len(names)} state(s); the brief needs at least three ({', '.join(names)})")
    renders = case_dir / "renders"
    missing = [f"{name}-{view}.png" for name in names for view in ("light", "tablet") if not (renders / f"{name}-{view}.png").is_file()]
    if missing:
        return result(False, f"missing state renders: {', '.join(missing)}")
    return result(True)


def run(case_dir: Path, surface: str = "phone", code_ids=()):
    """The default checks, plus any extra code checks a case lists (C-states)."""
    case_dir = Path(case_dir)
    out = {
        "C-lint": check_lint(case_dir),
        "C-review": check_review(case_dir),
        "C-palette": check_palette(case_dir),
    }
    if "C-states" in code_ids:
        out["C-states"] = check_states(case_dir)
    return out


if __name__ == "__main__":
    if len(sys.argv) not in (2, 3) or (len(sys.argv) == 3 and sys.argv[2] not in SURFACES):
        sys.exit("usage: checks.py <case_dir> [phone]")
    print(json.dumps(run(Path(sys.argv[1])), indent=2))
