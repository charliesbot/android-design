"""Rendering output collection must be explicit and collision-free."""
import importlib.util
from pathlib import Path
import tempfile
import sys
import unittest
from unittest.mock import patch
import subprocess

SCRIPT = Path(__file__).resolve().parents[1] / 'skills/android-design/assets/exploration-starter/render.py'


class RenderTests(unittest.TestCase):
    def setUp(self):
        spec = importlib.util.spec_from_file_location('render', SCRIPT)
        self.module = importlib.util.module_from_spec(spec)
        previous = sys.dont_write_bytecode
        sys.dont_write_bytecode = True
        try:
            spec.loader.exec_module(self.module)
        finally:
            sys.dont_write_bytecode = previous
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def test_missing_images_fails(self):
        with self.assertRaises(ValueError):
            self.module.collect(self.root / 'missing', self.root / 'renders')

    def test_same_filenames_in_different_packages_are_preserved(self):
        source = self.root / 'reference'
        for package in ('a', 'b'):
            directory = source / package
            directory.mkdir(parents=True)
            (directory / 'proposal.png').write_bytes(package.encode())
        output = self.root / 'renders'
        self.module.collect(source, output)
        for package in ('a', 'b'):
            self.assertEqual((output / package / 'proposal.png').read_bytes(), package.encode())

    def test_symlink_destination_is_rejected(self):
        output = self.root / 'renders'
        output.symlink_to(self.root, target_is_directory=True)
        with self.assertRaises(ValueError):
            self.module.collect(self.root / 'reference', output)

    def test_failed_build_removes_stale_outputs(self):
        output = self.root / 'renders/app'
        references = self.root / 'app/src/screenshotTestDebug/reference'
        for folder in (output, references):
            folder.mkdir(parents=True)
            (folder / 'old.png').write_bytes(b'stale')
        with patch.object(self.module, '__file__', str(self.root / 'render.py')):
            with patch.object(sys, 'argv', ['render.py', 'app']):
                with patch.object(self.module.subprocess, 'run', side_effect=subprocess.CalledProcessError(1, 'gradle')):
                    self.assertEqual(self.module.main(), 1)
        self.assertFalse(output.exists())
        self.assertFalse(references.exists())


if __name__ == '__main__':
    unittest.main()
