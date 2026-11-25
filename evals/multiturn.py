#!/usr/bin/env python3
"""Multi-turn revision benchmark (10 sequential revisions in one resumed Claude session)."""
import argparse
import datetime
import json
import os
import shutil
import statistics
import subprocess
import time
from pathlib import Path

from backends import execute
from checks import run as run_checks
from preview_app import DEFAULT_EVAL_BACKEND, app_binary, bootstrap_dir, preview_src, server_cp
import run as eval_run

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
FIXTURE_DIR = HERE / 'multiturn_fixture'
FIXTURE_ASSETS = ('recipe_shakshuka.jpg', 'recipe_pasta.jpg', 'recipe_ramen.jpg', 'recipe_salad.jpg')
DEFAULT_BASELINE_PATH = HERE / 'baseline' / 'v4-multiturn-baseline.json'

REVISIONS = (
    (
        'Layout shift (Editorial split)',
        'In the "Editorial split" direction, turn the featured dish card into a split horizontal hero '
        'on tablet and desktop widths (photo on the left, title, subtitle, and primary button on the right) '
        'while keeping the stacked vertical hero on phone and foldable widths.',
    ),
    (
        'Typography emphasis (Editorial split)',
        'In "Editorial split", make the featured dish title use displaySmallEmphasized (keeping the brand serif '
        'face) and tighten the subtitle underneath to a single crisp line with ellipsis overflow.',
    ),
    (
        'Targeted element tweak (Editorial split)',
        'In "Editorial split", add a compact metadata row showing prep time ("25 min") and servings '
        '("4 servings") right above the "Start cooking" button, while keeping "Start cooking" visually larger '
        'than every peer control.',
    ),
    (
        'Theme and contrast adjustment (shared)',
        'Shift the board seed color from warm terracotta to deep forest olive (#3F6240 / 0xFF3F6240L) with '
        'PaletteVariant.TonalSpot in designBoard(...), and confirm light and dark views pass '
        'contrast checks.',
    ),
    (
        'Layout shift (Quick pantry)',
        'In the "Quick pantry" direction, make the filter chip row horizontally scrollable and arrange the dish '
        'rows in a two-column layout on foldable, tablet, and desktop widths while staying single-column on phone.',
    ),
    (
        'Targeted element tweak (Quick pantry)',
        'In "Quick pantry", add a bookmark IconButton (using SymbolIcon("bookmark") with at least a 48 dp touch '
        'target) to each dish card or row without clipping or truncating the dish title.',
    ),
    (
        'Adaptive fix (Editorial split detail pane)',
        'In the "Editorial split" detail pane, show the Ingredients card and Steps card side by side on tablet '
        'and desktop widths (keeping each card capped to 420 dp max width so line length stays under 60 chars) '
        'while stacking them vertically on narrower widths.',
    ),
    (
        'Typography emphasis (Quick pantry)',
        'In "Quick pantry", render each dish cook time ("25 min", "20 min", "35 min", "15 min") using '
        'bundledFontFamily(BundledFont.GoogleSansCode) so the numeric durations align cleanly.',
    ),
    (
        'Theme and contrast adjustment (Editorial split)',
        'Add a subtle tag pill (e.g. "Skillet") inside the featured dish hero in "Editorial split" using '
        'secondaryContainer and onSecondaryContainer tokens so it stays legible in both light and dark modes '
        'without raw Color literals or .copy(alpha = ...).',
    ),
    (
        'Final polish and full verification',
        'Tighten spacing rhythm across both directions, verify every view (light, dark, foldable, tablet, desktop) '
        'passes all checks with zero error findings, and inspect the final renders.',
    ),
)


def _git_head_sha():
    try:
        done = subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=ROOT, capture_output=True, text=True, timeout=10)
        return done.stdout.strip() if done.returncode == 0 else None
    except (OSError, subprocess.TimeoutExpired):
        return None


