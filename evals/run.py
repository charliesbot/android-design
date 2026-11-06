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

from checks import read_report, run as run_checks
from judge import judge, parse, aggregate, validate_runs, SCHEMA, prompt_for as judge_prompt
from backends import BACKENDS, execute
from preview_app import app_binary
from report import label, write_report, write_summary

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
SKILL_DIR = ROOT / "skills" / "android-design"
EXPLORE_DIR = ROOT / "skills" / "android-design-explore"
# Where prepare() copies both skills inside a case directory.
CASE_SKILLS = "skills"
# Names inside a case directory. The agent writes DESIGN.md and the proposal; `render` writes renders/.
PROPOSAL = "design/proposal.proposal.json"
DESIGN_MD = "DESIGN.md"
GIT = ["git", "-c", "user.name=android-design-evals", "-c", "user.email=evals@example.invalid"]


def run_tool(command, **kwargs):
    """Runs a short helper command and fails loudly with its output."""
    done = subprocess.run(command, capture_output=True, text=True, stdin=subprocess.DEVNULL, timeout=120, **kwargs)
    if done.returncode:
        raise RuntimeError(f"{' '.join(map(str, command[:3]))} exited {done.returncode}: {(done.stderr or done.stdout)[-1500:]}")
    return done.stdout


def case_info(case_dir):
    return json.loads((case_dir / "case.json").read_text())


def proposal_path(case_dir):
    return Path(case_info(case_dir)["proposal"])


def prepare(case, case_dir):
    """A fresh fixture repository with design/ holding the brief's assets. The agent writes the proposal and DESIGN.md."""
    case_dir.mkdir(parents=True)
    # Give the agent copies, not repository paths. This is not a security sandbox.
    for skill in (SKILL_DIR, EXPLORE_DIR):
        shutil.copytree(skill, case_dir / CASE_SKILLS / skill.name)
    run_tool(["git", "init", "-q"], cwd=case_dir)
    run_tool([*GIT, "commit", "-q", "--allow-empty", "-m", "Initial commit"], cwd=case_dir)
    proposal = case_dir / PROPOSAL
    for f in case.get("files", []):
        target = proposal.parent / f["to"]
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy(HERE / f["from"], target)
    proposal.parent.mkdir(exist_ok=True)
    (case_dir / "case.json").write_text(json.dumps(
        {"proposal": str(proposal), "title": case["name"]}, indent=2))
    return proposal


# What the agent prompt says per surface: where the proposal must work, what check_proposal covers, and how to
# read its layout summaries. Phone-size cases cover the window size classes; Wear cases cover the two round watches.
SURFACE_PROMPT = {
    'phone': {
        'TARGET': 'The screen must work and adapt across phone (411x923), foldable (704x933), tablet (1280x800), and desktop (1920x1080) windows, in light and dark.',
        'VIEWS': 'all five views',
        'LAYOUT': "Read each view's `layout` summary in the `check_proposal` result (navigation, panes, and regions). Check that the larger views reveal what your section 0 adaptation answer promised, rather than rearranging the phone content. Call `get_tree` only when a summary is not enough.",
        'DONE': 'implement the brief and adapt across all four window sizes',
    },
    'wear': {
        'TARGET': 'This is a Wear OS proposal: every screen uses the Wear catalog (`get_catalog` lists it), and the previewer draws it on two round, dark watches: small (204x204) and large (227x227). The screen must work on both. Design for the small one first; the large one may show more but never less.',
        'VIEWS': 'both watch views',
        'LAYOUT': "Read each view's `layout` summary in the `check_proposal` result. Check that nothing important sits where the round edge cuts it, and that the large watch shows at least what the small one does. Call `get_tree` only when a summary is not enough.",
        'DONE': 'implement the brief on the small and the large watch',
    },
}


def prompt_for(case, case_dir):
    text = (HERE / "agent-prompt.md").read_text()
    for key, value in SURFACE_PROMPT[case['surface']].items():
        text = text.replace('{{' + key + '}}', value)
    return (text.replace("{{SKILL_DIR}}", str(case_dir / CASE_SKILLS / SKILL_DIR.name))
                .replace("{{EXPLORE_DIR}}", str(case_dir / CASE_SKILLS / EXPLORE_DIR.name))
                .replace("{{PROPOSAL}}", case_info(case_dir)["proposal"])
                .replace("{{DESIGN_MD}}", str(case_dir / DESIGN_MD))
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
                    role='generator', model=model, mcp_binary=app_binary())
    (case_dir / 'agent.json').write_text(json.dumps(reply, indent=2))
    return reply


