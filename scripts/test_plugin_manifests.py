"""Checks version sync and manifest validity across supervisor/ and plugin/."""

import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SUPERVISOR_PKG = ROOT / "supervisor/package.json"
PLUGIN_DIR = ROOT / "plugin"
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
        pkg = json.loads(SUPERVISOR_PKG.read_text())
        version = pkg["version"]
        expected_npm_arg = f"{pkg['name']}@{version}"

        agy_plugin = json.loads((PLUGIN_DIR / "plugin.json").read_text())
        self.assertEqual(set(agy_plugin) - ANTIGRAVITY_ALLOWED_KEYS, set())
        self.assertEqual(agy_plugin["version"], version)
        self.assertTrue((PLUGIN_DIR / agy_plugin["logo"]).is_file())
        self.assertTrue(1 <= len(agy_plugin["suggestedPrompts"]) <= 3)

        for rel in ("mcp_config.json", ".mcp.json"):
            mcp = json.loads((PLUGIN_DIR / rel).read_text())
            server = mcp["mcpServers"]["android_design_preview"]
            self.assertEqual(server["command"], "npx")
            self.assertEqual(server["args"], ["-y", expected_npm_arg])

        claude_plugin = json.loads((PLUGIN_DIR / ".claude-plugin/plugin.json").read_text())
        self.assertEqual(claude_plugin["name"], agy_plugin["name"])
        self.assertEqual(claude_plugin["version"], version)

        codex_plugin = json.loads((PLUGIN_DIR / ".codex-plugin/plugin.json").read_text())
        self.assertEqual(codex_plugin["name"], agy_plugin["name"])
        self.assertEqual(codex_plugin["version"], version)
        self.assertEqual(codex_plugin["skills"], "./skills/")
        self.assertEqual(codex_plugin["mcpServers"], "./.mcp.json")

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

    def test_no_em_dashes_in_plugin_or_supervisor(self):
        for folder in (PLUGIN_DIR, ROOT / "supervisor"):
            for path in folder.rglob("*"):
                if path.is_file() and path.suffix in {".md", ".json", ".js", ".mjs", ".svg"}:
                    self.assertNotIn(
                        "\u2014",
                        path.read_text(encoding="utf-8"),
                        f"em-dash found in {path.relative_to(ROOT)}",
                    )


if __name__ == "__main__":
    unittest.main()
