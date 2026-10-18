#!/usr/bin/env python3
"""Agent-independent artifacts: prepare, evaluate, score; optional automated run adapters."""
import argparse
import hashlib
import re
import json
import shutil
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from checks import run as run_checks
from judge import judge, parse, aggregate, validate_runs, SCHEMA, prompt_for as judge_prompt
from backends import BACKENDS, execute
from report import write_report, write_summary

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
SKILL_DIR = ROOT / "skills" / "android-design"
EXPLORE_DIR = ROOT / "skills" / "android-design-explore"
# Where prepare() copies both skills inside a case directory.
CASE_SKILLS = "skills"
CLI = EXPLORE_DIR / "gallery" / "cli.mjs"
PROPOSAL = "a"
# The views `cli.mjs render` writes, in its order. test_run.py keeps this in step with gallery/render.mjs.
REVIEW_ONLY_RENDERS = ("light-full.png", "review.png")
RENDER_NAMES = ("light", "dark", "wallpaper", "foldable", "tablet", "desktop")
GIT = ["git", "-c", "user.name=android-design-evals", "-c", "user.email=evals@example.invalid"]


def run_tool(command, **kwargs):
    """Runs a short helper command and fails loudly with its output."""
    done = subprocess.run(command, capture_output=True, text=True, stdin=subprocess.DEVNULL, timeout=120, **kwargs)
    if done.returncode:
        raise RuntimeError(f"{' '.join(map(str, command[:3]))} exited {done.returncode}: {(done.stderr or done.stdout)[-1500:]}")
    return done.stdout


def case_info(case_dir):
    return json.loads((case_dir / "case.json").read_text())


def proposal_dir(case_dir):
    return Path(case_info(case_dir)["exploration"]) / "project" / "proposals" / PROPOSAL


def prepare(case, case_dir):
    """A fresh fixture repository with an exploration whose proposal `a` holds the case assets."""
    case_dir.mkdir(parents=True)
    # Give the agent copies, not repository paths. This is not a security sandbox.
    for skill in (SKILL_DIR, EXPLORE_DIR):
        shutil.copytree(skill, case_dir / CASE_SKILLS / skill.name)
    run_tool(["git", "init", "-q"], cwd=case_dir)
    run_tool([*GIT, "commit", "-q", "--allow-empty", "-m", "Initial commit"], cwd=case_dir)
    created = run_tool(["node", str(CLI), "create", "--project", str(case_dir), "--title", case["name"]])
    lines = created.strip().splitlines()
    exploration = Path(lines[-1].strip()) if lines else None
    if exploration is None or not (exploration / "project" / "proposals" / PROPOSAL).is_dir():
        raise RuntimeError(f"cli.mjs create did not report an exploration with proposal {PROPOSAL}: {created!r}")
    for f in case.get("files", []):
        target = exploration / "project" / "proposals" / PROPOSAL / f["to"]
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy(HERE / f["from"], target)
    (case_dir / "case.json").write_text(json.dumps(
        {"exploration": str(exploration), "proposal": PROPOSAL, "title": case["name"]}, indent=2))
    return exploration


def prompt_for(case, case_dir):
    text = (HERE / "agent-prompt.md").read_text()
    return (text.replace("{{SKILL_DIR}}", str(case_dir / CASE_SKILLS / SKILL_DIR.name))
                .replace("{{EXPLORE_DIR}}", str(case_dir / CASE_SKILLS / EXPLORE_DIR.name))
                .replace("{{EXPLORATION}}", case_info(case_dir)["exploration"])
                .replace("{{BRIEF}}", case["prompt"]))


def prepare_run(cases, out):
    """Freeze inputs and prompts without starting any agent or browser."""
    out = Path(out).resolve()
    out.mkdir(parents=True, exist_ok=False)
    for case in cases:
        directory = out / case['name']
        prepare(case, directory)
        (directory / 'prompt.md').write_text(prompt_for(case, directory))
    (out / 'rubric.md').write_text((HERE / 'rubric.md').read_text())
    (out / 'manifest.json').write_text(json.dumps({'version': 1, 'cases': cases}, indent=2))
    return out