def render(case_dir):
    """Renders the proposal headlessly with the previewer app into <case>/renders/ (PNGs, trees, report.json).

    Stale renders are removed first. Returns (images, error). An invalid proposal (exit 2) or failing findings
    (exit 1) are not errors here: report.json records them and checks.py reports them. Only an infrastructure
    failure (no report.json, timeout) is an error. A missing app raises RuntimeError.
    """
    renders = case_dir / 'renders'
    if renders.is_symlink():
        raise ValueError(f'Refusing symlink render directory: {renders}')
    if renders.exists():
        shutil.rmtree(renders)
    (case_dir / 'render.log').unlink(missing_ok=True)
    command = [str(app_binary()), 'render', str(proposal_path(case_dir)), '--out', str(renders)]
    design = case_dir / DESIGN_MD
    if design.is_file():
        command += ['--design', str(design)]
    try:
        done = subprocess.run(command, cwd=case_dir, capture_output=True, text=True, timeout=900,
                              stdin=subprocess.DEVNULL)
    except (OSError, subprocess.TimeoutExpired) as error:
        (case_dir / 'render.log').write_text(str(error))
        return [], f'render failed: {error}'
    log = done.stdout + done.stderr
    (case_dir / 'render.log').write_text(log)
    if done.returncode not in (0, 1, 2) or not (renders / 'report.json').is_file():
        return [], f'render exited {done.returncode} without report.json: {log[-1500:]}'
    return [r['png_path'] for r in report_renders(case_dir)], None


def report_renders(case_dir):
    """The renders listed in renders/report.json whose PNG exists, each with its absolute `png_path`."""
    listed = (read_report(case_dir) or {}).get('renders') or []
    found = []
    for entry in listed:
        png = (case_dir / 'renders' / str(entry.get('png', ''))).resolve()
        if png.is_file():
            found.append({**entry, 'png_path': str(png)})
    return found


def image_labels(case_dir):
    """Maps each rendered PNG's absolute path to `screen/view`, for the judge request."""
    return {r['png_path']: label(r) for r in report_renders(case_dir)}


def build(case, case_dir, backend, model=None):
    """Optional automated generation for a scored run."""
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
        files += [p for p in (case_dir / 'design').rglob('*') if p.is_file()]
        files += [p for p in [case_dir / DESIGN_MD] if p.is_file()]
    return {str(p.relative_to(case_dir)): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(files)}


def make_request(case, case_dir, rubric):
    ids = [a['id'] for a in case['assertions'] if a['type'] == 'rubric']
    images = [str(p.resolve()) for p in rendered_images(case_dir)]
    prompt = judge_prompt(case['name'], images, ids, case=case, rubric=rubric, labels=image_labels(case_dir))
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


def render_complete(renders):
    """Rendered at all: the report lists at least one render. Which screens and views exist is the app's business."""
    return bool(renders)


def pending(ids, reason):
    """A verdict that awaits visual judgment, for every rubric ID."""
    return {key: {'pass': None, 'votes': '0/0', 'reason': reason} for key in ids}


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
    renders = [{k: r.get(k) for k in ('screen', 'view', 'png', 'pass')} for r in report_renders(directory)]
    checks = run_checks(directory, case['surface'])
    if render_error:
        checks['C-review'] = {'pass': False, 'detail': render_error}
    (directory / 'checks.json').write_text(json.dumps(checks, indent=2))
    request = make_request(case, directory, (out / 'rubric.md').read_text())
    (directory / 'judge-request.json').write_text(json.dumps(request, indent=2))
    # An imported ballot must always be explicitly rescored against the latest request.
    ids = request['item_ids']
    if not render_complete(renders):
        verdict = pending(ids, 'render incomplete')
    elif judge_backend:
        try:
            verdict = judge(case['name'], images, ids, judge_runs,
                            backend=judge_backend, model=judge_model, prompt=request['prompt'])
        except (RuntimeError, ValueError) as error:
            verdict = pending(ids, f'judge error: {error}')
    else:
        verdict = pending(ids, 'awaiting visual judgment')
    (directory / 'judge.json').write_text(json.dumps(verdict, indent=2))
    agent_path = directory / 'agent.json'
    agent = json.loads(agent_path.read_text()) if agent_path.exists() else {
        'backend': 'external', 'cost': None, 'turns': None, 'message': 'Implementation supplied outside the runner.'}
    return {'name': case['name'], 'surface': case['surface'],
            'minutes': round((time.time() - started) / 60, 1), 'agent': agent,
            'judge_backend': judge_backend, 'judge_model': judge_model,
            'checks': checks, 'judge': verdict, 'renders': renders,
            'status': overall_status(checks, verdict), 'request_id': request['request_id']}


def exit_code(results):
    if any(r['status'] == 'failed' for r in results):
        return 1
    return 2 if any(r['status'] != 'passed' for r in results) else 0


def select_cases(names):
    cases = json.loads((HERE / 'evals.json').read_text())['evals']
    if not names:
        return cases
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
            print(f'Prepared {len(cases)} cases in {out}. Read each <case>/prompt.md; the agent writes only the DESIGN.md and design/ proposal it names.')
            for case in cases:
                print(f"  {case['name']}: {proposal_path(out / case['name'])}")
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
