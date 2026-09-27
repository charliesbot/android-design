# android-design

An agent skill for designing Android UI with Material 3 Expressive in Jetpack Compose: phone, tablet, desktop, Wear OS, and widgets. It gives an agent design judgment (hierarchy, color, type, shape, motion, and component choice), distilled from Google's official guidance.

## Install

With [Chai](https://github.com/charliesbot/chai), add this repo's `skills` folder as a local skill source in `~/chai.toml` and run `chai sync`:

```toml
[skills]
local = ['~/projects/android-design/skills']
```

Any agent that reads skills can also load `skills/android-design/SKILL.md` directly.

## What is here

| Path | Job |
| --- | --- |
| `skills/android-design/` | The skill itself |
| `docs/research/` | Distilled research from m3.material.io, developer.android.com/design/ui, and official videos and articles |
| `docs/research/source-coverage.md` | Every official source page and whether its guidance reached the research |
| `evals/` | Headless agents build six briefs, and the renders are checked against the skill's rules |

## History

The skill started in [charliesbot/skills](https://github.com/charliesbot/skills): [#6](https://github.com/charliesbot/skills/pull/6) added it and [#7](https://github.com/charliesbot/skills/pull/7) added the evals. It moved here to live with its source corpus and evals.