def prepare_multiturn(out_dir):
    """Create a fresh multi-turn exploration fixture repository."""
    out_dir = Path(out_dir).resolve()
    out_dir.mkdir(parents=True, exist_ok=False)
    for skill in (eval_run.SKILL_DIR, eval_run.EXPLORE_DIR):
        shutil.copytree(skill, out_dir / eval_run.CASE_SKILLS / skill.name)
    shutil.copy(FIXTURE_DIR / 'DESIGN.md', out_dir / 'DESIGN.md')
    design_dir = out_dir / 'design'
    assets_dir = design_dir / 'assets'
    assets_dir.mkdir(parents=True)
    shutil.copy(FIXTURE_DIR / 'design' / 'proposal.explore.kt', design_dir / 'proposal.explore.kt')
    for asset in FIXTURE_ASSETS:
        shutil.copy(HERE / 'assets' / asset, assets_dir / asset)
    (out_dir / 'case.json').write_text(json.dumps({
        'proposal': str(design_dir / 'proposal.explore.kt'),
        'title': 'multiturn-recipes',
    }, indent=2))
    eval_run.run_tool(['git', 'init', '-q'], cwd=out_dir)
    eval_run.run_tool([*eval_run.GIT, 'add', '.'], cwd=out_dir)
    eval_run.run_tool([*eval_run.GIT, 'commit', '-q', '-m', 'Initial fixture'], cwd=out_dir)
    return out_dir / 'design' / 'proposal.explore.kt'


def first_turn_prompt(out_dir, revision_text, total_turns):
    template = (HERE / 'multiturn-prompt.md').read_text()
    return (
        template
        .replace('{{SKILL_DIR}}', str(out_dir / eval_run.CASE_SKILLS / eval_run.SKILL_DIR.name))
        .replace('{{EXPLORE_DIR}}', str(out_dir / eval_run.CASE_SKILLS / eval_run.EXPLORE_DIR.name))
        .replace('{{PROPOSAL}}', str(out_dir / 'design' / 'proposal.explore.kt'))
        .replace('{{DESIGN_MD}}', str(out_dir / 'DESIGN.md'))
        .replace('{{TOTAL_TURNS}}', str(total_turns))
        .replace('{{REVISION}}', revision_text)
    )


def followup_turn_prompt(turn_number, total_turns, title, revision_text):
    verify = (
        'run ONE unscoped `android-design-preview check` across all views and open its returned `light` and `desktop` PNGs'
        if turn_number == total_turns
        else 'run ONE scoped `android-design-preview check` on the single changed direction, screen (if multiple), and view, and open that single returned 1x PNG to verify the visual change'
    )
    return (
        f'Revision {turn_number} of {total_turns} ({title}):\n\n'
        f'{revision_text}\n\n'
        f'Apply this edit to the exploration, {verify}, and reply briefly (under 100 words) with what changed.'
    )


def _int_or_zero(value):
    return value if isinstance(value, (int, float)) else 0


def _turn_record(index, title, reply, *, prev_cumulative_cost=None):
    inp = reply.get('input_tokens')
    cache_read = reply.get('cache_read_tokens')
    cache_create = reply.get('cache_creation_tokens')
    out = reply.get('output_tokens')
    has_input = any(isinstance(v, (int, float)) for v in (inp, cache_read, cache_create))
    total_input = int(_int_or_zero(inp) + _int_or_zero(cache_read) + _int_or_zero(cache_create)) if has_input else None
    total_tokens = (total_input + int(out)) if (total_input is not None and isinstance(out, (int, float))) else None
    raw_cost = reply.get('cost')
    turn_cost = raw_cost
    if isinstance(raw_cost, (int, float)) and isinstance(prev_cumulative_cost, (int, float)):
        # Claude CLI's total_cost_usd is cumulative across --resume turns; take the per-turn delta.
        if raw_cost >= prev_cumulative_cost:
            turn_cost = round(float(raw_cost - prev_cumulative_cost), 6)
    return {
        'turn': index,
        'title': title,
        'wall_seconds': reply.get('wall_seconds'),
        'agent_turns': reply.get('turns'),
        'input_tokens': inp,
        'cache_read_tokens': cache_read,
        'cache_creation_tokens': cache_create,
        'total_input_tokens': total_input,
        'output_tokens': out,
        'total_tokens': total_tokens,
        'cost': turn_cost,
        'cumulative_cost': raw_cost,
        'check_calls': reply.get('check_calls'),
        'compile_retries': reply.get('compile_retries'),
        'skill_bytes_read': reply.get('skill_bytes_read'),
        'skill_read_calls': reply.get('skill_read_calls'),
        'skill_unique_files': reply.get('skill_unique_files'),
        'skill_files_read': reply.get('skill_files_read') or {},
        'model': reply.get('model'),
    }


