"""Checks that :host bundles the full set of required fonts and licenses. Run: python3 -m unittest discover scripts"""
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
APP_FONTS = ROOT / "app/host/src/main/assets/fonts"

EXPECTED_FONTS = {
    "big_shoulders_display.ttf",
    "bricolage_grotesque.ttf",
    "doto.ttf",
    "fraunces.ttf",
    "google_sans_code.ttf",
    "google_sans_flex.ttf",
    "material_symbols_rounded.ttf",
    "roboto_flex.ttf",
}

EXPECTED_LICENSES = {
    "big-shoulders-display-OFL.txt",
    "bricolage-grotesque-OFL.txt",
    "doto-OFL.txt",
    "fraunces-OFL.txt",
    "google-sans-code-OFL.txt",
    "google-sans-flex-OFL.txt",
    "material-symbols-rounded-Apache-2.0.txt",
    "roboto-flex-OFL.txt",
}


class BundledFontsTest(unittest.TestCase):
    def test_bundled_fonts_and_licenses_are_present(self):
        actual_fonts = {p.name for p in APP_FONTS.glob("*.ttf")}
        self.assertEqual(actual_fonts, EXPECTED_FONTS)
        for name in sorted(EXPECTED_FONTS):
            self.assertGreater((APP_FONTS / name).stat().st_size, 0, name)

        actual_licenses = {p.name for p in (APP_FONTS / "licenses").glob("*.txt")}
        self.assertEqual(actual_licenses, EXPECTED_LICENSES)


if __name__ == "__main__":
    unittest.main()
