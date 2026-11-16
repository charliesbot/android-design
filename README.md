# android-design

Agent skills for designing Android UI with Material 3 Expressive and Wear OS Jetpack Compose, for phone, foldable, tablet, desktop, and Wear OS apps. Every rule is distilled from Google's official guidance.

- **`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose: hierarchy, color, type, shape, motion, and component choice.
- **`android-design-explore`** lets an agent explore directions as live `.explore.kt` Compose boards checked over MCP, view alternatives side by side in a localhost browser board, and wire approved screens into an app.

## Install the plugin

Requires **Node.js 18+** and **Java 17+** (JDK or Android Studio's bundled JBR).

- **Antigravity:** point a workspace or user plugin directory to `plugin/` (contains `plugin.json`, `mcp_config.json`, and `skills/`).
- **Standalone skills only (`npx skills` or Chai):**

```bash
npx skills add charliesbot/android-design
```

`plugin/skills/` holds `android-design` and `android-design-explore` directly for `npx skills` or copying into `~/.claude/skills/`.

## Manual MCP server registration

`plugin/mcp_config.json` and `plugin/.mcp.json` start the pure-JS Node MCP supervisor automatically:

```json
{
  "mcpServers": {
    "android_design_preview": {
      "command": "npx",
      "args": ["-y", "@charliesbot/android-design-preview@0.4.0"]
    }
  }
}
```

The first `check_design` call downloads and SHA-256 verifies `android-design-preview.jar` into `~/.cache/android-design-preview/0.4.0/`. To run against a local build (`cd app && ./gradlew :app:workerJar`), set `ANDROID_DESIGN_PREVIEW_JAR` to `app/app/build/libs/android-design-preview.jar`.
