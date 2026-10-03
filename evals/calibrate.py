#!/usr/bin/env python3
"""Calibrate an explicitly selected judge, or prepare/import provider-independent ballots."""
import argparse
import json
from pathlib import Path
import shutil

from backends import BACKENDS
from judge import judge, validate_runs
from run import make_request, import_ballot

HERE = Path(__file__).resolve().parent


def report(labels, verdicts):
    misses = 0
    for label, verdict in zip(labels, verdicts):
        for item, expected in label['expect'].items():
            got = verdict[item]
            ok = got['pass'] == expected
            misses += not ok
            print(f"{'ok  ' if ok else 'MISS'} {label['image']:40} {item:22} expected {expected}, got {got['pass']} ({got['votes']}): {got['reason'][:90]}")
    print(f"\n{'calibrated' if not misses else f'{misses} miss(es)'}")
    return 1 if misses else 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    automatic = commands.add_parser('run')
    automatic.add_argument('--judge', choices=BACKENDS, required=True)
    automatic.add_argument('--model')
    automatic.add_argument('--runs', type=int, default=3)
    for name in ('prepare', 'score'):
        commands.add_parser(name).add_argument('--out', type=Path, required=True)
    args = parser.parse_args()
    try:
        labels = json.loads((HERE / 'calibration/labels.json').read_text())['images']
        if args.command == 'run':
            validate_runs(args.runs)
            verdicts = [judge(label['case'], [str(HERE / 'calibration' / label['image'])],
                              list(label['expect']), args.runs, backend=args.judge, model=args.model)
                        for label in labels]
            return report(labels, verdicts)
        out = args.out.resolve()
        if args.command == 'prepare':
            out.mkdir(parents=True, exist_ok=False)
            cases = {c['name']: c for c in json.loads((HERE / 'evals.json').read_text())['evals']}
            for index, label in enumerate(labels):
                directory = out / str(index)
                (directory / 'renders').mkdir(parents=True)
                shutil.copy2(HERE / 'calibration' / label['image'], directory / 'renders' / Path(label['image']).name)
                case = dict(cases[label['case']])
                case['assertions'] = [a for a in case['assertions'] if a['id'] in label['expect']]
                request = make_request(case, directory, (HERE / 'rubric.md').read_text())
                (directory / 'judge-request.json').write_text(json.dumps(request, indent=2))
            # Expected labels stay out of individual judge requests.
            (out / 'labels.json').write_text(json.dumps(labels, indent=2))
            print(f'Prepared {len(labels)} independent judging requests in {out}; grade each without reading labels.json.')
            return 0
        labels = json.loads((out / 'labels.json').read_text())
        verdicts = [import_ballot(out / str(index)) for index in range(len(labels))]
        return report(labels, verdicts)
    except (OSError, ValueError, RuntimeError) as error:
        parser.exit(1, f'{error}\n')


if __name__ == '__main__':
    raise SystemExit(main())
