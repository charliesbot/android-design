"""Safety and isolation contracts for disposable design explorations."""
import importlib.util
from pathlib import Path
import tempfile
import sys
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / 'skills/android-design/scripts/explore.py'


class ExplorationTests(unittest.TestCase):
    def setUp(self):
        spec = importlib.util.spec_from_file_location('explore', SCRIPT)
        self.module = importlib.util.module_from_spec(spec)
        previous = sys.dont_write_bytecode
        sys.dont_write_bytecode = True
        try:
            spec.loader.exec_module(self.module)
        finally:
            sys.dont_write_bytecode = previous
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name).resolve()
        self.template = self.root / 'starter'
        self.template.mkdir()
        (self.template / 'settings.gradle.kts').write_text('starter')

    def test_unique_copies_leave_starter_unchanged(self):
        first = self.module.create(self.template, self.root)
        second = self.module.create(self.template, self.root)
        self.assertNotEqual(first, second)
        (first / 'settings.gradle.kts').write_text('proposal')
        self.assertEqual((second / 'settings.gradle.kts').read_text(), 'starter')
        self.assertEqual((self.template / 'settings.gradle.kts').read_text(), 'starter')

    def test_generated_artifacts_are_not_copied(self):
        for name in ('build', '.gradle', '.git', 'renders'):
            (self.template / name).mkdir()
            (self.template / name / 'artifact').write_text('cache')
        (self.template / 'local.properties').write_text('machine-specific')
        result = self.module.create(self.template, self.root)
        for name in ('build', '.gradle', '.git', 'renders', 'local.properties'):
            self.assertFalse((result / name).exists())

    def test_cleanup_only_removes_marked_workspace(self):
        result = self.module.create(self.template, self.root)
        self.module.cleanup(result, self.root)
        self.assertFalse(result.exists())
        self.assertTrue(self.template.exists())

    def test_cleanup_refuses_unmarked_root_and_nested_paths(self):
        result = self.module.create(self.template, self.root)
        nested = result / 'nested'
        nested.mkdir()
        for path in (self.root, self.template, nested):
            with self.assertRaises(ValueError):
                self.module.cleanup(path, self.root)
            self.assertTrue(path.exists())

    def test_cleanup_refuses_symlink_and_copied_marker(self):
        result = self.module.create(self.template, self.root)
        alias = self.root / 'android-design-explore-alias'
        alias.symlink_to(result, target_is_directory=True)
        with self.assertRaises(ValueError):
            self.module.cleanup(alias, self.root)
        other = self.root / 'android-design-explore-other'
        other.mkdir()
        (other / self.module.MARKER).write_text((result / self.module.MARKER).read_text())
        with self.assertRaises(ValueError):
            self.module.cleanup(other, self.root)
        self.assertTrue(result.exists())

    def test_cleanup_does_not_follow_internal_symlinks(self):
        result = self.module.create(self.template, self.root)
        (result / 'outside').symlink_to(self.template, target_is_directory=True)
        self.module.cleanup(result, self.root)
        self.assertTrue((self.template / 'settings.gradle.kts').exists())

class InstalledStarterTests(unittest.TestCase):
    def test_template_contains_only_portable_inputs(self):
        starter = SCRIPT.parent.parent / 'assets/exploration-starter'
        forbidden = {'build', '.gradle', '.kotlin', '.git', 'local.properties', 'renders', 'screenshotTestDebug', '__pycache__'}
        for path in starter.rglob('*'):
            self.assertNotIn(path.name, forbidden, str(path))
            self.assertFalse(path.is_symlink(), str(path))
        for name in ('app', 'wear', 'core/model', 'core/domain', 'core/data',
                     'core/strings', 'core/designsystem/common'):
            self.assertTrue((starter / name / 'build.gradle.kts').is_file(), name)
        for name in ('google_sans_flex', 'roboto_flex'):
            font = starter / 'core/designsystem/common/src/main/res/font' / f'{name}.ttf'
            self.assertGreater(font.stat().st_size, 1000)


if __name__ == '__main__':
    unittest.main()
