#!/usr/bin/env python3
"""Create isolated Compose explorations; remove only explicitly selected copies."""
import argparse
import json
from pathlib import Path
import shutil
import tempfile

PREFIX = 'android-design-explore-'
MARKER = '.android-design-exploration.json'
TEMP_ROOT = Path('/tmp').resolve()
TEMPLATE = Path(__file__).resolve().parents[1] / 'assets' / 'exploration-starter'


def create(template=TEMPLATE, root=TEMP_ROOT):
    template, root = Path(template).resolve(), Path(root).resolve()
    if not (template / 'settings.gradle.kts').is_file():
        raise ValueError(f'Missing starter: {template}')
    destination = Path(tempfile.mkdtemp(prefix=PREFIX, dir=root))
    try:
        shutil.copytree(
            template, destination, dirs_exist_ok=True,
            ignore=shutil.ignore_patterns('build', '.gradle', '.kotlin', '.git',
                                         'renders', 'local.properties', '__pycache__', 'screenshotTestDebug'),
        )
        (destination / MARKER).write_text(json.dumps({'version': 1, 'path': str(destination)}))
    except Exception:
        shutil.rmtree(destination)
        raise
    return destination


def cleanup(path, root=TEMP_ROOT):
    path, root = Path(path), Path(root).resolve()
    if path.is_symlink():
        raise ValueError('Refusing to remove a symlink')
    path = path.resolve()
    if path.parent != root or not path.name.startswith(PREFIX):
        raise ValueError(f'Not a direct exploration directory in {root}: {path}')
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
    commands.add_parser('create', help='Print the absolute path of a fresh temporary starter')
    remove = commands.add_parser('cleanup', help='Delete a selected exploration after user confirmation')
    remove.add_argument('path', type=Path)
    remove.add_argument('--yes', action='store_true', help='Confirm deletion of the selected directory')
    args = parser.parse_args()
    try:
        if args.command == 'create':
            print(create())
        else:
            if not args.yes:
                parser.error('Cleanup deletes source and renders; confirm with the user, then pass --yes')
            cleanup(args.path)
            print(f'Removed {args.path}')
    except (OSError, ValueError) as error:
        parser.exit(1, f'{error}\n')


if __name__ == '__main__':
    main()