def generate(case_dir, backend, model=None):
    reply = execute(backend, (case_dir / 'prompt.md').read_text(), case_dir,
                    role='generator', model=model)
    (case_dir / 'agent.json').write_text(json.dumps(reply, indent=2))
    return reply


def render(case_dir):
    """Renders proposal `a` with the skill's `cli.mjs render` and copies the result into <case>/renders/.

    Stale renders are removed first. Returns (images, error). Lint errors (exit 2) are not an error here: they
    leave no renders and checks.py reports them as C-lint from lint.json. Only an infrastructure failure
    (Chrome, timeout) is an error.
    """
    renders = case_dir / 'renders'
    if renders.is_symlink():
        raise ValueError(f'Refusing symlink render directory: {renders}')
    if renders.exists():
        shutil.rmtree(renders)
    for stale in ('lint.json', 'render.log'):
        (case_dir / stale).unlink(missing_ok=True)
    exploration = Path(case_info(case_dir)['exploration'])
    try:
        done = subprocess.run(['node', str(CLI), 'render', str(exploration), PROPOSAL],
                              cwd=ROOT, capture_output=True, text=True, timeout=900, stdin=subprocess.DEVNULL)
    except (OSError, subprocess.TimeoutExpired) as error:
        (case_dir / 'render.log').write_text(str(error))
        return [], f'render failed: {error}'
    log = done.stdout + done.stderr
    (case_dir / 'render.log').write_text(log)
    if done.returncode == 2:
        (case_dir / 'lint.json').write_text(json.dumps({'ok': False, 'output': log}, indent=2))
        return [], None
    if done.returncode:
        return [], f'render exited {done.returncode}: {log[-1500:]}'
    (case_dir / 'lint.json').write_text(json.dumps({'ok': True}, indent=2))
    source = exploration / 'renders' / PROPOSAL
    renders.mkdir()
    # The six default views, plus <state>-light.png and <state>-tablet.png for each state the proposal declares in p-states.
    # light-full.png (the phone view at full document height) and review.png (every view on one image) are for the agent's
    # own review, not for judging.
    for path in [*sorted(source.glob('*.png')), source / 'checks.json']:
        if path.name in REVIEW_ONLY_RENDERS:
            continue
        shutil.copy(path, renders / path.name)
    return sorted(str(p.resolve()) for p in renders.glob('*.png')), None


def build(case, case_dir, backend, model=None):
    """Optional automated generation used by preference rounds as well as scored runs."""
    if case_dir.exists():
        raise FileExistsError(f'Preserving existing case directory: {case_dir}')
    prepare(case, case_dir)
    (case_dir / 'prompt.md').write_text(prompt_for(case, case_dir))
    reply = generate(case_dir, backend, model)
    images, error = render(case_dir)
    if error:
        raise RuntimeError(error)
    return case_dir, reply, images


def rendered_images(case_dir):
    """Discover supported screenshot formats for requests and integrity checks."""
    return sorted(p for p in (case_dir / 'renders').glob('*')
                  if p.is_file() and p.suffix.lower() in {'.png', '.jpg', '.jpeg'})


def artifact_hashes(case_dir):
    """Bind imported grades to the exact rendered images and the proposal files."""
    files = rendered_images(case_dir)
    if (case_dir / 'case.json').is_file():
        files += [p for p in proposal_dir(case_dir).rglob('*') if p.is_file()]
    return {str(p.relative_to(case_dir)): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(files)}


