"""Tests how ledger routes map to m3.material.io source files. Run: python3 -m unittest discover scripts"""
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import check_coverage

SECTION = "m3.material.io: Components (A to L)"


class M3SourceTest(unittest.TestCase):
    def setUp(self):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        self.pages = Path(tmp.name)
        (self.pages / "components").mkdir()
        (self.pages / "components.md").write_text("# Components\n\n## Overview\n")
        (self.pages / "components" / "chips.md").write_text("# Chips\n\n## Overview\n\n## Specs\n\n## App bars\n")
        patcher = mock.patch.object(check_coverage, "M3_PAGES", self.pages)
        patcher.start()
        self.addCleanup(patcher.stop)

    def resolve(self, route):
        return check_coverage.source_for(route, SECTION)

    def test_page_route_maps_to_its_file(self):
        self.assertEqual(self.resolve("/components/chips"), self.pages / "components" / "chips.md")

    def test_tab_route_maps_to_its_page_when_the_page_has_that_section(self):
        self.assertEqual(self.resolve("/components/chips/specs"), self.pages / "components" / "chips.md")
        self.assertEqual(self.resolve("/components/chips/app-bars"), self.pages / "components" / "chips.md")

    def test_tab_missing_from_its_page_is_a_missing_source(self):
        self.assertFalse(self.resolve("/components/chips/guidelines").exists())

    def test_removed_page_does_not_fall_back_to_its_hub(self):
        self.assertFalse(self.resolve("/components/renamed-chips/overview").exists())
        self.assertFalse(self.resolve("/components/renamed-chips").exists())


class UnlistedSourceTest(unittest.TestCase):
    def setUp(self):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        root = Path(tmp.name)
        sources = root / "sources"
        pages = sources / "m3.material.io" / "pages"
        pages.mkdir(parents=True)
        (pages / "styles.md").write_text("# Styles\n")
        self.new_page = pages / "brand-new.md"
        self.ledger = root / "source-coverage.md"
        self.ledger.write_text("## m3.material.io: Styles\n- [x] /styles -> material-3.md (Styles)\n")
        for name, value in (("ROOT", root), ("SOURCES", sources), ("M3_PAGES", pages), ("LEDGER", self.ledger)):
            patcher = mock.patch.object(check_coverage, name, value)
            patcher.start()
            self.addCleanup(patcher.stop)

    def test_every_source_listed_passes(self):
        check_coverage.main()

    def test_new_page_without_a_ledger_row_fails(self):
        self.new_page.write_text("# Brand new\n")
        with self.assertRaises(SystemExit):
            check_coverage.main()


if __name__ == "__main__":
    unittest.main()
