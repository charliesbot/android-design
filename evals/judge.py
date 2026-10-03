#!/usr/bin/env python3
"""Model-graded screenshot rubric for one eval case.

Usage: judge.py <case_name> <image>... [--items R-a,R-b] [--runs N]
Prints JSON: {"R-id": {"pass": bool, "votes": "2/3", "reason": str}}. With several runs, the majority wins.
"""
import argparse
import json
import re
from backends import BACKENDS, execute
from pathlib import Path

HERE = Path(__file__).resolve().parent


def case_items(case_name):
    spec = json.loads((HERE / "evals.json").read_text())
    case = next(c for c in spec["evals"] if c["name"] == case_name)
    return [a for a in case["assertions"] if a["type"] == "rubric"]


def prompt_for(case_name, images, item_ids, *, case=None, rubric=None):
    if case is None:
        spec = json.loads((HERE / "evals.json").read_text())
        case = next(c for c in spec["evals"] if c["name"] == case_name)
    items = [a for a in case["assertions"] if a["id"] in item_ids]
    rubric = rubric if rubric is not None else (HERE / 'rubric.md').read_text()
    listed = "\n".join(
        f"- {a['id']}" + ("" if a.get("text", "See rubric.md") == "See rubric.md" else f": {a['text']}") for a in items
    )
    shots = "\n".join(f"- {Path(p).stem}: {Path(p).resolve()}" for p in images)
    return f"""You are grading screenshots of an Android screen against a design rubric.

{rubric}

## Screen brief (context for the intended task, not evidence of implementation)

{case.get('prompt', '')}

## Screenshots to inspect (open each image with your available image tools)

{shots}

## Items to answer (only these)

{listed}

Answer every listed item. Reply with only the JSON object described in "Output"."""


def parse(text, item_ids):
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*|\s*```$", "", text)
    reply = json.loads(text)
    if not isinstance(reply, dict) or not isinstance(reply.get("items"), list):
        raise ValueError("judge must return an items array")
    items = {}
    for item in reply["items"]:
        if not isinstance(item, dict):
            raise ValueError("each verdict must be an object")
        key = item.get("id")
        if not isinstance(key, str) or key not in item_ids or key in items:
            raise ValueError(f"unexpected or duplicate rubric ID: {key}")
        if type(item.get("pass")) is not bool:
            raise ValueError(f"{key}: pass must be a JSON boolean")
        if not isinstance(item.get("reason"), str) or not item["reason"].strip():
            raise ValueError(f"{key}: reason must be nonempty")
        items[key] = item
    if set(items) != set(item_ids):
        raise ValueError(f"missing rubric IDs: {sorted(set(item_ids) - set(items))}")
    return items


def validate_runs(runs):
    if runs < 1 or runs % 2 == 0:
        raise ValueError("judge runs must be a positive odd number")


def aggregate(ballots, item_ids):
    validate_runs(len(ballots))
    verdict = {}
    for key in item_ids:
        passed_count = sum(ballot[key]["pass"] for ballot in ballots)
        passed = passed_count > len(ballots) // 2
        reason = next(ballot[key]["reason"] for ballot in ballots if ballot[key]["pass"] == passed)
        verdict[key] = {"pass": passed, "votes": f"{passed_count}/{len(ballots)}", "reason": reason}
    return verdict


SCHEMA = {
    "type": "object",
    "properties": {"items": {"type": "array", "items": {
        "type": "object",
        "properties": {"id": {"type": "string"}, "pass": {"type": "boolean"}, "reason": {"type": "string"}},
        "required": ["id", "pass", "reason"], "additionalProperties": False}}},
    "required": ["items"],
    "additionalProperties": False,
}


def judge_once(case_name, images, item_ids, *, backend, model=None, rubric=None):
    if not images:
        raise ValueError("cannot judge without images")
    prompt = rubric if rubric is not None else prompt_for(case_name, images, item_ids)
    reply = execute(backend, prompt, Path(images[0]).resolve().parent, role="judge",
                    images=images, schema=SCHEMA, model=model, timeout=600)
    return parse(reply["message"], item_ids)


def judge(case_name, images, item_ids=None, runs=1, *, backend, model=None, rubric=None):
    validate_runs(runs)
    item_ids = item_ids if item_ids is not None else [a["id"] for a in case_items(case_name)]
    ballots = [judge_once(case_name, images, item_ids, backend=backend, model=model, rubric=rubric)
               for _ in range(runs)]
    return aggregate(ballots, item_ids)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("case")
    parser.add_argument("images", nargs="+")
    parser.add_argument("--items")
    parser.add_argument("--runs", type=int, default=1)
    parser.add_argument("--judge", choices=BACKENDS, required=True)
    parser.add_argument("--model")
    args = parser.parse_args()
    ids = args.items.split(",") if args.items else None
    print(json.dumps(judge(args.case, args.images, ids, args.runs, backend=args.judge, model=args.model), indent=2))
