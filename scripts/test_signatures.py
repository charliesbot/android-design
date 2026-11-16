"""Checks that compose-signatures.md stays in sync with generate_signatures.py and pinned artifacts."""

import re
import unittest
from pathlib import Path

import generate_signatures as gen

ROOT = Path(__file__).resolve().parent.parent


class ComposeSignaturesTest(unittest.TestCase):
    def test_committed_signatures_match_generator(self):
        expected = gen.render_markdown()
        self.assertTrue(gen.OUT_PATH.is_file(), f"Missing {gen.OUT_PATH}")
        actual = gen.OUT_PATH.read_text(encoding="utf-8")
        self.assertEqual(actual, expected)
        self.assertLessEqual(len(actual.splitlines()), 220)
        self.assertNotIn("\u2014", actual)

    def test_all_curated_m3_symbols_exist_in_inventory(self):
        inventory = gen.load_inventory()
        missing = gen.verify_inventory(inventory)
        self.assertEqual(missing, [])

        content = gen.OUT_PATH.read_text(encoding="utf-8")
        shapes_line = next(
            line for line in content.splitlines() if line.startswith("Available `MaterialShapes`:")
        )
        for shape in re.findall(r"`([A-Z]\w+)`", shapes_line):
            if shape == "MaterialShapes":
                continue
            self.assertIn(f"MaterialShapes.{shape}", inventory)


if __name__ == "__main__":
    unittest.main()
