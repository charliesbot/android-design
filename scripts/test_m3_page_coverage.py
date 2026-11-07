"""Checks that every Material 3 component page in sources/ is drawn or deliberately omitted by the previewer catalog.

The registry (app/core/model/.../Catalog.kt) names the pages each component implements (x-m3-pages) and the pages it
omits with a reason (x-m3-omissions), both written to app/schemas/m3-expressive.catalog.json. A page that is neither
fails here, so a monthly fetch that brings a new component turns into a decision: implement it or omit it with a reason.

Needs no JDK: it reads the committed schema. Run: python3 -m unittest discover scripts
"""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PAGES = ROOT / "sources/m3.material.io/pages/components"
CATALOG = ROOT / "app/schemas/m3-expressive.catalog.json"

# Component pages that choose between components instead of describing one, so no catalog entry implements them.
CHOOSERS = {
    "all-buttons": "Lists the ten button types and when to pick each; every type has its own page.",
}
REASONS = {"retired", "no-compose-api"}


def source_pages():
    return {path.stem for path in PAGES.glob("*.md")}


def problems(catalog, pages):
    """Returns what is wrong with the catalog's claims and omissions against the set of source [pages]."""
    claims = {page for comp in catalog["components"].values() for page in comp.get("x-m3-pages", [])}
    omissions = {item["page"]: item for item in catalog.get("x-m3-omissions", [])}
    found = []
    for page in sorted(pages - set(CHOOSERS) - claims - set(omissions)):
        found.append(f"page '{page}' is neither claimed by a component (m3Pages) nor omitted (m3Omissions)")
    for page in sorted(claims - pages):
        found.append(f"a component claims '{page}', which is not a page in sources/")
    for page in sorted(set(omissions) - pages):
        found.append(f"an omission names '{page}', which is not a page in sources/")
    for page in sorted(claims & set(omissions)):
        found.append(f"page '{page}' is both claimed and omitted")
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
        catalog = json.loads(CATALOG.read_text())
        self.assertEqual(problems(catalog, source_pages()), [])

    def test_a_new_page_fails(self):
        catalog = json.loads(CATALOG.read_text())
        found = problems(catalog, source_pages() | {"stepper"})
        self.assertEqual(found, ["page 'stepper' is neither claimed by a component (m3Pages) nor omitted (m3Omissions)"])

    def test_a_page_that_no_longer_exists_fails(self):
        catalog = json.loads(CATALOG.read_text())
        found = problems(catalog, source_pages() - {"carousel", "navigation-drawer"})
        self.assertIn("a component claims 'carousel', which is not a page in sources/", found)
        self.assertIn("an omission names 'navigation-drawer', which is not a page in sources/", found)

    def test_an_omission_needs_a_known_reason_and_a_why(self):
        catalog = {"components": {}, "x-m3-omissions": [{"page": "x", "reason": "boring", "why": " "}]}
        found = problems(catalog, {"x", *CHOOSERS})
        self.assertEqual(len(found), 2)


if __name__ == "__main__":
    unittest.main()