def make_request(case, case_dir, rubric):
    ids = [a['id'] for a in case['assertions'] if a['type'] == 'rubric']
    images = [str(p.resolve()) for p in rendered_images(case_dir)]
    prompt = judge_prompt(case['name'], images, ids, case=case, rubric=rubric)
    request = {'case': case['name'], 'item_ids': ids, 'images': images,
               'prompt': prompt, 'schema': SCHEMA, 'artifacts': artifact_hashes(case_dir)}
    request['request_id'] = hashlib.sha256(json.dumps(request, sort_keys=True).encode()).hexdigest()
    return request


def import_ballot(case_dir):
    request = json.loads((case_dir / 'judge-request.json').read_text())
    if request['artifacts'] != artifact_hashes(case_dir):
        raise ValueError(f'{case_dir.name}: artifacts changed; evaluate and judge again')
    ballot = json.loads((case_dir / 'judge-ballot.json').read_text())
    if ballot.get('request_id') != request['request_id']:
        raise ValueError(f'{case_dir.name}: ballot belongs to a different judging request')
    return aggregate([parse(json.dumps(ballot), request['item_ids'])], request['item_ids'])


def render_complete(images):
    return {Path(p).stem for p in images} >= set(RENDER_NAMES)


def overall_status(checks, verdict):
    if any(v['pass'] is False for v in checks.values()):
        return 'failed'
    if any(v['pass'] is None for v in verdict.values()):
        return 'pending'
    return 'passed' if all(v['pass'] for v in verdict.values()) else 'failed'


def evaluate_case(case, out, judge_backend=None, judge_model=None, judge_runs=1):
    started = time.time()
    directory = out / case['name']
    images, render_error = render(directory)
    checks = run_checks(directory, case['surface'], [a['id'] for a in case['assertions'] if a['type'] == 'code'])
    if render_error:
        checks['C-review'] = {'pass': False, 'detail': render_error}
    (directory / 'checks.json').write_text(json.dumps(checks, indent=2))
    request = make_request(case, directory, (out / 'rubric.md').read_text())
    (directory / 'judge-request.json').write_text(json.dumps(request, indent=2))
    # An imported ballot must always be explicitly rescored against the latest request.
    verdict = {key: {'pass': None, 'votes': '0/0', 'reason': 'awaiting visual judgment'}
               for key in request['item_ids']}
    if not render_complete(images):
        verdict = {key: {'pass': None, 'votes': '0/0', 'reason': 'render incomplete'} for key in verdict}
    elif judge_backend:
        try:
            verdict = judge(case['name'], images, request['item_ids'], judge_runs,
                            backend=judge_backend, model=judge_model, rubric=request['prompt'])
        except (RuntimeError, ValueError) as error:
            verdict = {key: {'pass': None, 'votes': '0/0', 'reason': f'judge error: {error}'} for key in verdict}
    (directory / 'judge.json').write_text(json.dumps(verdict, indent=2))
    agent_path = directory / 'agent.json'
    agent = json.loads(agent_path.read_text()) if agent_path.exists() else {
        'backend': 'external', 'cost': None, 'turns': None, 'message': 'Implementation supplied outside the runner.'}
    return {'name': case['name'], 'surface': case['surface'],
            'minutes': round((time.time() - started) / 60, 1), 'agent': agent,
            'judge_backend': judge_backend, 'judge_model': judge_model,
            'checks': checks, 'judge': verdict, 'renders': [Path(p).name for p in images],
            'status': overall_status(checks, verdict), 'request_id': request['request_id']}


def exit_code(results):
    if any(r['status'] == 'failed' for r in results):
        return 1
    return 2 if any(r['status'] != 'passed' for r in results) else 0


def select_cases(names):
    cases = json.loads((HERE / 'evals.json').read_text())['evals']
    if not names:
        return [c for c in cases if not c.get('parked')]
    wanted = set(names.split(','))
    unknown = wanted - {c['name'] for c in cases}
    if unknown:
        raise ValueError(f'Unknown cases: {sorted(unknown)}')
    return [c for c in cases if c['name'] in wanted]