def _stat_pair(values, round_digits=2):
    nums = [v for v in values if isinstance(v, (int, float))]
    if not nums:
        return {'mean': None, 'median': None}
    return {
        'mean': round(float(statistics.mean(nums)), round_digits),
        'median': round(float(statistics.median(nums)), round_digits),
    }


def _sum_optional(values, round_digits=None):
    nums = [v for v in values if isinstance(v, (int, float))]
    if not nums:
        return None
    total = sum(nums)
    if round_digits is not None:
        return round(float(total), round_digits)
    return int(total) if all(isinstance(v, int) for v in nums) else round(float(total), 2)


def aggregate_multiturn(turn_records, *, model=None, commit_sha=None, pipeline='v6', final_checks=None, captured_at=None):
    """Aggregate per-turn records into session totals and per-turn means/medians."""
    merged_files = {}
    for rec in turn_records:
        for rel, entry in (rec.get('skill_files_read') or {}).items():
            dst = merged_files.setdefault(rel, {'reads': 0, 'bytes': 0})
            dst['reads'] += int(entry.get('reads', 0))
            dst['bytes'] += int(entry.get('bytes', 0))

    resolved_model = model or next((r['model'] for r in turn_records if r.get('model')), None)
    totals = {
        'wall_seconds': _sum_optional([r.get('wall_seconds') for r in turn_records], round_digits=2),
        'agent_turns': _sum_optional([r.get('agent_turns') for r in turn_records]),
        'input_tokens': _sum_optional([r.get('input_tokens') for r in turn_records]),
        'cache_read_tokens': _sum_optional([r.get('cache_read_tokens') for r in turn_records]),
        'cache_creation_tokens': _sum_optional([r.get('cache_creation_tokens') for r in turn_records]),
        'total_input_tokens': _sum_optional([r.get('total_input_tokens') for r in turn_records]),
        'output_tokens': _sum_optional([r.get('output_tokens') for r in turn_records]),
        'total_tokens': _sum_optional([r.get('total_tokens') for r in turn_records]),
        'cost': _sum_optional([r.get('cost') for r in turn_records], round_digits=4),
        'check_calls': _sum_optional([r.get('check_calls') for r in turn_records]),
        'compile_retries': _sum_optional([r.get('compile_retries') for r in turn_records]),
        'skill_bytes_read': _sum_optional([r.get('skill_bytes_read') for r in turn_records]),
        'skill_read_calls': _sum_optional([r.get('skill_read_calls') for r in turn_records]),
        'skill_unique_files': len(merged_files),
        'skill_files_read': dict(sorted(merged_files.items())),
    }
    per_turn = {
        'wall_seconds': _stat_pair([r.get('wall_seconds') for r in turn_records], round_digits=2),
        'input_tokens': _stat_pair([r.get('input_tokens') for r in turn_records], round_digits=1),
        'total_input_tokens': _stat_pair([r.get('total_input_tokens') for r in turn_records], round_digits=1),
        'output_tokens': _stat_pair([r.get('output_tokens') for r in turn_records], round_digits=1),
        'total_tokens': _stat_pair([r.get('total_tokens') for r in turn_records], round_digits=1),
        'check_calls': _stat_pair([r.get('check_calls') for r in turn_records], round_digits=2),
        'compile_retries': _stat_pair([r.get('compile_retries') for r in turn_records], round_digits=2),
        'cost': _stat_pair([r.get('cost') for r in turn_records], round_digits=4),
    }
    final_checks_pass = None
    if isinstance(final_checks, dict):
        final_checks_pass = all(v.get('pass') is True for v in final_checks.values())
    return {
        'pipeline': pipeline,
        'backend': 'claude',
        'model': resolved_model,
        'commit_sha': commit_sha,
        'captured_at': captured_at or datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'),
        'turns_count': len(turn_records),
        'final_checks_pass': final_checks_pass,
        'final_checks': final_checks,
        'totals': totals,
        'per_turn': per_turn,
        'turns': turn_records,
    }


