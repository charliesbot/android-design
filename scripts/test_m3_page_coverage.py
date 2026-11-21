"""Checks that every Material 3 component page in sources/ is covered or deliberately omitted.

Every non-chooser component page in sources/m3.material.io/pages/components/ must either map to real
Material 3 Compose APIs supported by the v4 Kotlin sandbox (M3_PAGE_CLAIMS, verified against both
scripts/material3-inventory.txt and CallNameMapper.kt's PHONE_MAP) or record a deliberate omission
in M3_OMISSIONS.

Needs no JDK: reads committed sources, scripts/material3-inventory.txt, and CallNameMapper.kt.
Run: python3 -m unittest discover scripts
"""
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PAGES = ROOT / "sources/m3.material.io/pages/components"
INVENTORY = ROOT / "scripts/material3-inventory.txt"
CALL_MAPPER = (
    ROOT
    / "app/features/preview/desktop/src/main/kotlin/bot/charlies/androiddesign/preview/feature/preview/sandbox/CallNameMapper.kt"
)

# Component pages that choose between components instead of describing one.
CHOOSERS = {
    "all-buttons": "Lists the ten button types and when to pick each; every type has its own page.",
}
REASONS = {"retired", "no-compose-api"}

# M3 component pages implemented directly in the Compose sandbox -> Material 3 composable names.
M3_PAGE_CLAIMS = {
    "app-bars": ["TopAppBar", "CenterAlignedTopAppBar", "MediumFlexibleTopAppBar", "LargeFlexibleTopAppBar"],
    "badges": ["Badge", "BadgedBox"],
    "bottom-sheets": ["ModalBottomSheet", "BottomSheet"],
    "button-groups": ["ButtonGroup"],
    "buttons": [
        "Button",
        "FilledTonalButton",
        "ElevatedButton",
        "OutlinedButton",
        "TextButton",
        "ToggleButton",
        "FilledTonalToggleButton",
        "ElevatedToggleButton",
        "OutlinedToggleButton",
    ],
    "cards": ["Card", "ElevatedCard", "OutlinedCard"],
    "carousel": [
        "HorizontalMultiBrowseCarousel",
        "HorizontalCenteredHeroCarousel",
        "HorizontalUncontainedCarousel",
    ],
    "checkbox": ["Checkbox", "TriStateCheckbox"],
    "chips": [
        "AssistChip",
        "ElevatedAssistChip",
        "FilterChip",
        "ElevatedFilterChip",
        "InputChip",
        "SuggestionChip",
        "ElevatedSuggestionChip",
    ],
    "date-pickers": ["DatePicker", "DatePickerDialog", "DateRangePicker"],
    "dialogs": ["AlertDialog", "BasicAlertDialog"],
    "divider": ["HorizontalDivider", "VerticalDivider"],
    "extended-fab": [
        "ExtendedFloatingActionButton",
        "SmallExtendedFloatingActionButton",
        "MediumExtendedFloatingActionButton",
        "LargeExtendedFloatingActionButton",
    ],
    "fab-menu": ["FloatingActionButtonMenu", "FloatingActionButtonMenuItem", "ToggleFloatingActionButton"],
    "floating-action-button": [
        "FloatingActionButton",
        "SmallFloatingActionButton",
        "MediumFloatingActionButton",
        "LargeFloatingActionButton",
    ],
    "icon-buttons": [
        "IconButton",
        "FilledIconButton",
        "FilledTonalIconButton",
        "OutlinedIconButton",
        "IconToggleButton",
        "FilledIconToggleButton",
        "FilledTonalIconToggleButton",
        "OutlinedIconToggleButton",
    ],
    "lists": ["ListItem", "SegmentedListItem"],
    "loading-indicator": ["LoadingIndicator", "ContainedLoadingIndicator"],
    "menus": [
        "DropdownMenu",
        "DropdownMenuPopup",
        "DropdownMenuGroup",
        "DropdownMenuItem",
        "SelectableDropdownMenuItem",
        "CheckableDropdownMenuItem",
        "ExposedDropdownMenu",
    ],
    "navigation-bar": ["ShortNavigationBar", "ShortNavigationBarItem", "NavigationBar", "NavigationBarItem"],
    "navigation-rail": [
        "WideNavigationRail",
        "WideNavigationRailItem",
        "ModalWideNavigationRail",
        "NavigationRail",
        "NavigationRailItem",
    ],
    "progress-indicators": [
        "LinearProgressIndicator",
        "LinearWavyProgressIndicator",
        "CircularProgressIndicator",
        "CircularWavyProgressIndicator",
    ],
    "radio-button": ["RadioButton"],
    "scroll-fields": ["ScrollField"],
    "search": [
        "SearchBar",
        "DockedSearchBar",
        "ExpandedFullScreenContainedSearchBar",
        "ExpandedDockedSearchBarWithGap",
    ],
    "side-sheets": ["Surface"],
    "sliders": ["Slider", "VerticalSlider", "RangeSlider"],
    "snackbar": ["SnackbarHost", "Snackbar"],
    # TODO(https://github.com/charliesbot/android-design/issues/153): Update if Compose Multiplatform 1.13.0 stable ships SplitButton replacing SplitButtonLayout.
    "split-button": ["SplitButtonLayout"],
    "switch": ["Switch"],
    "tabs": [
        "PrimaryTabRow",
        "SecondaryTabRow",
        "PrimaryScrollableTabRow",
        "SecondaryScrollableTabRow",
        "Tab",
        "LeadingIconTab",
    ],
    "text-fields": ["TextField", "OutlinedTextField", "SecureTextField", "OutlinedSecureTextField"],
    "time-pickers": ["TimePicker", "TimePickerDialog", "VibrantTimePickerDialog", "TimeInput", "TimeScroll"],
    "toolbars": ["HorizontalFloatingToolbar", "VerticalFloatingToolbar", "FlexibleBottomAppBar"],
    "tooltips": ["TooltipBox", "PlainTooltip", "RichTooltip"],
}

