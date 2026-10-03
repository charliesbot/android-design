#!/usr/bin/env python3
"""Render screenshotTest previews for one surface, without launching an emulator."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import sys


def collect(source, destination):
    if destination.is_symlink():
        raise ValueError(f'Refusing symlink output: {destination}')
    images = sorted(source.rglob('*.png')) if source.exists() else []
    if not images:
        raise ValueError(f'No screenshot previews found in {source}')
    destination.mkdir(parents=True, exist_ok=True)
    for image in images:
        relative = image.relative_to(source)
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(image, target)
        print(target)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('surface', choices=('app', 'wear'))
    args = parser.parse_args()
    root = Path(__file__).resolve().parent
    output = root / 'renders' / args.surface
    references = root / args.surface / 'src/screenshotTestDebug/reference'
    try:
        # Clear only generated outputs, before Gradle, so failures cannot show stale proposals.
        for path in (output, references):
            if path.is_symlink() or any(parent.is_symlink() for parent in path.parents if parent != root.parent):
                raise ValueError(f'Refusing symlink output path: {path}')
            if path.exists():
                shutil.rmtree(path)
        env = os.environ.copy()
        sdk = Path.home() / 'Library/Android/sdk'
        if not env.get('ANDROID_HOME') and not env.get('ANDROID_SDK_ROOT') and sdk.is_dir():
            env['ANDROID_HOME'] = str(sdk)
        subprocess.run(
            [str(root / 'gradlew'), '--no-daemon', '--console=plain',
             f':{args.surface}:updateDebugScreenshotTest'],
            cwd=root, env=env, check=True,
        )
        collect(references, output)
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        print(f'Render failed; visual quality unverified: {error}', file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
