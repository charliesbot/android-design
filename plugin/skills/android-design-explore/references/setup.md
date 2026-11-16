# Previewer setup

Read this when the `android_design_preview` MCP tools (`check_design`, `get_tree`) are unavailable or report missing prerequisites. Tell the user these steps, then wait.

## Prerequisites

- **Node.js 18+** (runs `npx -y @charliesbot/android-design-preview@0.4.0`)
- **Java 17+** (system JDK or Android Studio's bundled JBR)

The first `check_design` call downloads and SHA-256 verifies `android-design-preview.jar` into `~/.cache/android-design-preview/0.4.0/`.

## Register the plugin or MCP server

- **Antigravity:** point an installed or workspace plugin directory at `plugin/` from the `android-design` repository (includes `plugin.json`, `mcp_config.json`, and `skills/`). Or add the server in `~/.gemini/antigravity/mcp_config.json`:

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

- **Claude Code:** `claude mcp add --scope user android_design_preview -- npx -y @charliesbot/android-design-preview@0.4.0`
- **Codex:** add a `[mcp_servers.android_design_preview]` table in `~/.codex/config.toml` with `command = "npx"`, `args = ["-y", "@charliesbot/android-design-preview@0.4.0"]`, and `default_tools_approval_mode = "approve"`.

To test against a local worker JAR (`cd app && ./gradlew :app:workerJar`), set `ANDROID_DESIGN_PREVIEW_JAR` to `<repo>/app/app/build/libs/android-design-preview.jar`. Restart the session after registering.
