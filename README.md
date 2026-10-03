# android-design

An agent skill for designing Android UI with Material 3 Expressive in Jetpack Compose, for phone, tablet, and desktop apps. Wear OS and widget guidance is included but not yet in scope. It gives an agent design judgment (hierarchy, color, type, shape, motion, and component choice), distilled from Google's official guidance.

## Install

With [Chai](https://github.com/charliesbot/chai), add this repo's `skills` folder as a local skill source in `~/chai.toml` and run `chai sync`:

```toml
[skills]
local = ['~/projects/android-design/skills']
```

Any agent that reads skills can also load `skills/android-design/SKILL.md` directly.

## Visual tooling prerequisites

Install the [`compose-preview` CLI](https://github.com/yschimke/compose-ai-tools) and the [`compose-preview` and `compose-ui-builder` skills](https://github.com/yschimke/skills) before using visual workflows. MCP integration is optional; the preview skill supports the CLI directly.

```bash
compose-preview --version
compose-preview doctor
```

The skill assumes this setup is complete. `android-design` owns design judgment and exploration isolation; `compose-preview` owns rendering existing Compose code. `compose-ui-builder` owns catalog-backed design authoring when that workflow is requested. The builder's PNG/SVG export requires Java 21+, while checkout rendering requires Java 17+ and the project's Android toolchain.

Exploration currently uses a temporary copy of the bundled starter, leaving the target app unchanged. Installing the builder does not automatically switch exploration to a projectless workflow. The phone starter has been rendered with compose-preview 2.33.0, including light/dark and main-source previews. Wear remains outside the validated design scope. The existing screenshot harness is a maintenance check, not a fallback for the agent.

## What is here

| Path | Job |
| --- | --- |
| `skills/android-design/` | The skill itself |
| `docs/research/` | Distilled research from m3.material.io, developer.android.com/design/ui, and official videos and articles |
| `docs/research/source-coverage.md` | Every official source page and whether its guidance reached the research |
| `evals/` | Headless agents build four phone briefs (plus parked Wear and widget briefs), and the renders are checked against the skill's rules |
| `sources/` | Every official source page as Markdown, fetched by `scripts/fetch.py` |
| `scripts/` | `fetch.py` refreshes `sources/`; `check_coverage.py` checks it against the coverage ledger |

## History

The skill started in [charliesbot/skills](https://github.com/charliesbot/skills): [#6](https://github.com/charliesbot/skills/pull/6) added it and [#7](https://github.com/charliesbot/skills/pull/7) added the evals. It moved here to live with its source corpus and evals.
