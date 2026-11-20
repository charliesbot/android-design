# android-design

Agent skills for designing Android UI with Material 3 Expressive and Wear OS Jetpack Compose, for phone, foldable, tablet, desktop, and Wear OS apps. Every rule is distilled from Google's official guidance.

- **`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose: hierarchy, color, type, shape, motion, and component choice.
- **`android-design-explore`** lets an agent explore directions as live `.explore.kt` Compose boards checked through the `android-design-preview` CLI and long-running Kotlin HTTP/SSE server, view alternatives side by side in a localhost browser board, and wire approved screens into an app.

## Install the plugin

Runs on macOS and Linux (Windows is unsupported) and requires **Java 17+** (JDK or Android Studio's bundled JBR). No Node.js or Android SDK is required.

- **Claude Code:** installing the plugin puts `plugin/bin/` on `PATH` automatically.
- **From a local clone:** `./scripts/install_local_plugin.sh` installs this checkout's plugin into Claude Code, Codex, and Antigravity (whichever are on `PATH`), links the CLI into `~/.local/bin`, and builds `:app:workerJar` into `~/.cache/android-design-preview/<version>/` so every harness runs the local build. Pass `--skip-build` to reuse the last build, and rerun it after changing `plugin/` or `app/`.
- **Standalone skills (`npx skills` or Chai):**

```bash
npx skills add charliesbot/android-design
```

`plugin/skills/` holds `android-design` and `android-design-explore` directly for `npx skills` or copying into `~/.claude/skills/`. To put `android-design-preview` on `PATH`, run `./plugin/bin/android-design-preview install` once (symlinks into `~/.local/bin/android-design-preview`).

If `~/.cache/android-design-preview/0.5.0/android-design-preview.jar` is absent, the first CLI invocation downloads and SHA-256 verifies the previewer for your OS and Java CPU from the matching GitHub Release (about 50 MB, unpacked to an uncompressed JAR). Merging a version bump to `main` publishes that release: CI builds the JAR and creates the `v<version>` tag and release. To use a local build (`cd app && ./gradlew :app:workerJar`), run `./scripts/install_local_plugin.sh` or set `ANDROID_DESIGN_PREVIEW_JAR` to `app/app/build/libs/android-design-preview.jar`.
