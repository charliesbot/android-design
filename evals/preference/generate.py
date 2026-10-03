#!/usr/bin/env python3
"""Builds several unsteered variants of each brief for a preference round.

Usage: generate.py [--variants 3] [--cases music,chat] [--parallel 4] [--out DIR]

Every variant gets the same prompt the scored evals use; the spread between variants shows where the
skill leaves design choices open. Runs the deterministic code checks but not the judge: the owner's
picks are the verdict. Rerunning with the same --out resumes and skips finished variants.
Output goes to ~/.cache/android-design-evals/preference-<timestamp>/<case>/v<n>/.
Then run serve.py on the output folder to pick.
"""
import argparse
import json
import shutil
import sys
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
from checks import run as run_checks  # noqa: E402
from run import build  # noqa: E402


def load_briefs():
    """The scored phone cases plus the preference-only briefs, in one list."""
    scored = json.loads((HERE.parent / "evals.json").read_text())["evals"]
    extra = json.loads((HERE / "briefs.json").read_text())["briefs"]
    return [c for c in scored if not c.get("parked")] + extra


def prune(variant_dir, project):
    """Keeps the variant's source for later diffing but drops build output and the skill copy."""
    shutil.rmtree(variant_dir / "skill", ignore_errors=True)
    shutil.rmtree(project / ".gradle", ignore_errors=True)
    for build_dir in project.glob("*/build"):
        shutil.rmtree(build_dir, ignore_errors=True)


def run_variant(case, variant_dir, backend, model=None):
    project, agent_info, images = build(case, variant_dir, backend, model)
    checks = run_checks(project, case["surface"])
    prune(variant_dir, project)
    record = {**agent_info, "checks": checks, "renders": [Path(p).name for p in images]}
    (variant_dir / "variant.json").write_text(json.dumps(record, indent=2))
    print(f"done {variant_dir.parent.name}/{variant_dir.name}: {len(images)} renders", flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--generator", choices=("claude", "codex"), required=True)
    parser.add_argument("--generator-model")
    parser.add_argument("--variants", type=int, default=3)
    parser.add_argument("--cases", help="comma-separated brief names; default: all")
    parser.add_argument("--parallel", type=int, default=4)
    parser.add_argument("--out")
    args = parser.parse_args()

    briefs = load_briefs()
    if args.cases:
        wanted = set(args.cases.split(","))
        briefs = [b for b in briefs if b["name"] in wanted]
    stamp = datetime.now().strftime("%Y-%m-%dT%H%M%S")
    out = Path(args.out) if args.out else Path.home() / ".cache" / "android-design-evals" / f"preference-{stamp}"
    out.mkdir(parents=True, exist_ok=True)

    manifest = {"cases": [{"name": b["name"], "prompt": b["prompt"]} for b in briefs], "variants": args.variants}
    (out / "manifest.json").write_text(json.dumps(manifest, indent=2))

    jobs = [(b, out / b["name"] / f"v{n}") for b in briefs for n in range(1, args.variants + 1)]
    jobs = [(b, d) for b, d in jobs if not (d / "variant.json").exists()]
    print(f"{len(jobs)} variants to build into {out}", flush=True)

    def safe(job):
        try:
            run_variant(*job, args.generator, args.generator_model)
        except Exception as error:  # one broken variant must not stop the round
            print(f"failed {job[1]}: {error}", flush=True)

    with ThreadPoolExecutor(max_workers=args.parallel) as pool:
        list(pool.map(safe, jobs))
    print(f"round ready: python3 {HERE / 'serve.py'} {out}", flush=True)


if __name__ == "__main__":
    main()
