"""Checks version sync and manifest validity across plugin/ and supervisor/."""

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
        agy_plugin = json.loads((PLUGIN_DIR / "plugin.json").read_text())
        version = agy_plugin["version"]
        self.assertEqual(set(agy_plugin) - ANTIGRAVITY_ALLOWED_KEYS, set())
        self.assertTrue((PLUGIN_DIR / agy_plugin["logo"]).is_file())
        self.assertTrue(1 <= len(agy_plugin["suggestedPrompts"]) <= 3)

        self.assertTrue((PLUGIN_DIR / "scripts/android-design-preview.js").is_file())
        self.assertEqual(json.loads((PLUGIN_DIR / "scripts/package.json").read_text()), {"type": "module"})

        sup_pkg = json.loads(SUPERVISOR_PKG.read_text())
        self.assertIs(sup_pkg.get("private"), True)
        for npm_key in ("version", "bin", "files"):
            self.assertNotIn(npm_key, sup_pkg)

        expected_args = {
            "mcp_config.json": ["${PLUGIN_ROOT}/scripts/android-design-preview.js"],
            ".mcp.json": ["${CLAUDE_PLUGIN_ROOT}/scripts/android-design-preview.js"],
        }
        for rel, args in expected_args.items():
            mcp = json.loads((PLUGIN_DIR / rel).read_text())
            server = mcp["mcpServers"]["android_design_preview"]
            self.assertEqual(server["command"], "node")
            self.assertEqual(server["args"], args)

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
