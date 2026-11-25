"""Checks that compose-signatures.md stays in sync with generate_signatures.py and pinned artifacts."""

import io
import re
import tempfile
import unittest
import zipfile
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

    def test_kit_enums_list_only_enum_entries_not_their_arguments(self):
        _, fonts = gen.extract_kit_enums()
        self.assertEqual(
            fonts,
            [
                "GoogleSansFlex",
                "GoogleSansCode",
                "Fraunces",
                "BricolageGrotesque",
                "BigShouldersDisplay",
                "Doto",
            ],
        )

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

    def test_verify_wear_aars_validates_required_classes_and_symbols(self):
        with tempfile.TemporaryDirectory() as td:
            gradle_files = Path(td)
            m3_dir = gradle_files / "androidx.wear.compose/compose-material3/1.8.0-alpha03/abc"
            fn_dir = gradle_files / "androidx.wear.compose/compose-foundation/1.8.0-alpha03/def"
            m3_dir.mkdir(parents=True)
            fn_dir.mkdir(parents=True)

            def write_aar(out_path: Path, entries: dict[str, bytes]) -> None:
                jar_buf = io.BytesIO()
                with zipfile.ZipFile(jar_buf, "w") as jar:
                    for name, data in entries.items():
                        jar.writestr(name, data)
                with zipfile.ZipFile(out_path, "w") as aar:
                    aar.writestr("classes.jar", jar_buf.getvalue())

            m3_entries = {
                "androidx/wear/compose/material3/AppScaffoldKt.class": b"AppScaffold",
                "androidx/wear/compose/material3/ScreenScaffoldKt.class": b"ScreenScaffold",
                "androidx/wear/compose/material3/EdgeButtonKt.class": b"EdgeButton",
                "androidx/wear/compose/material3/ButtonKt.class": b"FilledTonalButton",
                "androidx/wear/compose/material3/CardKt.class": b"TitleCard",
                "androidx/wear/compose/material3/SwitchButtonKt.class": b"SwitchButton",
                "androidx/wear/compose/material3/AlertDialogKt.class": b"AlertDialog",
                "androidx/wear/compose/material3/ConfirmationDialogKt.class": b"ConfirmationDialog",
                "androidx/wear/compose/material3/TimeTextKt.class": b"TimeText",
                "androidx/wear/compose/material3/ListHeaderKt.class": b"ListHeader",
                "androidx/wear/compose/material3/CircularProgressIndicatorKt.class": b"CircularProgressIndicator",
                "androidx/wear/compose/material3/CheckboxButtonKt.class": b"CheckboxButton",
            }
            write_aar(m3_dir / "compose-material3-1.8.0-alpha03.aar", m3_entries)
            write_aar(
                fn_dir / "compose-foundation-1.8.0-alpha03.aar",
                {"androidx/wear/compose/foundation/lazy/TransformingLazyColumnKt.class": b"TransformingLazyColumn"},
            )

            gen.verify_wear_aars("1.8.0-alpha03", gradle_files=gradle_files, require_cache=True)
            m3_entries["androidx/wear/compose/material3/EdgeButtonKt.class"] = b"OtherSymbol"
            write_aar(m3_dir / "compose-material3-1.8.0-alpha03.aar", m3_entries)
            with self.assertRaisesRegex(RuntimeError, "Missing symbol EdgeButton"):
                gen.verify_wear_aars("1.8.0-alpha03", gradle_files=gradle_files, require_cache=True)


if __name__ == "__main__":
    unittest.main()
