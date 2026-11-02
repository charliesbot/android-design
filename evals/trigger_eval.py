#!/usr/bin/env python3
"""Trigger evals: does a skill's description reach its own prompts among every installed skill?

Each run happens in a sandbox project whose .claude/skills holds this repo's skills (the one under
test optionally with a candidate description) and links every other installed skill. claude -p runs
there with --setting-sources project, so installed copies of this repo's skills never compete with the
repo copies, and the real neighbors (adaptive, compose-animations, ...) still do. The sandbox lives in
a neutral temp directory: Claude sees its working directory, and a path naming the skill skews runs.
"""
import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
INSTALLED = Path.home() / ".claude" / "skills"
# Claude may look around (Glob, Read) before loading a skill, so it gets a few turns but no writes.
MAX_TURNS = "3"
DENIED = ["Edit", "Write", "Bash", "NotebookEdit"]


def set_description(skill_md: Path, description: str):
    """Replace the frontmatter description, keeping the name and the body."""
    text = skill_md.read_text()
    _, front, body = text.split("---\n", 2)
    name = next(line for line in front.splitlines() if line.startswith("name:"))
    indented = "\n".join("  " + line for line in description.strip().splitlines())
    skill_md.write_text(f"---\n{name}\ndescription: >\n{indented}\n---\n{body}")


def build_sandbox(repo_skills: Path, installed: Path, name: str, description: str | None = None) -> Path:
    """A project directory with this repo's skills plus every other installed one."""
    sandbox = Path(tempfile.mkdtemp(prefix="trigger-eval-"))
    skills = sandbox / ".claude" / "skills"
    shutil.copytree(repo_skills, skills)
    for child in sorted(installed.iterdir()) if installed.is_dir() else []:
        if not (skills / child.name).exists() and (child / "SKILL.md").exists():
            (skills / child.name).symlink_to(child.resolve())
    if description:
        set_description(skills / name / "SKILL.md", description)
    return sandbox


def loads_skill(tool_use: dict, name: str) -> bool:
    """True when a tool call loads the named skill, through the Skill tool or by reading SKILL.md."""
    tool_input = tool_use.get("input", {})
    if tool_use.get("name") == "Skill":
        return tool_input.get("skill", "").split(":")[-1] == name
    if tool_use.get("name") == "Read":
        return tool_input.get("file_path", "").endswith(f"/{name}/SKILL.md")
    return False


def run_query(query: str, name: str, sandbox: Path, timeout: int) -> dict:
    """One claude -p run; stops as soon as the skill loads."""
    cmd = ["claude", "-p", query, "--output-format", "stream-json", "--verbose",
           "--setting-sources", "project", "--max-turns", MAX_TURNS, "--disallowedTools", *DENIED]
    # Unset so the run can nest inside a Claude Code session.
    env = {k: v for k, v in os.environ.items() if k != "CLAUDECODE"}
    process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, cwd=sandbox, env=env, text=True)
    skills, hit, start = [], False, time.time()
    try:
        for line in process.stdout:
            if hit or time.time() - start > timeout:
                break
            try:
                event = json.loads(line)
            except json.JSONDecodeError:
                continue
            if event.get("type") != "assistant":
                continue
            for block in event["message"].get("content", []):
                if block.get("type") != "tool_use":
                    continue
                if block["name"] == "Skill":
                    skills.append(block["input"].get("skill", ""))
                hit = hit or loads_skill(block, name)
    finally:
        if process.poll() is None:
            process.kill()
        process.wait()
    return {"hit": hit, "skills": skills}


def passes(should_trigger: bool, hits: int, runs: int) -> bool:
    """Positives trigger in at least two thirds of runs; near-misses in at most one third."""
    return hits * 3 >= runs * 2 if should_trigger else hits * 3 <= runs


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--eval-set", required=True, type=Path, help="triggers/<skill>.json")
    parser.add_argument("--description", help="candidate description to test instead of the repo's")
    parser.add_argument("--runs", type=int, default=3)
    parser.add_argument("--workers", type=int, default=8)
    parser.add_argument("--timeout", type=int, default=120, help="seconds per run")
    parser.add_argument("--out", type=Path, help="write every run's result here as JSON")
    args = parser.parse_args()

    name = args.eval_set.stem
    sandbox = build_sandbox(ROOT / "skills", INSTALLED, name, args.description)
    items = json.loads(args.eval_set.read_text())
    jobs = [item for item in items for _ in range(args.runs)]
    try:
        with ThreadPoolExecutor(args.workers) as pool:
            results = list(pool.map(lambda item: run_query(item["query"], name, sandbox, args.timeout), jobs))
    finally:
        shutil.rmtree(sandbox)

    by_query = {}
    for item, result in zip(jobs, results):
        by_query.setdefault(item["query"], {"should_trigger": item["should_trigger"], "runs": []})["runs"].append(result)
    failed = 0
    for query, entry in by_query.items():
        hits = sum(run["hit"] for run in entry["runs"])
        ok = passes(entry["should_trigger"], hits, len(entry["runs"]))
        failed += not ok
        others = sorted({s for run in entry["runs"] for s in run["skills"] if s.split(":")[-1] != name})
        print(f"{'PASS' if ok else 'FAIL'} {hits}/{len(entry['runs'])} want={entry['should_trigger']!s:5} "
              f"{query[:70]}  also={others}")
    print(f"{len(by_query) - failed}/{len(by_query)} passed")
    if args.out:
        args.out.write_text(json.dumps(by_query, indent=1))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