def format_summary(report):
    t = report['totals']
    p = report['per_turn']
    lines = [
        f"# Multi-turn revision benchmark ({report['pipeline']})",
        '',
        f"- Backend: `{report['backend']}` | Model: `{report['model'] or '-'}` | Commit: `{report['commit_sha'] or '-'}`",
        f"- Turns: {report['turns_count']} | Final checks pass: `{report['final_checks_pass']}`",
        f"- Wall time: total `{t['wall_seconds']}s`, mean/turn `{p['wall_seconds']['mean']}s`, median/turn `{p['wall_seconds']['median']}s`",
        f"- Tokens/turn (mean): total `{p['total_tokens']['mean']}`, input+cache `{p['total_input_tokens']['mean']}`, output `{p['output_tokens']['mean']}`",
        f"- Check calls: total `{t['check_calls']}`, compile retries: `{t['compile_retries']}`",
        f"- Skill bytes read: `{t['skill_bytes_read']}` across `{t['skill_read_calls']}` reads (`{t['skill_unique_files']}` unique files)",
        '',
        '| Turn | Revision | Wall (s) | In+Cache tok | Out tok | Total tok | Checks | Retries | Skill B |',
        '| --- | --- | --- | --- | --- | --- | --- | --- | --- |',
    ]
    for rec in report['turns']:
        lines.append(
            f"| {rec['turn']} | {rec['title']} | {rec['wall_seconds']} | {rec['total_input_tokens']} "
            f"| {rec['output_tokens']} | {rec['total_tokens']} | {rec['check_calls']} "
            f"| {rec['compile_retries']} | {rec['skill_bytes_read']} |"
        )
    return '\n'.join(lines) + '\n'


