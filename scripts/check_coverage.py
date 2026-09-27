#!/usr/bin/env python3
"""Cross-checks docs/research/source-coverage.md against sources/.

Usage: check_coverage.py

Fails when a distilled or partly distilled ledger row has no source file (the evidence is missing), and
lists source files that no ledger row mentions (new upstream pages that still need a verdict).
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LEDGER = ROOT / "docs" / "research" / "source-coverage.md"
SOURCES = ROOT / "sources"
M3_PAGES = SOURCES / "m3.material.io" / "pages"
SKIPPED = object()  # rows for platforms the skill does not cover, which are never fetched


def source_for(route, section):
    """Maps a ledger route to its source file, or None when the ledger uses a form this script does not know."""
    if section.startswith("Out of scope"):
        return SKIPPED
    if route.startswith("youtube.com/watch?v="):
        return SOURCES / "youtube.com" / (route.split("=", 1)[1] + ".md")
    if route.startswith("design.google/"):
        return SOURCES / (route + ".md")
    if section.startswith("developer.android.com"):
        return SOURCES / "developer.android.com" / (route.strip("/") + ".md")
    if section.startswith("m3.material.io"):
        parts = route.strip("/").split("/")
        if parts[0] == "blog" and len(parts) > 1:
            return SOURCES / "m3.material.io" / "blog" / (parts[1] + ".md")
        exact = M3_PAGES / ("/".join(parts) + ".md")
        if exact.exists() or len(parts) < 2:
            return exact
        # A tab or subpage (/components/chips/specs) is a "## Specs" section of its page's file. Only that one
        # level counts: falling back further would let a renamed page pass as its section hub.
        parent = M3_PAGES / ("/".join(parts[:-1]) + ".md")
        if parent.exists() and parts[-1] in section_slugs(parent):
            return parent
        return exact
    return None


def section_slugs(path):
    return {re.sub(r"[^a-z0-9]+", "-", heading.lower()).strip("-")
            for heading in re.findall(r"^## (.+)$", path.read_text(), re.M)}


def rows():
    section = ""
    for line in LEDGER.read_text().splitlines():
        if line.startswith("## "):
            section = line[3:]
        match = re.match(r"- \[(.)\] (\S+?)[;:]?(\s|$)", line)
        if match:
            yield match.group(1), match.group(2), section


def main():
    missing, unknown, covered = [], [], set()
    for status, route, section in rows():
        source = source_for(route, section)
        if source is SKIPPED:
            continue
        if source is None:
            unknown.append(f"{route} ({section})")
            continue
        covered.add(source)
        if status in "x~" and not source.exists():
            missing.append(f"[{status}] {route} -> {source.relative_to(ROOT)}")
    unlisted = sorted(str(p.relative_to(ROOT)) for p in SOURCES.rglob("*.md") if p not in covered)

    for title, items in (("Distilled rows without a source file", missing),
                         ("Rows in an unknown form", unknown),
                         ("Source files with no ledger row (new pages to review)", unlisted)):
        if items:
            print(f"{title} ({len(items)}):")
            print("  " + "\n  ".join(items))
    if missing or unknown:
        sys.exit(1)
    print(f"ok: every distilled row has its source; {len(unlisted)} source files await a ledger row")


if __name__ == "__main__":
    main()
