# Previewer setup

Read this when the `android_design_preview` MCP tools (`check_design`, `get_tree`) are unavailable or report missing prerequisites. Tell the user these steps, then wait.

## Prerequisites

- **Node.js 18+** (runs `plugin/scripts/android-design-preview.js`)
- **Java 17+** (system JDK or Android Studio's bundled JBR)

When `~/.cache/android-design-preview/0.4.0/android-design-preview.jar` is missing, the first session downloads and SHA-256 verifies `android-design-preview.jar` from GitHub Releases into that directory.

## Register the plugin or MCP server

- **Antigravity:** run `agy plugin install <path-to-repo>/plugin` (or `./scripts/install_local_plugin.sh` from a repo checkout to build a local worker JAR first). Or add the server in `~/.gemini/config/mcp_config.json`:

  ```json
  {
    "mcpServers": {
      "android_design_preview": {
        "command": "node",
        "args": ["<path-to-repo>/plugin/scripts/android-design-preview.js"]
      }
    }
  }
  ```

- **Claude Code:** `claude mcp add --scope user android_design_preview -- node <path-to-repo>/plugin/scripts/android-design-preview.js`
- **Codex:** add a `[mcp_servers.android_design_preview]` table in `~/.codex/config.toml` with `command = "node"`, `args = ["<path-to-repo>/plugin/scripts/android-design-preview.js"]`, and `default_tools_approval_mode = "approve"`.

To override the cached worker JAR with a local build (`cd app && ./gradlew :app:workerJar`), set `ANDROID_DESIGN_PREVIEW_JAR` to `<path-to-repo>/app/app/build/libs/android-design-preview.jar`. Restart the session after registering.