def write_results(out, results, no_open):
    (out / 'results.json').write_text(json.dumps({'run': out.name, 'cases': results}, indent=2))
    report = write_report(out, results, HERE / 'baseline')
    print(write_summary(out, results).read_text())
    print(f'report: {report}')
    if not no_open and sys.platform == 'darwin':
        subprocess.run(['open', str(report)], check=True)


def main():
    parser = argparse.ArgumentParser(description='Prepare, evaluate, and score agent-independent design artifacts.')
    commands = parser.add_subparsers(dest='command', required=True)
    for name in ('prepare', 'evaluate', 'run', 'score'):
        command = commands.add_parser(name)
        command.add_argument('--out', required=True, type=Path)
        if name in ('prepare', 'run'):
            command.add_argument('--cases')
        if name != 'prepare':
            command.add_argument('--no-open', action='store_true')
        if name in ('evaluate', 'run'):
            command.add_argument('--parallel', type=int, default=3)
            command.add_argument('--judge', choices=BACKENDS, required=name == 'run')
            command.add_argument('--judge-model')
            command.add_argument('--judge-runs', type=int, default=1)
        if name == 'run':
            command.add_argument('--generator', choices=BACKENDS, required=True)
            command.add_argument('--generator-model')
        if name == 'score':
            command.add_argument('--judge-label', required=True, help='Who produced the imported ballots, including model if known')
    args = parser.parse_args()
    out = args.out.resolve()
    try:
        if args.command in ('evaluate', 'run'):
            validate_runs(args.judge_runs)
            if args.parallel < 1:
                raise ValueError('parallel must be positive')
            if args.judge_model and not args.judge:
                raise ValueError('--judge-model requires --judge')
        if args.command in ('prepare', 'run'):
            cases = select_cases(args.cases)
            prepare_run(cases, out)
        else:
            manifest = json.loads((out / 'manifest.json').read_text())
            if manifest.get('version') != 1:
                raise ValueError('Unsupported run manifest')
            cases = manifest['cases']
        if not cases or any(not re.fullmatch(r'[a-z0-9-]+', c['name']) for c in cases):
            raise ValueError('Invalid or empty case list')
        if args.command == 'prepare':
            print(f'Prepared {len(cases)} cases in {out}. Read each <case>/prompt.md; edit only the proposal folder it names.')
            for case in cases:
                print(f"  {case['name']}: {proposal_dir(out / case['name'])}")
            return 0
        if args.command == 'score':
            results = json.loads((out / 'results.json').read_text())['cases']
            for result in results:
                if not render_complete(result['renders']):
                    raise ValueError(f"{result['name']}: cannot score an incomplete render")
                request = json.loads((out / result['name'] / 'judge-request.json').read_text())
                if request['request_id'] != result['request_id']:
                    raise ValueError('Judging request changed since results were written; evaluate again')
                verdict = import_ballot(out / result['name'])
                result.update(judge=verdict, judge_backend=args.judge_label, judge_model=None,
                              status=overall_status(result['checks'], verdict))
                (out / result['name'] / 'judge.json').write_text(json.dumps(verdict, indent=2))
        else:
            def work(case):
                generation_error = None
                if args.command == 'run':
                    try:
                        generate(out / case['name'], args.generator, args.generator_model)
                    except RuntimeError as error:
                        generation_error = str(error)
                result = evaluate_case(case, out, None if generation_error else args.judge, args.judge_model, args.judge_runs)
                if generation_error:
                    result['checks']['C-generation'] = {'pass': False, 'detail': generation_error}
                    result['status'] = 'failed'
                return result
            with ThreadPoolExecutor(max_workers=args.parallel) as pool:
                results = list(pool.map(work, cases))
        write_results(out, results, args.no_open)
        return exit_code(results)
    except (OSError, ValueError, RuntimeError) as error:
        parser.exit(1, f'{error}\n')


if __name__ == '__main__':
    raise SystemExit(main())