M3_OMISSIONS = {
    "navigation-drawer": {
        "reason": "retired",
        "why": "Material 3 Expressive no longer recommends it; use an expanded navigation rail.",
    },
    "segmented-buttons": {
        "reason": "retired",
        "why": "Material 3 Expressive no longer recommends it; use a connected button group.",
    },
}


def source_pages():
    return {path.stem for path in PAGES.glob("*.md")}


def load_inventory():
    return {
        line.strip()
        for line in INVENTORY.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.startswith("#")
    }


def load_phone_mapper_calls():
    text = CALL_MAPPER.read_text(encoding="utf-8")
    phone_block = text.split("private val PHONE_MAP")[1].split("private val WEAR_MAP")[0]
    return set(re.findall(r'"(\w+)"\s*to\s*', phone_block))


def problems(claims, omissions, pages):
    """Returns what is wrong with page coverage claims and omissions against source [pages]."""
    claimed = set(claims)
    omitted = set(omissions)
    found = []
    for page in sorted(pages - set(CHOOSERS) - claimed - omitted):
        found.append(f"page '{page}' is neither claimed in M3_PAGE_CLAIMS nor omitted in M3_OMISSIONS")
    for page in sorted(claimed - pages):
        found.append(f"M3_PAGE_CLAIMS names '{page}', which is not a page in sources/")
    for page in sorted(omitted - pages):
        found.append(f"M3_OMISSIONS names '{page}', which is not a page in sources/")
    for page in sorted(claimed & omitted):
        found.append(f"page '{page}' is both claimed and omitted")
    for page, composables in sorted(claims.items()):
        if not composables:
            found.append(f"claim '{page}' lists no composables")
    for page, item in sorted(omissions.items()):
        if item["reason"] not in REASONS:
            found.append(f"omission '{page}' has reason '{item['reason']}', expected one of {sorted(REASONS)}")
        if not item.get("why", "").strip():
            found.append(f"omission '{page}' has no why")
    for page in sorted(set(CHOOSERS) - pages):
        found.append(f"chooser '{page}' is not a page in sources/")
    return found


class M3PageCoverageTest(unittest.TestCase):
    def test_every_page_is_claimed_or_omitted(self):
        self.assertEqual(problems(M3_PAGE_CLAIMS, M3_OMISSIONS, source_pages()), [])

    def test_a_new_page_fails(self):
        found = problems(M3_PAGE_CLAIMS, M3_OMISSIONS, source_pages() | {"stepper"})
        self.assertEqual(
            found,
            ["page 'stepper' is neither claimed in M3_PAGE_CLAIMS nor omitted in M3_OMISSIONS"],
        )

    def test_a_page_that_no_longer_exists_fails(self):
        found = problems(M3_PAGE_CLAIMS, M3_OMISSIONS, source_pages() - {"carousel", "navigation-drawer"})
        self.assertIn("M3_PAGE_CLAIMS names 'carousel', which is not a page in sources/", found)
        self.assertIn("M3_OMISSIONS names 'navigation-drawer', which is not a page in sources/", found)

    def test_an_omission_needs_a_known_reason_and_a_why(self):
        omissions = {"x": {"reason": "boring", "why": " "}}
        found = problems({}, omissions, {"x", *CHOOSERS})
        self.assertEqual(len(found), 2)

    def test_claimed_composables_exist_in_inventory_and_call_name_mapper(self):
        inventory = load_inventory()
        mapped = load_phone_mapper_calls()
        missing_inv = []
        missing_mapper = []
        for page, composables in sorted(M3_PAGE_CLAIMS.items()):
            for sym in composables:
                if sym not in inventory:
                    missing_inv.append(f"{page}: {sym}")
                if sym not in mapped:
                    missing_mapper.append(f"{page}: {sym}")
        self.assertEqual(missing_inv, [])
        self.assertEqual(missing_mapper, [])


if __name__ == "__main__":
    unittest.main()
