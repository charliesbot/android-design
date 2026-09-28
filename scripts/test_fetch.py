"""Tests that a fetch never replaces saved sources with a suspiciously small result. Run: python3 -m unittest discover scripts"""
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import fetch


class WriteAllGuardTest(unittest.TestCase):
    def setUp(self):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        self.root = Path(tmp.name)
        patcher = mock.patch.object(fetch, "ROOT", self.root)
        patcher.start()
        self.addCleanup(patcher.stop)
        self.folder = self.root / "sources" / "site"
        self.folder.mkdir(parents=True)
        for name in ("a", "b", "c", "d"):
            (self.folder / f"{name}.md").write_text(f"# {name}\n")

    def saved(self):
        return sorted(p.name for p in self.folder.glob("*.md"))

    def test_empty_fetch_keeps_saved_pages(self):
        with self.assertRaises(SystemExit):
            fetch.write_all(self.folder, {})
        self.assertEqual(self.saved(), ["a.md", "b.md", "c.md", "d.md"])

    def test_fetch_with_under_half_the_pages_keeps_saved_pages(self):
        with self.assertRaises(SystemExit):
            fetch.write_all(self.folder, {"a.md": "# a"})
        self.assertEqual(self.saved(), ["a.md", "b.md", "c.md", "d.md"])

    def test_allow_shrink_accepts_a_real_removal(self):
        fetch.write_all(self.folder, {"a.md": "# a"}, allow_shrink=True)
        self.assertEqual(self.saved(), ["a.md"])

    def test_normal_fetch_replaces_pages_and_records_removals(self):
        fetch.write_all(self.folder, {"a.md": "# a", "b.md": "# b", "e.md": "# e"})
        self.assertEqual(self.saved(), ["a.md", "b.md", "e.md"])

    def test_first_fetch_into_an_empty_folder_is_allowed(self):
        fresh = self.root / "sources" / "new"
        fetch.write_all(fresh, {"a.md": "# a"})
        self.assertEqual([p.name for p in fresh.glob("*.md")], ["a.md"])


if __name__ == "__main__":
    unittest.main()