def run_multiturn(out_dir, *, model=None, max_turns=None, pipeline='v6', timeout=1200,
                  binary=None, src=None, bdir=None, backend=DEFAULT_EVAL_BACKEND, cache_dir=None):
    out_dir = Path(out_dir).resolve()
    prepare_multiturn(out_dir)
    revisions = REVISIONS[:max_turns] if max_turns is not None else REVISIONS
    total = len(revisions)
    mcp_binary = app_binary() if binary is None else Path(binary)
    resolved_src = preview_src() if src is None else Path(src)
    resolved_bdir = bootstrap_dir() if bdir is None else Path(bdir)
    if bdir is None:
        server_cp()

    created_cache = cache_dir is None
    active_cache_dir = (
        Path(cache_dir).resolve()
        if cache_dir is not None
        else (Path.home() / '.cache' / 'android-design-preview' / f'eval-mt-{os.getpid()}-{int(time.time())}').resolve()
    )
    active_cache_dir.mkdir(parents=True, exist_ok=True)

    session_id = None
    prev_cumulative_cost = None
    turn_records = []
    try:
        for idx, (title, body) in enumerate(revisions, start=1):
            prompt = (
                first_turn_prompt(out_dir, body, total)
                if idx == 1
                else followup_turn_prompt(idx, total, title, body)
            )
            reply = execute(
                'claude', prompt, out_dir,
                role='generator', model=model, timeout=timeout,
                mcp_binary=mcp_binary,
                preview_src=resolved_src,
                bootstrap_dir=resolved_bdir,
                preview_backend=backend,
                cache_dir=active_cache_dir,
                client_timeout_ms=120000,
                resume_session=session_id,
            )
            if idx == 1:
                session_id = reply.get('session_id')
                if not session_id:
                    raise RuntimeError('Claude did not return a session_id on turn 1; cannot resume session')
            (out_dir / f'turn-{idx:02d}.json').write_text(json.dumps(reply, indent=2))
            rec = _turn_record(idx, title, reply, prev_cumulative_cost=prev_cumulative_cost)
            if isinstance(reply.get('cost'), (int, float)):
                prev_cumulative_cost = reply['cost']
            turn_records.append(rec)
            print(
                f"[turn {idx:02d}/{total}] {title}: {rec['wall_seconds']}s, "
                f"in+cache={rec['total_input_tokens']}, out={rec['output_tokens']}, "
                f"checks={rec['check_calls']}, retries={rec['compile_retries']}, skill_B={rec['skill_bytes_read']}",
                flush=True,
            )

        images, render_error = eval_run.render(
            out_dir,
            binary=mcp_binary,
            src=resolved_src,
            bdir=resolved_bdir,
            cache_dir=active_cache_dir,
        )
    finally:
        eval_run._stop_preview_server(
            out_dir,
            mcp_binary,
            src=resolved_src,
            bdir=resolved_bdir,
            backend=backend,
            cache_dir=active_cache_dir,
        )
        if created_cache:
            shutil.rmtree(active_cache_dir, ignore_errors=True)
    checks = run_checks(out_dir, 'phone')
    if render_error:
        checks['C-review'] = {'pass': False, 'detail': render_error}
    (out_dir / 'checks.json').write_text(json.dumps(checks, indent=2))

    report = aggregate_multiturn(
        turn_records,
        model=model,
        commit_sha=_git_head_sha(),
        pipeline=pipeline,
        final_checks=checks,
    )
    report['session_id'] = session_id
    report['rendered_images'] = len(images)
    (out_dir / 'multiturn-results.json').write_text(json.dumps(report, indent=2) + '\n')
    summary = format_summary(report)
    (out_dir / 'multiturn-summary.md').write_text(summary)
    return report


def main():
    parser = argparse.ArgumentParser(description='Run the 10-turn revision eval benchmark on Claude.')
    parser.add_argument('--out', required=True, type=Path, help='Output directory for the fixture and turn artifacts')
    parser.add_argument('--model', help='Optional Claude model override')
    parser.add_argument('--turns', type=int, default=len(REVISIONS), help='Number of revision turns to run (default: 10)')
    parser.add_argument('--pipeline', default='v6', choices=('v4', 'v5', 'v6'), help='Label recorded in the report')
    parser.add_argument('--timeout', type=int, default=1200, help='Per-turn timeout in seconds')
    parser.add_argument('--write-baseline', type=Path, help='Copy aggregated metrics JSON to this committed baseline path')
    args = parser.parse_args()
    if args.turns < 1 or args.turns > len(REVISIONS):
        parser.error(f'--turns must be between 1 and {len(REVISIONS)}')
    report = run_multiturn(args.out, model=args.model, max_turns=args.turns, pipeline=args.pipeline, timeout=args.timeout)
    print(format_summary(report))
    if args.write_baseline:
        args.write_baseline.parent.mkdir(parents=True, exist_ok=True)
        baseline = {k: v for k, v in report.items() if k != 'session_id'}
        args.write_baseline.write_text(json.dumps(baseline, indent=2) + '\n')
        print(f'Wrote baseline to {args.write_baseline}')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
