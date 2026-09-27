#!/usr/bin/env python3
"""Model-graded screenshot rubric for one eval case.

Usage: judge.py <case_name> <image>... [--items R-a,R-b] [--runs N]
Prints JSON: {"R-id": {"pass": bool, "votes": "2/3", "reason": str}}. With several runs, the majority wins.
"""
import argparse
import json
import re
import subprocess
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent


def case_items(case_name):
    spec = json.loads((HERE / "evals.json").read_text())
    case = next(c for c in spec["evals"] if c["name"] == case_name)
    return [a for a in case["assertions"] if a["type"] == "rubric"]


def prompt_for(case_name, images, item_ids):
    items = [a for a in case_items(case_name) if a["id"] in item_ids]
    listed = "\n".join(
        f"- {a['id']}" + ("" if a["text"] == "See rubric.md" else f": {a['text']}") for a in items
    )
    shots = "\n".join(f"- {Path(p).stem}: {Path(p).resolve()}" for p in images)
    return f"""You are grading screenshots of an Android screen against a design rubric.

{(HERE / 'rubric.md').read_text()}

## Screenshots to open (use the Read tool on each)

{shots}

## Items to answer (only these)

{listed}

Answer every listed item. Reply with only the JSON object described in "Output"."""


def parse(text):
    match = re.search(r"\{.*\"items\".*\}", text, re.S)
    if not match:
        raise ValueError(f"judge returned no JSON: {text[:200]}")
    return {item["id"]: item for item in json.loads(match.group(0))["items"]}


SCHEMA = json.dumps({
    "type": "object",
    "properties": {"items": {"type": "array", "items": {
        "type": "object",
        "properties": {"id": {"type": "string"}, "pass": {"type": "boolean"}, "reason": {"type": "string"}},
        "required": ["id", "pass", "reason"]}}},
    "required": ["items"],
})


def judge_once(case_name, images, item_ids, attempts=3):
    folders = sorted({str(Path(p).resolve().parent) for p in images})
    cmd = ["claude", "-p", prompt_for(case_name, images, item_ids), "--output-format", "json",
           "--json-schema", SCHEMA, "--allowedTools", "Read"]
    for folder in folders:
        cmd += ["--add-dir", folder]
    for attempt in range(attempts):
        try:
            done = subprocess.run(cmd, capture_output=True, text=True, cwd=folders[0], timeout=600, stdin=subprocess.DEVNULL)
            reply = json.loads(done.stdout)
            structured = reply.get("structured_output")
            if structured:
                return {item["id"]: item for item in structured["items"]}
            return parse(reply["result"])
        except (ValueError, KeyError, subprocess.TimeoutExpired) as error:
            if attempt == attempts - 1:
                detail = "timed out" if isinstance(error, subprocess.TimeoutExpired) else error
                raise RuntimeError(f"judge failed after {attempts} attempts: {detail}") from error


def judge(case_name, images, item_ids=None, runs=1):
    item_ids = item_ids or [a["id"] for a in case_items(case_name)]
    ballots = [judge_once(case_name, images, item_ids) for _ in range(runs)]
    verdict = {}
    for item_id in item_ids:
        votes = [b[item_id]["pass"] for b in ballots if item_id in b]
        passed = Counter(votes).most_common(1)[0][0] if votes else False
        reason = next((b[item_id]["reason"] for b in ballots if item_id in b and b[item_id]["pass"] == passed), "no answer")
        verdict[item_id] = {"pass": passed, "votes": f"{votes.count(True)}/{len(votes)}", "reason": reason}
    return verdict


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("case")
    parser.add_argument("images", nargs="+")
    parser.add_argument("--items")
    parser.add_argument("--runs", type=int, default=1)
    args = parser.parse_args()
    ids = args.items.split(",") if args.items else None
    print(json.dumps(judge(args.case, args.images, ids, args.runs), indent=2))
