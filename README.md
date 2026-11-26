![android-design](assets/banner.png)

# android-design

Agent skills for designing Jetpack Compose UI with Material 3 Expressive across phone, foldable, tablet, desktop, and Wear OS. Every rule is distilled from Google's official guidance.

**`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose screens (hierarchy, color, typography, shape, motion, and adaptive layouts).

**`android-design-explore`** adds live visual exploration. It generates side-by-side Compose alternatives in a local browser board, then wires the approved direction into your app.

## Install

**Claude Code**

```bash
claude plugin marketplace add charliesbot/android-design
claude plugin install android-design@charliesbot-android-design
```

**Codex**

```bash
codex plugin marketplace add charliesbot/android-design
codex plugin add android-design@charliesbot-android-design
```

**Antigravity**

```bash
agy plugin install https://github.com/charliesbot/android-design
```

**Standalone skills (`npx skills` or Chai)**

```bash
npx skills add charliesbot/android-design
```

## Requirements

- macOS or Linux
- Java 21+ (JDK or Android Studio's bundled JBR)
- Android SDK (`ANDROID_HOME` or `ANDROID_SDK_ROOT`)

## Local development

From a local clone, install and build everything for your local harnesses:

```bash
./scripts/install_local_plugin.sh
```

Pass `--skip-build` to reuse the last build.
