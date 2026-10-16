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

1. Install Node.js 22+ and Google Chrome for design exploration. Proposals are HTML rendered headlessly by `cli.mjs render`, so no browser tool or server is needed. The installed skill needs no build or `npm install`: it uses the committed proposal runtime in `skills/android-design/gallery/public/runtime/`. Python 3 runs the evals and the source scripts.

2. For implementing and reviewing real Compose code, running the evals, and Wear work, install the [Compose preview CLI](https://github.com/yschimke/compose-ai-tools) and the Android SDK:

   ```bash
   curl -fsSL https://raw.githubusercontent.com/yschimke/skills/main/scripts/install.sh | bash -s -- --cli-only
   ```

3. Install the [`compose-preview` and `compose-ui-builder` skills](https://github.com/yschimke/skills).

4. Check the Compose setup:

   ```bash
   compose-preview doctor
   ```

### Render and view

`node skills/android-design/gallery/cli.mjs render <exploration>` lints each proposal, renders it headlessly in Chrome (no port), and writes `renders/<id>/` screenshots for phone, foldable, tablet, and desktop, a `checks.json`, and a self-contained `view.html`, plus `renders/contact-sheet.png`. Double-click `view.html` to switch devices, light and dark, seed colors, and palette. Iteration happens in chat: the agent shows the contact sheet, you reply, it edits and renders again.

Google Sans Flex is shared with the proposal runtime, including its local font license. The runtime also bundles the optional brand and data faces (Fraunces, Bricolage Grotesque, Big Shoulders Display, Doto, Google Sans Code).

### Developing the proposal runtime

The runtime source is in `skills/android-design/gallery/runtime-src/`, built into the committed `skills/android-design/gallery/public/runtime/`. `npm install` is for developing the runtime and running the tests only.

1. Edit the runtime source.
2. Run `npm run build` to rebuild the runtime, then `npm test`.

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
