#!/usr/bin/env python3
"""Create project-local Compose explorations; remove only confirmed marked copies."""
import argparse
import json
from pathlib import Path
import shutil
import subprocess
import tempfile

PREFIX = 'android-design-explore-'
MARKER = '.android-design-exploration.json'
DIRECTORY = 'design-explorations'
TEMPLATE = Path(__file__).resolve().parents[1] / 'assets' / 'exploration-starter'


def git(root, *args):
    result = subprocess.run(['git', '-C', str(root), *args], capture_output=True, text=True)
    if result.returncode:
        raise ValueError('Exploration requires an existing Git repository: ' + result.stderr.strip())
    return result.stdout.strip()


def repository(root):
    return Path(git(Path(root or Path.cwd()).resolve(), 'rev-parse', '--show-toplevel')).resolve()


def storage(root):
    destination = root / DIRECTORY
    if destination.is_symlink():
        raise ValueError('Refusing symlink exploration storage')
    return destination


def create(template=TEMPLATE, root=None, title='Design exploration'):
    template, root = Path(template).resolve(), repository(root)
    if not isinstance(title, str) or not title.strip() or len(title) > 200:
        raise ValueError('Title must contain 1 to 200 characters')
    if not (template / 'settings.gradle.kts').is_file():
        raise ValueError(f'Missing starter: {template}')
    base = storage(root)
    if git(root, 'ls-files', '--', DIRECTORY):
        raise ValueError('Exploration storage is already tracked; leave it untouched')
    # Local Git metadata keeps generated proposals out of commits without editing .gitignore.
    exclude = Path(git(root, 'rev-parse', '--path-format=absolute', '--git-path', 'info/exclude'))
    exclude.parent.mkdir(parents=True, exist_ok=True)
    existing = exclude.read_text() if exclude.exists() else ''
    rule = f'/{DIRECTORY}/'
    if rule not in existing.splitlines():
        with exclude.open('a') as output:
            output.write(('\n' if existing and not existing.endswith('\n') else '') + rule + '\n')
    base.mkdir(exist_ok=True)
    destination = Path(tempfile.mkdtemp(prefix=PREFIX, dir=base))
    try:
        shutil.copytree(
            template, destination / 'project',
            ignore=shutil.ignore_patterns('build', '.gradle', '.kotlin', '.git',
                                         'renders', 'local.properties', '__pycache__', 'screenshotTestDebug'),
        )
        for name, data in {
            MARKER: {'version': 1, 'path': str(destination)},
            'exploration.json': {'version': 1, 'title': title, 'rounds': []},
            'feedback.json': {'revision': 0, 'rounds': {}, 'approved': None},
        }.items():
            (destination / name).write_text(json.dumps(data, indent=2) + '\n')
    except Exception:
        shutil.rmtree(destination)
        raise
    return destination


def cleanup(path, root=None):
    path, root = Path(path), repository(root)
    if path.is_symlink():
        raise ValueError('Refusing to remove a symlink')
    path = path.resolve()
    if path.parent != storage(root) or not path.name.startswith(PREFIX):
        raise ValueError(f'Not a direct exploration directory in {root / DIRECTORY}: {path}')
    marker = path / MARKER
    if marker.is_symlink() or not marker.is_file():
        raise ValueError(f'Missing exploration marker: {path}')
    try:
        identity = json.loads(marker.read_text())
    except (OSError, ValueError) as error:
        raise ValueError(f'Invalid exploration marker: {path}') from error
    if identity != {'version': 1, 'path': str(path)}:
        raise ValueError(f'Exploration marker does not match: {path}')
    shutil.rmtree(path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    new = commands.add_parser('create', help='Copy the starter under the requesting repository')
    new.add_argument('--project', type=Path, default=Path.cwd(), help='Requesting Git repository')
    new.add_argument('--title', default='Design exploration')
    remove = commands.add_parser('cleanup', help='Delete a selected exploration after user confirmation')
    remove.add_argument('path', type=Path)
    remove.add_argument('--project', type=Path, default=Path.cwd())
    remove.add_argument('--yes', action='store_true', help='Confirm deletion of the selected directory')
    args = parser.parse_args()
    try:
        if args.command == 'create':
            print(create(root=args.project, title=args.title))
        else:
            if not args.yes:
                parser.error('Cleanup deletes source, feedback, history, and renders; confirm with the user, then pass --yes')
            cleanup(args.path, args.project)
            print(f'Removed {args.path}')
    except (OSError, ValueError) as error:
        parser.exit(1, f'{error}\n')


if __name__ == '__main__':
    main()
