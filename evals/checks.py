#!/usr/bin/env python3
"""Deterministic code checks for one eval case. Usage: checks.py <project_dir> <phone|wear|widget>

Prints JSON: {"C-id": {"pass": bool, "detail": str}}. Checks that do not apply to the surface are omitted.
"""
import json
import re
import sys
from pathlib import Path

EXPECTED_RENDERS = {
    "phone": {"light", "dark", "font200", "wallpaper"},
    "wear": {"large", "small", "font"},
    "widget": {"light-2x2", "light-4x2", "dark-2x2", "dark-4x2"},
}
PLACEHOLDERS = {
    "phone": "Replace EvalScreen with the screen from the brief",
    "wear": "Replace EvalWearScreen",
}
REPLACED = re.compile(
    r"(?<![A-Za-z])(NavigationBar|NavigationBarItem|SegmentedButton|SingleChoiceSegmentedButtonRow|"
    r"MultiChoiceSegmentedButtonRow|MediumTopAppBar|LargeTopAppBar|BottomAppBar|ModalNavigationDrawer|"
    r"PermanentNavigationDrawer|DismissibleNavigationDrawer|SmallFloatingActionButton)\s*\("
)
ALPHA = re.compile(r"\.copy\(\s*alpha\s*=")
HEX = re.compile(r"Color\(\s*0x[0-9A-Fa-f]{6,8}")
PALETTE = re.compile(r"PaletteStyle\.(Expressive|Vibrant)\b")
THEME_FILE = re.compile(r"(Theme|Color|Colour|Palette|Type)\w*\.kt$")


def sources(project: Path, surface: str):
    module = "wear" if surface == "wear" else "app"
    return sorted((project / module / "src" / "main").rglob("*.kt"))


def hits(files, pattern, skip=lambda path, line: False):
    found = []
    for path in files:
        for number, line in enumerate(path.read_text(errors="ignore").splitlines(), 1):
            if pattern.search(line) and not skip(path, line):
                found.append(f"{path.name}:{number}: {line.strip()[:120]}")
    return found


def result(ok, detail=""):
    return {"pass": ok, "detail": detail}


def run(project: Path, surface: str):
    files = sources(project, surface)
    text = "\n".join(p.read_text(errors="ignore") for p in files)
    out = {}

    renders = {p.stem for p in (project / "renders").glob("*.png")}
    missing = EXPECTED_RENDERS[surface] - renders
    out["C-build"] = result(not missing, f"missing renders: {sorted(missing)}" if missing else "")

    if surface == "widget":
        ok = re.search(r"class\s+EvalWidget\b[^{]*GlanceAppWidget", text) is not None
        out["C-contract"] = result(ok, "" if ok else "no `class EvalWidget : GlanceAppWidget`")
    else:
        name = "EvalWearScreen" if surface == "wear" else "EvalScreen"
        defined = re.search(rf"fun\s+{name}\s*\(", text) is not None
        placeholder = PLACEHOLDERS[surface] in text
        out["C-contract"] = result(defined and not placeholder, "placeholder still present" if placeholder else ("" if defined else f"{name}() not found"))

    found = hits(files, ALPHA)
    out["C-alpha"] = result(not found, "; ".join(found[:5]))

    def seed_or_theme(path, line):
        return THEME_FILE.search(path.name) or "/theme/" in str(path) or re.search(r"seed|fallback", line, re.I)

    found = hits(files, HEX, seed_or_theme)
    out["C-hex"] = result(not found, "; ".join(found[:5]))

    if surface == "phone":
        out["C-theme"] = result("MaterialExpressiveTheme(" in text, "" if "MaterialExpressiveTheme(" in text else "MaterialExpressiveTheme not used")
        fonts = list((project / "app" / "src" / "main" / "res" / "font").glob("*google_sans_flex*"))
        used = "google_sans_flex" in text
        out["C-font"] = result(bool(fonts) and used, "" if fonts and used else ("font file missing" if not fonts else "font not referenced in code"))
        found = hits(files, PALETTE)
        out["C-palette"] = result(not found, "; ".join(found[:5]))
        found = hits(files, REPLACED)
        out["C-replaced"] = result(not found, "; ".join(found[:5]))
    return out


if __name__ == "__main__":
    if len(sys.argv) != 3 or sys.argv[2] not in EXPECTED_RENDERS:
        sys.exit("usage: checks.py <project_dir> <phone|wear|widget>")
    print(json.dumps(run(Path(sys.argv[1]), sys.argv[2]), indent=2))
