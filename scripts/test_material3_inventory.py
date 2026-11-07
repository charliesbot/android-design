"""Checks references/compose-mapping.md against the generated Material 3 inventory and the previewer app's catalog.

Needs no JDK: it reads the committed scripts/material3-inventory.txt. Run: python3 -m unittest discover scripts
"""
import json
import re
import unittest
from pathlib import Path

import material3_inventory as inventory

ROOT = Path(__file__).resolve().parent.parent
MAPPING = ROOT / "skills/android-design-explore/references/compose-mapping.md"
CATALOG = ROOT / "app/schemas/m3-expressive.catalog.json"

# Names the mapping uses that are not in the material3 artifact. A root listed here also allows its members
# (Modifier.size, Alignment.BottomCenter, PaletteStyle.TonalSpot).
ALLOWED_ROOTS = {
    # androidx.compose.foundation and androidx.compose.ui, which the inventory does not cover.
    "Modifier", "Arrangement", "Alignment", "WindowInsets", "BoxWithConstraints", "Box", "Row", "Column", "Spacer", "BorderStroke",
    "RectangleShape", "CircleShape", "ZeroCornerSize", "Color", "TextStyle", "FontFamily", "Font", "FontVariation", "FontWeight",
    "Dialog", "DialogProperties", "RoundedCornerShape", "LaunchedEffect", "Image", "ContentScale", "TextOverflow", "TextAlign", "Grid", "FlexBox", "FlowRow", "LazyVerticalGrid",
    "GridCells", "ToggleableState", "Role", "Popup", "LocalDensity",
    # Navigation 3 and its Material 3 adaptive scenes.
    "NavDisplay", "ListDetailSceneStrategy", "SupportingPaneSceneStrategy",
    # MaterialKolor, the Kotlin port of Google's color utilities.
    "PaletteStyle", "ColorSpec",
    # The app's generated resource class (R.font.google_sans_flex).
    "R",
}

# Theme containers whose members live in another inventory class.
THEME_MEMBERS = {"colorScheme": "ColorScheme", "typography": "Typography", "shapes": "Shapes"}
IDENTIFIER = re.compile(r"(?<![\w.])[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*")


def load_inventory():
    """Returns every name in the committed inventory."""
    return {line for line in inventory.INVENTORY.read_text().splitlines() if line and not line.startswith("#")}


def mapping_rows():
    """Yields (section heading, cells) for every table body row of the mapping."""
    heading = ""
    lines = MAPPING.read_text().splitlines()
    for index, line in enumerate(lines):
        if line.startswith("#"):
            heading = line.lstrip("# ").strip()
        elif line.startswith("|") and not re.match(r"^\|[\s\-|]+\|$", line):
            if index + 1 < len(lines) and re.match(r"^\|[\s\-|]+\|$", lines[index + 1]):
                continue  # header row
            yield heading, [cell.strip() for cell in line.strip().strip("|").split("|")]


def code_spans(cell):
    return re.findall(r"`([^`]+)`", cell)


def unresolved_names(cell, known):
    """Compose identifiers in a cell that are neither in the inventory nor allowed."""
    missing = []
    for span in code_spans(cell):
        # String literals (font axis tags such as "SOFT") are data, not Compose names.
        for token in IDENTIFIER.findall(re.sub(r'"[^"]*"', '""', span)):
            if not token[0].isupper() or token.split(".")[0] in ALLOWED_ROOTS:
                continue  # named arguments, locals, and external roots
            if not resolves(token, known):
                missing.append(token)
    return missing


def resolves(token, known):
    parts = token.split(".")
    if token in known:
        return True
    if parts[0] == "MaterialTheme" and len(parts) >= 3 and parts[1] in THEME_MEMBERS:
        return "MaterialTheme." + parts[1] in known and f"{THEME_MEMBERS[parts[1]]}.{parts[2]}" in known
    return len(parts) >= 3 and ".".join(parts[:2]) in known


class MappingTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.known = load_inventory()
        cls.rows = list(mapping_rows())
        cls.text = MAPPING.read_text()

    def first_cells(self, *sections):
        return [cells[0] for heading, cells in self.rows if heading in sections]

    def test_inventory_has_a_versioned_header(self):
        first = inventory.INVENTORY.read_text().splitlines()[0]
        self.assertRegex(first, r"^# androidx\.compose\.material3:material3-android:\S+$")
        starter = inventory.material3_version(inventory.LIBS_TOML.read_text())
        self.assertTrue(first.endswith(":" + starter), "Run scripts/material3_inventory.py after changing the material3 version")

    def test_every_compose_name_exists_in_the_inventory(self):
        problems = []
        for heading, cells in self.rows:
            for cell in cells[1:]:
                for name in unresolved_names(cell, self.known):
                    problems.append(f"{heading}: {name}")
        self.assertEqual(problems, [])

    def test_component_rows_have_a_status(self):
        statuses = {cells[0]: cells[2] for heading, cells in self.rows if heading == "Components"}
        self.assertTrue(statuses)
        self.assertEqual({row: status for row, status in statuses.items() if status not in ("verified", "unverified")}, {})
        self.assertIn("verified", statuses.values())

    def test_icons_port_as_symbol_drawables_not_the_legacy_icon_set(self):
        self.assertNotRegex(self.text, r"Icons\.(Auto)?(Mirrored\.)?Rounded\.[A-Z]")
        section = self.text.split("\n## Icons\n")[1].split("\n## ")[0]
        self.assertIn("painterResource(R.drawable.name)", section)
        self.assertIn('android:autoMirrored="true"', section)
        self.assertIn('android:tint="?attr/colorControlNormal"', section)

    def test_every_catalog_component_has_a_row(self):
        catalog = json.loads(CATALOG.read_text())
        mapped = {code_spans(cells[0])[0].split()[0] for heading, cells in self.rows
                  if heading == "Components" and code_spans(cells[0])}
        self.assertEqual(sorted(set(catalog["components"]) - mapped), [])

    def test_every_catalog_token_exists_in_compose(self):
        tokens = json.loads(CATALOG.read_text())["$defs"]
        names = [f"ColorScheme.{role}" for role in tokens["ColorRole"]["enum"]]
        names += [f"Typography.{role}" for role in tokens["TypeRole"]["enum"]]
        names += [f"MaterialShapes.{shape[0].upper()}{shape[1:]}" for shape in tokens["MaterialShapeName"]["enum"]]
        names += [f"Shapes.{corner}" for corner in tokens["ShapeToken"]["enum"] if corner not in ("none", "full")]
        self.assertEqual([name for name in names if name not in self.known], [])

    def test_prose_has_no_em_dash(self):
        self.assertNotIn(chr(0x2014), self.text)


class InventoryParsingTest(unittest.TestCase):
    def test_mangling_suffix_is_stripped(self):
        self.assertEqual(inventory.strip_mangling("shapesFor-8Feqmps"), "shapesFor")
        self.assertEqual(inventory.strip_mangling("Button"), "Button")

    def test_getter_case_follows_the_kotlin_metadata(self):
        self.assertEqual(inventory.property_name("getExtraLargeContainerHeight", {"ExtraLargeContainerHeight"}), "ExtraLargeContainerHeight")
        self.assertEqual(inventory.property_name("getColorScheme", {"colorScheme"}), "colorScheme")

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
            entries |= inventory.entries_for_class(name.rsplit(".", 1)[-1], members, {"ExtraLargeContainerHeight"})
        self.assertEqual(entries, {"ButtonDefaults", "ButtonDefaults.ExtraLargeContainerHeight", "ButtonDefaults.shapesFor", "Button"})


if __name__ == "__main__":
    unittest.main()
