#!/usr/bin/env python3
"""Runs the android-design evals end to end.

Usage: run.py [--cases music,finance] [--parallel 3] [--judge-runs 1] [--out DIR] [--no-open]

For each case: copy the template project, let a headless agent build the brief while following the
skill, re-render offscreen, run the code checks and the screenshot judge, then write results.json and
report.html. Output goes to ~/.cache/android-design-evals/<timestamp>/ unless --out is given.
"""
import argparse
import json
import shutil
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path

from checks import run as run_checks
from judge import judge
from report import write_report, write_summary

HERE = Path(__file__).resolve().parent
SKILL_DIR = HERE.parent / "skills" / "android-design"
CONTRACTS = {
    "phone": "Implement the screen as `@Composable fun EvalScreen()` in package `com.example.evalapp` in the :app module, replacing the placeholder in EvalScreen.kt. It must apply the app's own theme. MainActivity already calls it.",
    "wear": "Implement the screen as `@Composable fun EvalWearScreen()` in package `com.example.evalapp.wear` in the :wear module, replacing the placeholder in EvalWearScreen.kt. It must apply its own Wear theme. MainActivity already calls it.",
    "widget": "Implement the widget as `class EvalWidget : GlanceAppWidget()` in package `com.example.evalapp` in the :app module, with its receiver registered in the manifest.",
}


def restore_harness(case, project):
    """Copies the fixed previews, render test, and eval-render.sh over whatever the agent left."""
    shutil.copytree(HERE / "overlays" / case["surface"], project, dirs_exist_ok=True)


def prepare(case, case_dir):
    # The agent only ever sees copies of the skill and the template project, never the repo.
    shutil.copytree(SKILL_DIR, case_dir / "skill")
    project = case_dir / "project"
    shutil.copytree(HERE / "template", project)
    restore_harness(case, project)
    for f in case.get("files", []):
        target = project / f["to"]
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy(HERE / f["from"], target)
    return project


def prompt_for(case, skill_copy):
    text = (HERE / "agent-prompt.md").read_text()
    return (text.replace("{{SKILL_DIR}}", str(skill_copy))
                .replace("{{CONTRACT}}", CONTRACTS[case["surface"]])
                .replace("{{BRIEF}}", case["prompt"]))


def run_case(case, out, judge_runs):
    case_dir = out / case["name"]
    if case_dir.exists():
        shutil.rmtree(case_dir)
    case_dir.mkdir(parents=True)
    project = prepare(case, case_dir)
    started = time.time()

    try:
        agent = subprocess.run(
            ["claude", "-p", prompt_for(case, case_dir / "skill"), "--dangerously-skip-permissions", "--output-format", "json"],
            cwd=project, capture_output=True, text=True, timeout=3600, stdin=subprocess.DEVNULL,
        )
        (case_dir / "agent.json").write_text(agent.stdout or agent.stderr)
        meta = json.loads(agent.stdout)
        agent_info = {"cost": meta.get("total_cost_usd"), "turns": meta.get("num_turns"), "message": meta.get("result", "")}
    except subprocess.TimeoutExpired:
        agent_info = {"cost": None, "turns": None, "message": "agent timed out after 60 minutes"}
    except json.JSONDecodeError:
        agent_info = {"cost": None, "turns": None, "message": (agent.stderr or "agent produced no output")[-2000:]}

    restore_harness(case, project)
    try:
        render = subprocess.run(["./eval-render.sh"], cwd=project, capture_output=True, text=True, timeout=1800, stdin=subprocess.DEVNULL)
        (case_dir / "render.log").write_text(render.stdout + render.stderr)
    except subprocess.TimeoutExpired:
        (case_dir / "render.log").write_text("render timed out after 30 minutes")
    renders = case_dir / "renders"
    if (project / "renders").exists():
        shutil.copytree(project / "renders", renders)

    checks = run_checks(project, case["surface"])
    (case_dir / "checks.json").write_text(json.dumps(checks, indent=2))

    images = sorted(str(p) for p in renders.glob("*.png")) if renders.exists() else []
    rubric_ids = [a["id"] for a in case["assertions"] if a["type"] == "rubric"]
    if not images:
        verdict = {i: {"pass": False, "votes": "0/0", "reason": "no renders"} for i in rubric_ids}
    else:
        try:
            verdict = judge(case["name"], images, rubric_ids, judge_runs)
        except Exception as error:  # a judge failure must not lose the other cases' results
            verdict = {i: {"pass": False, "votes": "0/0", "reason": f"judge error: {error}"} for i in rubric_ids}
    (case_dir / "judge.json").write_text(json.dumps(verdict, indent=2))

    return {
        "name": case["name"], "surface": case["surface"], "minutes": round((time.time() - started) / 60, 1),
        "agent": agent_info, "checks": checks, "judge": verdict,
        "renders": [Path(p).name for p in images],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--cases", default="all")
    parser.add_argument("--parallel", type=int, default=3)
    parser.add_argument("--judge-runs", type=int, default=1)
    parser.add_argument("--out")
    parser.add_argument("--no-open", action="store_true", help="don't open the report when the run finishes")
    args = parser.parse_args()

    spec = json.loads((HERE / "evals.json").read_text())["evals"]
    wanted = None if args.cases == "all" else set(args.cases.split(","))
    cases = [c for c in spec if wanted is None or c["name"] in wanted]
    stamp = datetime.now().strftime("%Y-%m-%dT%H%M%S")
    out = Path(args.out) if args.out else Path.home() / ".cache" / "android-design-evals" / stamp
    out.mkdir(parents=True, exist_ok=True)

    with ThreadPoolExecutor(max_workers=args.parallel) as pool:
        results = list(pool.map(lambda c: run_case(c, out, args.judge_runs), cases))

    (out / "results.json").write_text(json.dumps({"run": stamp, "cases": results}, indent=2))
    report = write_report(out, results, HERE / "baseline")
    print(write_summary(out, results).read_text())
    print(f"report: {report}")
    if not args.no_open and sys.platform == "darwin":
        subprocess.run(["open", str(report)])


if __name__ == "__main__":
    main()
