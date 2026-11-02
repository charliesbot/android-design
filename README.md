# android-design

An agent skill for designing Android UI with Material 3 Expressive in Jetpack Compose, for phone, tablet, and desktop apps. Wear OS and widget guidance is included but not yet in scope. It gives an agent design judgment (hierarchy, color, type, shape, motion, and component choice), distilled from Google's official guidance.

## Install

With [Chai](https://github.com/charliesbot/chai), add this repo's `skills` folder as a local skill source in `~/chai.toml` and run `chai sync`:

```toml
[skills]
local = ['~/projects/android-design/skills']
```

Chai syncs the whole `skills/` folder, which installs two skills: `android-design` (design judgment for building, refining, and reviewing real Compose) and `android-design-explore` (proposals in the previewer app before implementation, built on `android-design`). Any agent that reads skills can also load `skills/android-design/SKILL.md` and `skills/android-design-explore/SKILL.md` directly.

## The previewer app

Design exploration runs in a native macOS app in `app/`. The agent writes a proposal as a JSON file (`design/<slug>.proposal.json`, next to the app's `DESIGN.md`), and the app draws it with real Compose Material 3 components in a live window that redraws on every save. The agent checks its work through the app's layout tree over MCP; you judge the design in the window, switching device, light and dark, seed color, and palette style. When you approve a direction, the app exports it as Kotlin for the port.

The names map one to one: the previewer app is the `AndroidDesignPreview.app` bundle, its window title is "Android Design Preview", and it registers as the MCP server `android_design_preview`.

### Build and install

Needs macOS and JDK 17.

```bash
cd app
./gradlew :app:installApp
```

This installs `~/Applications/AndroidDesignPreview.app`. Its binary, `~/Applications/AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview`, is what your agent harness runs.

To share the app, `./gradlew :app:packageDmg` builds a `.dmg`. It is unsigned and not notarized, so anyone who installs it from the DMG must clear the macOS quarantine flag on the app before its first launch; a quarantined copy started by an agent harness never responds.

### Register the MCP server

Register the binary as a stdio MCP server named `android_design_preview`, using its absolute path:

- **Claude Code:** `claude mcp add --scope user android_design_preview -- <binary>`
- **Codex:** add a `[mcp_servers.android_design_preview]` table to `~/.codex/config.toml` with `command = "<binary>"` and `default_tools_approval_mode = "approve"`.
- **Antigravity:** `agy mcp add android_design_preview <binary>`

Each agent session starts its own app process and window, and the process exits with the session. No port is opened.

### Other commands

The same binary renders headlessly for scripts and evals: `render <proposal.json> --out <dir>` writes PNGs, layout trees, and a `report.json`; `export <proposal.json>` prints Kotlin. After changing the app, run `./gradlew build spotlessCheck` in `app/`.

## Implementing real Compose

For implementing and reviewing real Compose code, and for Wear work, install the [Compose preview CLI](https://github.com/yschimke/compose-ai-tools) and the Android SDK:

```bash
curl -fsSL https://raw.githubusercontent.com/yschimke/skills/main/scripts/install.sh | bash -s -- --cli-only
```

Then install the [`compose-preview` and `compose-ui-builder` skills](https://github.com/yschimke/skills) and check the setup with `compose-preview doctor`.

The manual export check in [`evals/README.md`](evals/README.md) needs only JDK 17 and the Android SDK: it renders `evals/compose-starter/` with Gradle's `updateDebugScreenshotTest`. Python 3 runs the evals and the source scripts.

## What is here

| Path | Job |
| --- | --- |
| `skills/android-design/` | The design judgment skill |
| `skills/android-design-explore/` | The exploration skill: the proposal workflow, the proposal format, and the Compose porting table |
| `app/` | The previewer app: live window, MCP server, headless renderer, and Kotlin export. `app/schemas/` holds the generated catalog and proposal schemas |
| `docs/research/` | Distilled research from m3.material.io, developer.android.com/design/ui, and official videos and articles |
| `docs/research/source-coverage.md` | Every official source page and whether its guidance reached the research |
| `evals/` | Headless agents build four phone briefs, and the renders are checked against the skill's rules. `evals/compose-starter/` is the Compose sandbox for the manual export check, and `evals/verify/verify.proposal.json` uses every catalog component |
| `sources/` | Every official source page as Markdown, fetched by `scripts/fetch.py` |
| `scripts/` | `fetch.py` refreshes `sources/`; `check_coverage.py` checks it against the coverage ledger; `material3_inventory.py` lists the material3 names `compose-mapping.md` may use |

## History

The skill started in [charliesbot/skills](https://github.com/charliesbot/skills): [#6](https://github.com/charliesbot/skills/pull/6) added it and [#7](https://github.com/charliesbot/skills/pull/7) added the evals. It moved here to live with its source corpus and evals.
