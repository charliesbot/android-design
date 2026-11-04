# android-design

Agent skills for designing Android UI with Material 3 Expressive in Jetpack Compose, for phone, tablet, and desktop apps. Every rule is distilled from Google's official guidance.

- **`android-design`** gives an agent design judgment for building, refining, and reviewing real Compose: hierarchy, color, type, shape, motion, and component choice.
- **`android-design-explore`** lets an agent explore directions as live proposals in a native previewer app, then port the approved one to Compose.

## Install the skills

```bash
npx skills add charliesbot/android-design
```

Or copy the folders in `skills/` into your agent's skills directory (for Claude Code, `~/.claude/skills/`).

## Install the previewer app

Only `android-design-explore` needs it. Requires macOS and JDK 17.

```bash
git clone https://github.com/charliesbot/android-design
cd android-design/app
./gradlew :app:installApp
```

This installs `~/Applications/AndroidDesignPreview.app`. Register its binary as a stdio MCP server named `android_design_preview`. For Claude Code:

```bash
claude mcp add --scope user android_design_preview -- ~/Applications/AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview
```

Other agents take the same binary path as a stdio MCP command. All agents share one window, one tab per session. You can also open the app yourself and load a proposal with File > Open Proposal.

## Repository

| Path | What it holds |
| --- | --- |
| `skills/` | The two skills |
| `app/` | The previewer app: live window, MCP server, headless renderer, Kotlin export |
| `docs/research/` | Research distilled from Google's design sources |
| `sources/` | Every official source page as Markdown, fetched by `scripts/fetch.py` |
| `evals/` | Checks that agents following the skills still produce screens that meet their rules. See [`evals/README.md`](evals/README.md) |
