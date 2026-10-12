# android-design

An agent skill for designing Android UI with Material 3 Expressive in Jetpack Compose, for phone, tablet, and desktop apps. Wear OS and widget guidance is included but not yet in scope. It gives an agent design judgment (hierarchy, color, type, shape, motion, and component choice), distilled from Google's official guidance.

## Install

With [Chai](https://github.com/charliesbot/chai), add this repo's `skills` folder as a local skill source in `~/chai.toml` and run `chai sync`:

```toml
[skills]
local = ['~/projects/android-design/skills']
```

Any agent that reads skills can also load `skills/android-design/SKILL.md` directly.

## Requirements

1. Install Node.js 22+ for the gallery and design exploration. Exploration proposals are HTML reviewed in your agent's browser. The installed skill needs no build or `npm install`: it serves the committed build in `skills/android-design/gallery/public/` and the committed proposal runtime. Python 3 runs the evals and the source scripts.

2. For implementing and reviewing real Compose code, running the evals, and Wear work, install the [Compose preview CLI](https://github.com/yschimke/compose-ai-tools) and the Android SDK:

   ```bash
   curl -fsSL https://raw.githubusercontent.com/yschimke/skills/main/scripts/install.sh | bash -s -- --cli-only
   ```

3. Install the [`compose-preview` and `compose-ui-builder` skills](https://github.com/yschimke/skills).

4. Check the Compose setup:

   ```bash
   compose-preview doctor
   ```

### Developing the gallery frontend

The frontend is Vite and Lit source in `skills/android-design/gallery/src/`, built into the committed `skills/android-design/gallery/public/`. `npm install` is for developing the gallery or the proposal runtime only.

1. Serve an exploration: `node skills/android-design/gallery/cli.mjs serve <exploration> --port 8765`
2. Run `npm run dev` and open the URL Vite prints. It proxies `/api` and `/images` to port 8765.
3. Before committing, run `npm run build` and `npm test`.

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
