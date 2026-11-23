"""Checks pinned Material 3 inventory against app tokens, porting guidance, and metadata parsing.

Needs no JDK: reads the committed scripts/material3-inventory.txt. Run: python3 -m unittest discover scripts
"""
import io
import re
import tempfile
import unittest
import zipfile
from pathlib import Path

import material3_inventory as inventory

ROOT = Path(__file__).resolve().parent.parent
TOKENS_KT = ROOT / "app/core/model/src/main/kotlin/bot/charlies/androiddesign/preview/model/Tokens.kt"
PORTING_DOC = ROOT / "plugin/skills/android-design-explore/references/porting.md"


def load_inventory():
    """Returns every name in the committed inventory."""
    return {
        line
        for line in inventory.INVENTORY.read_text(encoding="utf-8").splitlines()
        if line and not line.startswith("#")
    }


def _kotlin_enum_wires(enum_name):
    tokens_text = TOKENS_KT.read_text(encoding="utf-8")
    block = re.search(rf"enum class {enum_name}[^{{]*\{{(.*?);", tokens_text, re.S)
    if not block:
        raise AssertionError(f"Could not find enum class {enum_name} in {TOKENS_KT}")
    entries = re.findall(r"^\s*([A-Z]\w+)", block.group(1), re.M)
    return [name[0].lower() + name[1:] for name in entries]


class InventoryTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.known = load_inventory()

    def test_inventory_has_a_versioned_header(self):
        first = inventory.INVENTORY.read_text(encoding="utf-8").splitlines()[0]
        self.assertRegex(first, r"^# androidx\.compose\.material3:material3-android:\S+$")
        pinned = inventory.material3_version(inventory.LIBS_TOML.read_text(encoding="utf-8"))
        self.assertTrue(
            first.endswith(":" + pinned),
            "Run scripts/material3_inventory.py after changing the androidxMaterial3 version",
        )

    def test_find_aar_and_extract_classes_from_classes_jar(self):
        with tempfile.TemporaryDirectory() as td:
            gradle_files = Path(td)
            aar_dir = gradle_files / "androidx.compose.material3/material3-android/1.5.0/abc"
            aar_dir.mkdir(parents=True)
            classes_buf = io.BytesIO()
            with zipfile.ZipFile(classes_buf, "w") as jar:
                jar.writestr("androidx/compose/material3/ButtonKt.class", b"cafe")
                jar.writestr("androidx/compose/material3/internal/Helper.class", b"skip")
            with zipfile.ZipFile(aar_dir / "material3-android-1.5.0.aar", "w") as aar:
                aar.writestr("classes.jar", classes_buf.getvalue())

            ver, found = inventory.find_aar(
                "androidx.compose.material3", "material3-android", "1.5.0", gradle_files=gradle_files
            )
            self.assertEqual(ver, "1.5.0")
            self.assertIsNotNone(found)
            out_dir = Path(td) / "out"
            wanted = inventory.extract_classes_from_aar(found, out_dir)
            self.assertEqual(wanted, ["androidx.compose.material3.ButtonKt"])
            self.assertTrue((out_dir / "androidx/compose/material3/ButtonKt.class").is_file())

    def test_every_model_token_exists_in_compose_inventory(self):
        names = [f"ColorScheme.{role}" for role in _kotlin_enum_wires("ColorRole")]
        names += [f"Typography.{role}" for role in _kotlin_enum_wires("TypeRole")]
        names += [
            f"MaterialShapes.{shape[0].upper()}{shape[1:]}"
            for shape in _kotlin_enum_wires("MaterialShape")
        ]
        names += [
            f"Shapes.{corner}"
            for corner in _kotlin_enum_wires("ShapeToken")
            if corner not in ("none", "full")
        ]
        self.assertEqual([name for name in names if name not in self.known], [])

    def test_porting_doc_uses_symbol_drawables_and_has_no_em_dash(self):
        text = PORTING_DOC.read_text(encoding="utf-8")
        self.assertIn("painterResource", text)
        self.assertNotIn(chr(0x2014), text)


class InventoryParsingTest(unittest.TestCase):
    def test_mangling_suffix_is_stripped(self):
        self.assertEqual(inventory.strip_mangling("shapesFor-8Feqmps"), "shapesFor")
        self.assertEqual(inventory.strip_mangling("Button"), "Button")

    def test_getter_case_follows_the_kotlin_metadata(self):
        self.assertEqual(
            inventory.property_name("getExtraLargeContainerHeight", {"ExtraLargeContainerHeight"}),
            "ExtraLargeContainerHeight",
        )
        self.assertEqual(
            inventory.property_name("getColorScheme", {"colorScheme"}),
            "colorScheme",
        )

    def test_objects_contribute_members_and_top_level_functions_contribute_their_names(self):
        javap = inventory.parse_javap(
            "Compiled from \"ButtonDefaults.kt\"\n"
            "public final class androidx.compose.material3.ButtonDefaults {\n"
            "  public static final androidx.compose.material3.ButtonDefaults INSTANCE;\n"
            "  public final float getExtraLargeContainerHeight-D9Ej5fM();\n"
            "  public final androidx.compose.material3.ButtonShapes shapesFor-8Feqmps(float, androidx.compose.runtime.Composer, int);\n"
            "}\n"
            "public final class androidx.compose.material3.ButtonKt {\n"
            "  public static final void Button(kotlin.jvm.functions.Function0<kotlin.Unit>);\n"
            "}\n"
            "final class androidx.compose.material3.Hidden {\n"
            "  public final void leaked();\n"
            "}\n"
        )
        entries = set()
        for name, members in javap:
            entries |= inventory.entries_for_class(
                name.rsplit(".", 1)[-1], members, {"ExtraLargeContainerHeight"}
            )
        self.assertEqual(
            entries,
            {
                "ButtonDefaults",
                "ButtonDefaults.ExtraLargeContainerHeight",
                "ButtonDefaults.shapesFor",
                "Button",
            },
        )


if __name__ == "__main__":
    unittest.main()
