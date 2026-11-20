"""Checks version sync, CLI bootstrap scripts, and manifest validity across plugin/."""

import json
import os
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PLUGIN_DIR = ROOT / "plugin"
APP_MAIN_KT = ROOT / "app/app/src/main/kotlin/bot/charlies/androiddesign/preview/app/Main.kt"
ANTIGRAVITY_ALLOWED_KEYS = {
    "name",
    "displayName",
    "version",
    "description",
    "logo",
    "suggestedPrompts",
    "disabled",
}


class PluginManifestsTest(unittest.TestCase):
    def test_versions_and_manifests_stay_in_sync(self):
        agy_plugin = json.loads((PLUGIN_DIR / "plugin.json").read_text())
        version = agy_plugin["version"]
        self.assertEqual(set(agy_plugin) - ANTIGRAVITY_ALLOWED_KEYS, set())
        self.assertTrue((PLUGIN_DIR / agy_plugin["logo"]).is_file())
        self.assertTrue(1 <= len(agy_plugin["suggestedPrompts"]) <= 3)

        # The v5 bootstrap script exists and is executable. Windows is unsupported, so no .cmd ships.
        posix_bin = PLUGIN_DIR / "bin/android-design-preview"
        self.assertTrue(posix_bin.is_file())
        self.assertTrue(os.access(posix_bin, os.X_OK), "plugin/bin/android-design-preview must be executable")
        self.assertFalse((PLUGIN_DIR / "bin/android-design-preview.cmd").exists())

        # v4 Node MCP supervisor and MCP configs are retired.
        for removed in (
            PLUGIN_DIR / "scripts",
            ROOT / "supervisor",
            PLUGIN_DIR / "mcp_config.json",
            PLUGIN_DIR / ".mcp.json",
            PLUGIN_DIR / ".codex-plugin/mcp.json",
        ):
            self.assertFalse(removed.exists(), f"retired v4 path should not exist: {removed.relative_to(ROOT)}")

        # AppInfo.VERSION in Main.kt matches plugin version.
        main_kt = APP_MAIN_KT.read_text(encoding="utf-8")
        match = re.search(r'const val VERSION = "([^"]+)"', main_kt)
        self.assertIsNotNone(match, "AppInfo.VERSION not found in Main.kt")
        self.assertEqual(match.group(1), version)

        claude_plugin = json.loads((PLUGIN_DIR / ".claude-plugin/plugin.json").read_text())
        self.assertEqual(claude_plugin["name"], agy_plugin["name"])
        self.assertEqual(claude_plugin["version"], version)

        codex_plugin = json.loads((PLUGIN_DIR / ".codex-plugin/plugin.json").read_text())
        self.assertEqual(codex_plugin["name"], agy_plugin["name"])
        self.assertEqual(codex_plugin["version"], version)
        self.assertEqual(codex_plugin["skills"], "./skills/")
        self.assertNotIn("mcpServers", codex_plugin)

        claude_mkt = json.loads((ROOT / ".claude-plugin/marketplace.json").read_text())
        entry = claude_mkt["plugins"][0]
        self.assertEqual(entry["version"], version)
        self.assertEqual(entry["source"]["source"], "git-subdir")
        self.assertEqual(entry["source"]["path"], "plugin")

        codex_mkt = json.loads((ROOT / ".agents/plugins/marketplace.json").read_text())
        self.assertEqual(codex_mkt["plugins"][0]["version"], version)
        self.assertEqual(codex_mkt["plugins"][0]["source"], "./plugin")

        self.assertTrue((PLUGIN_DIR / "skills/android-design/SKILL.md").is_file())
        self.assertTrue((PLUGIN_DIR / "skills/android-design-explore/SKILL.md").is_file())

    def test_no_em_dashes_in_plugin(self):
        for path in PLUGIN_DIR.rglob("*"):
            if path.is_file() and path.suffix in {".md", ".json", ".svg", ""}:
                self.assertNotIn(
                    "\u2014",
                    path.read_text(encoding="utf-8"),
                    f"em-dash found in {path.relative_to(ROOT)}",
                )


if __name__ == "__main__":
    unittest.main()
