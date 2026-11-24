# Previewer setup

Read this when `android-design-preview` cannot be found (neither on `PATH` nor bundled in the skill's `scripts/` folder) or exits `3` (missing Java or server prerequisites). Tell the user these steps, then wait. Never edit the user's harness config unasked.

## Prerequisites

- **macOS or Linux** (Windows is unsupported)
- **Java 17+** (system `java` on `PATH`, `JAVA_HOME`, or Android Studio's bundled JBR)
- **Android SDK** (`ANDROID_HOME` or `ANDROID_SDK_ROOT`) and **`git`**
- **`android` CLI and an emulator or connected device** for interactive live preview (`start`, `check`, `open`; Wear boards run offscreen `check` on a phone AVD with a round screen layout, and only live interactive Wear viewing needs a Wear OS AVD or watch; headless `render` uses Robolectric locally without booting a device)

On first run, if the cached runtime under `~/.cache/android-design-preview/<version>/` is missing, the bootstrap script sparse-checkouts `app/` from GitHub at tag `v<version>` and runs `./gradlew :host:exportBootstrap` once to build the previewer server/CLI, Android Host APK, and AndroidX compile and Robolectric bundles locally.

## Where `android-design-preview` comes from

The bootstrap script ships inside this skill at `scripts/android-design-preview`, so every install route brings it and agents run it from there. Claude Code also puts it on `PATH` as a bare command through the plugin's `bin/` folder.

If the skill folder is missing `scripts/android-design-preview`, the skill was copied without it: reinstall it from https://github.com/charliesbot/android-design. To type the bare command in your own shell, run `install` once to link it into `~/.local/bin` (which must be on your `PATH`):

```sh
<skill-dir>/scripts/android-design-preview install
```

## Codex sandbox settings (only if the command fails in Codex)

Codex runs shell commands inside its workspace-write sandbox, which blocks loopback HTTP, cache writes, and Gradle writes by default. If `android-design-preview` fails in Codex with a connection, download, or permission error, tell the user to add these settings to `~/.codex/config.toml` (noting that `network_access = true` opens outbound network access to GitHub and Maven hosts as well as all other Codex shell commands):

```toml
[sandbox_workspace_write]
network_access = true
writable_roots = ["~/.cache/android-design-preview", "~/.gradle"]
```

## Local development override

To run against a local checkout (`./gradlew :host:exportBootstrap`), set `ANDROID_DESIGN_PREVIEW_SRC` to the repository root path (`<path-to-repo>`), or run `./scripts/install_local_plugin.sh` from your clone to run `:host:exportBootstrap` and link `app/` into `~/.cache/android-design-preview/<version>/src/app`.
