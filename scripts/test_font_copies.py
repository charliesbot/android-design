"""Checks that the Compose sandbox and the previewer app bundle the same fonts. Run: python3 -m unittest discover scripts"""
import hashlib
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STARTER_FONTS = ROOT / "evals/compose-starter/core/designsystem/common/src/main/res/font"
APP_FONTS = ROOT / "app/features/preview/desktop/src/main/resources/fonts"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


class FontCopiesTest(unittest.TestCase):
    def test_starter_fonts_match_the_app_copies(self):
        starter = sorted(p.name for p in STARTER_FONTS.glob("*.ttf"))
        self.assertEqual(len(starter), 6)
        for name in starter:
            with self.subTest(font=name):
                self.assertEqual(digest(STARTER_FONTS / name), digest(APP_FONTS / name))


if __name__ == "__main__":
    unittest.main()
