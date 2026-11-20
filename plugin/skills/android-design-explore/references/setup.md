# Previewer setup

Read this when `android-design-preview` is not on `PATH` or exits `3` (missing Java or server prerequisites). Tell the user these steps, then wait. Never edit the user's harness config unasked.

## Prerequisites

- **macOS or Linux** (Windows is unsupported)
- **Java 17+** (system `java` on `PATH`, `JAVA_HOME`, or Android Studio's bundled JBR)

On first run, if `~/.cache/android-design-preview/<version>/android-design-preview.jar` is missing, the bootstrap script downloads the previewer for your OS and Java CPU from GitHub Releases and verifies its SHA-256 checksum automatically.

## Put `android-design-preview` on `PATH`

- **Claude Code:** installing the plugin puts `plugin/bin/` on `PATH` automatically.
- **Antigravity, Codex, `npx skills`, and Chai:** run `install` once from the plugin or repository checkout to link the bootstrap script into `~/.local/bin`:

  ```sh
  <path-to-plugin>/bin/android-design-preview install
  ```

  Make sure `~/.local/bin` is on your `PATH`, then retry.

## Codex sandbox settings (only if the command fails in Codex)

Codex runs shell commands inside its workspace-write sandbox, which blocks loopback HTTP and cache writes by default. If `android-design-preview` fails in Codex with a connection or permission error, tell the user to add these settings to `~/.codex/config.toml` (noting that `network_access = true` opens outbound network access to all Codex shell commands, not only this tool):

```toml
[sandbox_workspace_write]
network_access = true
writable_roots = ["~/.cache/android-design-preview"]
```

## Local development JAR override

To test a locally built JAR (`cd app && ./gradlew :app:workerJar`), set `ANDROID_DESIGN_PREVIEW_JAR` to `<path-to-repo>/app/app/build/libs/android-design-preview.jar`.
