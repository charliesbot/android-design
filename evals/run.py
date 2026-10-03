#!/usr/bin/env python3
"""Agent-independent artifacts: prepare, evaluate, score; optional automated run adapters."""
import argparse
import hashlib
import re
import json
import os
import shutil
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path

from checks import run as run_checks
from judge import judge, parse, aggregate, validate_runs, SCHEMA, prompt_for as judge_prompt
from backends import BACKENDS, execute
from report import write_report, write_summary

HERE = Path(__file__).resolve().parent
SKILL_DIR = HERE.parent / "skills" / "android-design"
DEFAULT_SDK = Path.home() / "Library" / "Android" / "sdk"
CONTRACTS = {
    "phone": "Implement the screen as `@Composable fun EvalScreen()` in package `com.example.evalapp` in the :app module, replacing the placeholder in EvalScreen.kt. It must apply the app's own theme. MainActivity already calls it.",
    "wear": "Implement the screen as `@Composable fun EvalWearScreen()` in package `com.example.evalapp.wear` in the :wear module, replacing the placeholder in EvalWearScreen.kt. It must apply its own Wear theme. MainActivity already calls it.",
    "widget": "Implement the widget as `class EvalWidget : GlanceAppWidget()` in package `com.example.evalapp` in the :app module, with its receiver registered in the manifest.",
}


def restore_harness(case, project):
    """Copies the fixed previews, render test, and eval-render.sh over whatever the agent left."""
    shutil.copytree(HERE / "overlays" / case["surface"], project, dirs_exist_ok=True)


def android_sdk():
    """The SDK Gradle should use, even when the runner's shell has no ANDROID_HOME."""
    for name in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        if os.environ.get(name):
            return Path(os.environ[name])
    return DEFAULT_SDK if DEFAULT_SDK.is_dir() else None


def prepare(case, case_dir):
    # Give the agent copies, not repository paths. This is not a security sandbox.
    shutil.copytree(SKILL_DIR, case_dir / "skill")
    project = case_dir / "project"
    shutil.copytree(HERE / "template", project)
    restore_harness(case, project)
    sdk = android_sdk()
    if sdk:
        (project / "local.properties").write_text(f"sdk.dir={sdk}\n")
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


def prepare_run(cases, out):
    """Freeze inputs and prompts without starting any agent or build."""
    out = Path(out).resolve()
    out.mkdir(parents=True, exist_ok=False)
    for case in cases:
        directory = out / case['name']
        prepare(case, directory)
        (directory / 'prompt.md').write_text(prompt_for(case, directory / 'skill'))
    (out / 'rubric.md').write_text((HERE / 'rubric.md').read_text())
    (out / 'manifest.json').write_text(json.dumps({'version': 1, 'cases': cases}, indent=2))
    return out


def generate(case_dir, backend, model=None):
    reply = execute(backend, (case_dir / 'prompt.md').read_text(), case_dir / 'project',
                    role='generator', model=model)
    (case_dir / 'agent.json').write_text(json.dumps(reply, indent=2))
    return reply


def render(case, case_dir):
    """Restore the harness and render; failed builds cannot reuse old PNGs."""
    project = case_dir / 'project'
    for path in (project / 'renders', case_dir / 'renders'):
        if path.is_symlink():
            raise ValueError(f'Refusing symlink render directory: {path}')
        if path.exists():
            shutil.rmtree(path)
    restore_harness(case, project)
    try:
        result = subprocess.run(['./eval-render.sh'], cwd=project, capture_output=True,
                                text=True, timeout=1800, stdin=subprocess.DEVNULL)
        log = result.stdout + result.stderr
        (case_dir / 'render.log').write_text(log)
        if result.returncode:
            return [], f'render exited {result.returncode}: {log[-1500:]}'
    except (OSError, subprocess.TimeoutExpired) as error:
        (case_dir / 'render.log').write_text(str(error))
        return [], f'render failed: {error}'
    if (project / 'renders').exists():
        shutil.copytree(project / 'renders', case_dir / 'renders')
    return sorted(str(p.resolve()) for p in (case_dir / 'renders').glob('*.png')), None


def build(case, case_dir, backend, model=None):
    """Optional automated generation used by preference rounds as well as scored runs."""
    if case_dir.exists():
        raise FileExistsError(f'Preserving existing case directory: {case_dir}')
    project = prepare(case, case_dir)
    (case_dir / 'prompt.md').write_text(prompt_for(case, case_dir / 'skill'))
    reply = generate(case_dir, backend, model)
    images, error = render(case, case_dir)
    if error:
        raise RuntimeError(error)
    return project, reply, images


def rendered_images(case_dir):
    """Discover supported screenshot formats for requests and integrity checks."""
    return sorted(p for p in (case_dir / 'renders').glob('*')
                  if p.is_file() and p.suffix.lower() in {'.png', '.jpg', '.jpeg'})


def artifact_hashes(case_dir):
    """Bind imported grades to the exact rendered images and implementation inputs."""
    files = rendered_images(case_dir)
    for p in (case_dir / 'project').rglob('*'):
        relative = p.relative_to(case_dir / 'project')
        if any(part in {'build', '.gradle', '.kotlin', '.git', 'renders', 'screenshotTestDebug'} for part in relative.parts):
            continue
        if p.is_file() and p.name != 'local.properties':
            files.append(p)
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


def overall_status(checks, verdict):
    if any(v['pass'] is False for v in checks.values()):
        return 'failed'
    if any(v['pass'] is None for v in verdict.values()):
        return 'pending'
    return 'passed' if all(v['pass'] for v in verdict.values()) else 'failed'


def evaluate_case(case, out, judge_backend=None, judge_model=None, judge_runs=1):
    started = time.time()
    directory = out / case['name']
    images, render_error = render(case, directory)
    checks = run_checks(directory / 'project', case['surface'])
    if render_error:
        checks['C-build'] = {'pass': False, 'detail': render_error}
    (directory / 'checks.json').write_text(json.dumps(checks, indent=2))
    request = make_request(case, directory, (out / 'rubric.md').read_text())
    (directory / 'judge-request.json').write_text(json.dumps(request, indent=2))
    # An imported ballot must always be explicitly rescored against the latest request.
    verdict = {key: {'pass': None, 'votes': '0/0', 'reason': 'awaiting visual judgment'}
               for key in request['item_ids']}
    if not checks.get('C-build', {}).get('pass'):
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
            print(f'Prepared {len(cases)} cases in {out}. Read each <case>/prompt.md; edit only <case>/project.')
            return 0
        if args.command == 'score':
            results = json.loads((out / 'results.json').read_text())['cases']
            for result in results:
                if not result['checks']['C-build']['pass']:
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
