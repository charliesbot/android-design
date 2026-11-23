# android-design

Agent skills for designing Android UI with Material 3 Expressive and Wear OS Jetpack Compose, for phone, foldable, tablet, desktop, and Wear OS apps. Every rule is distilled from Google's official guidance.

- **`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose: hierarchy, color, type, shape, motion, and component choice.
- **`android-design-explore`** lets an agent explore directions as live `.explore.kt` Compose boards checked through the `android-design-preview` CLI and long-running Kotlin HTTP/SSE server, view alternatives side by side in a localhost browser board, and wire approved screens into an app.

## Install the plugin

Runs on macOS and Linux (Windows is unsupported) and requires **Java 17+** (JDK or Android Studio's bundled JBR), an **Android SDK** (`ANDROID_HOME` or `ANDROID_SDK_ROOT`), **`git`**, and the **`android` CLI** with an emulator or connected device for live previews (headless `render` uses Robolectric locally).

**Claude Code:**

```bash
claude plugin marketplace add charliesbot/android-design
```

```bash
claude plugin install android-design@charliesbot-android-design
```

**Antigravity:**

```bash
agy plugin install https://github.com/charliesbot/android-design/tree/main/plugin
```

**Standalone skills (`npx skills` or Chai):**

```bash
npx skills add charliesbot/android-design
```

The previewer CLI's bootstrap script ships inside the explore skill (`plugin/skills/android-design-explore/scripts/android-design-preview`), so every install route brings it and agents run it from there; Claude Code also puts it on `PATH` through the plugin's `bin/` wrapper. To use the bare `android-design-preview` command in your own shell, run that script with `install` once (it links into `~/.local/bin`).

On first use the script sparse-checkouts `app/` from the matching `v<version>` tag and runs `./gradlew :host:exportBootstrap` once to cache the previewer runtime, CLI launcher, Android Host APK, and AndroidX compile/Robolectric bundles under `~/.cache/android-design-preview/<version>/`. Merging a version bump to `main` creates that `v<version>` tag in CI.

**From a local clone:** `./scripts/install_local_plugin.sh` installs this checkout's plugin into Claude Code, Codex, and Antigravity (whichever are on `PATH`), links the CLI into `~/.local/bin`, runs `:host:exportBootstrap`, and links `app/` into `~/.cache/android-design-preview/<version>/src/app` so every harness runs the local build. Pass `--skip-build` to reuse the last build, and rerun it after changing `plugin/` or `app/`. To point a single run at a local checkout instead, set `ANDROID_DESIGN_PREVIEW_SRC` to the repository root path.
