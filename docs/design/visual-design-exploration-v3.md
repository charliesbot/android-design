# Visual design exploration v3

**Date:** 2026-10-30

## TL;DR

Design exploration moves from HTML proposals to a native macOS previewer app built with Compose Multiplatform. The agent writes a proposal as an A2UI v1.0 file against our own Material 3 Expressive catalog. The app watches that file and draws it with real Compose Material 3 components in a live window, redrawing in one frame with no compile step. The agent checks its work from a layout tree it reads over a stdio MCP server, never from screenshots, while the owner judges taste in the live window. Speed and likeness to native Compose are the two goals, and every earlier version traded one for the other.

## Design

### Why a native app

Each earlier version bought speed with fidelity or fidelity with speed:

- **v1, Compose rendered to PNG:** real components. But about ten minutes to the first round and two per change, compile-fix loops against alpha material3 APIs, soft scaled images, and no live switching of size or theme.
- **v2, HTML on `@m3e/web`:** minutes per round, sharp, and `view.html` switches device and theme live. The components come from a third-party web library and visibly differ from Compose. The agent still judged its work from screenshots, which remained the slowest step.

v3 draws with Compose itself, so the preview is the real component rather than a lookalike. Speed comes from never compiling during iteration: the app is built once, and a proposal is data that the app interprets.

Rejected alternatives:
- **An Android emulator:** heavy, and parallel agent sessions would share one emulator.
- **Compose Hot Reload with the agent writing Kotlin:** a live window, but every change compiles, which brings back v1's compile-fix loop. Its MCP server, released in July 2026, is still a useful reference for tool design.
- **Anything that listens on a network port:** parallel sessions fought over ports in the v1 gallery.

### One iteration

1. The agent edits `design/<slug>.proposal.json` with its normal file tools. Small edits are ordinary file edits, so no patch protocol is needed.
2. The app watches the file, parses it, and updates Compose state. The window redraws in the next frame.
3. The agent calls `get_tree` and reads a few hundred tokens of layout text.
4. The owner looks at the window and replies in chat.

Measured on this Mac with Compose Multiplatform 1.13.0-beta01:
- A state change reaches the next frame of a live window in 6 to 17 ms, which is one 60 Hz frame.
- Headless, an update plus a fresh semantics tree takes about 1.5 ms. The first headless render takes 0.9 s.

Model time writing the edit is the only wait left.

### The app

The app lives in `app/` at the root of this repository, never under `skills/`, which Chai installs wholesale. It is a companion to `android-design-explore` and is not meant to be used on its own. macOS comes first; Linux comes later on the same codebase.

| Concern | Choice |
|---|---|
| Language and UI | Kotlin 2.4.21, Compose Multiplatform 1.13.0-beta01 (desktop JVM) |
| Components | JetBrains `material3` 1.13.0-beta01. JetBrains' changelog maps it to AndroidX material3 1.5.0-beta01. |
| Navigation | JetBrains `navigation3-ui` 1.2.0 and Material 3 `adaptive-navigation3` 1.3.0, both stable desktop releases. The latter includes `ListDetailSceneStrategy` and `SupportingPaneSceneStrategy`. |
| Color | MaterialKolor 5.0.1. It matched Google's color utilities on 717 of 720 role values in the v2 comparison. |
| Agent connection | Official Kotlin MCP SDK `io.modelcontextprotocol:kotlin-sdk` 0.16.0, `StdioServerTransport` |
| Parsing | kotlinx.serialization for proposals, and a Kotlin YAML library for `DESIGN.md` front matter |

The desktop material3 covers the skill's component guidance. Every name that `skills/android-design-explore/references/compose-mapping.md` relies on exists in the desktop jar. The only miss is `isDark`, a false match, because the mapping uses `isDark` as a MaterialKolor parameter. The 16 names the desktop jar lacks are Android-only APIs: wallpaper dynamic color, secure window flags, and carousel keyline internals.

**Architecture.** The app follows the `android-dev` skill's layering and conventions, adapted for a desktop platform, for consistency with the owner's apps. Its Android bootstrap scripts do not apply.

| Module | Holds |
|---|---|
| `:core:model` | Proposal, catalog, theme, layout tree, and finding types |
| `:core:domain` | Finding rules, Kotlin export, the proposal repository interface |
| `:core:data` | File watching, the `DESIGN.md` parser |
| `:features:preview:desktop` | The viewer and the renderer |
| `:app` | The desktop shell: `main()`, the window, Koin wiring |
| `:mcp` | The MCP server, an entry point for agents the way widgets are entry points for the OS |

Because finding rules and export are pure Kotlin, they test without Compose, and the headless renderer reuses the same composables as the window.

**Viewer.** The viewer keeps the layout of v2's `view.html`, rebuilt in Compose:
- A side panel with Brightness (light, dark), Color (the proposal's seed plus six presets), and Style (color variants).
- A list of the proposal's screens, to jump to any of them.
- A stage with Phone, Foldable, Tablet, and Desktop buttons, the proposal name with its size, and the framed preview.

The device sizes stay 411×923, 933×704, 1280×800, and 1920×1080 dp. The viewer itself uses `FlexBox`, `Grid`, and `MediaQuery`, all present in Compose Multiplatform 1.13.0-beta01 as experimental APIs, so its panel reflows when the window narrows.

### Files

Proposals are tracked files in the app's repository:
- `DESIGN.md` at the app's root holds the shared design system.
- Each proposal is `design/<slug>.proposal.json`.

The v2 rule of git-ignoring proposals is gone. Derived data, such as the tree and findings, is never stored.

**`DESIGN.md`** follows Google's format at `version: alpha`, linted with a pinned version of its CLI. The spec allows custom top-level keys, and its linter warns only on near misses of its own key names. Material settings that the spec cannot express go under one custom key, `material:`:
- **Seed color:** the seed is `colors.primary`, which the linter expects. `material.scheme` names the variant and contrast and refers to the seed as `{colors.primary}`. Explicit role colors in `colors` override generated ones.
- **Type:** typography uses the spec's `fontVariation` string, for example `"'wght' 500, 'wdth' 100, 'ROND' 100"`. The app parses it into Compose `FontVariation.Settings`.
- **Corners:** the corner scale uses `rounded`, with Material's names in px read as dp. Corner families and shape-library shapes, which `rounded` cannot hold, go under `material:`.
- **Named styles:** `material.styles` holds named styles shared across proposals. Their values are token names only (type role, color role, shape, spacing name), never hex or dimensions. That also keeps the linter's ignored-token warning quiet.

```yaml
---
version: alpha
name: Tonight's plate
colors:
  primary: "#B5523B"
rounded:
  extra-large: 28px
material:
  scheme: { seed: "{colors.primary}", variant: expressive, contrast: 0 }
  styles:
    hero_title: { type: displayLargeEmphasized, color: onPrimaryContainer }
---
```

**`design/<slug>.proposal.json`** wraps A2UI content in our own envelope. The A2UI wrapper file allows no keys besides `messages`, so the envelope is ours. It holds:
- `$schema` and a `format` version
- an optional `theme` override with the same keys as `DESIGN.md`, for exploring a new look
- `navigation`: the start screen, plus each screen's pane role
- `screens`: one A2UI `createSurface` body per screen, with its components and its sample `dataModel`

The app writes the file back with stable key order and pretty printing, so diffs stay readable.

```json
{
  "$schema": "<the app's published proposal schema>",
  "format": 1,
  "navigation": {
    "start": "plates",
    "panes": { "plates": "list", "plate_detail": "detail" }
  },
  "screens": {
    "plates": {
      "catalogId": "<our catalog id>",
      "dataModel": { "plates": [{ "name": "Burrata and tomatoes" }] },
      "components": [
        { "id": "root", "component": "Scaffold", "topBar": "bar", "content": "list" },
        { "id": "bar", "component": "TopAppBar", "title": "Tonight's plate" },
        { "id": "list", "component": "Column", "children": { "componentId": "plate_row", "path": "/plates" } },
        { "id": "plate_row", "component": "ListItem", "headline": { "@path": "name" },
          "action": { "functionCall": { "@call": "navigate", "args": { "screen": "plate_detail" } } } }
      ]
    }
  }
}
```

### Catalog

Proposals use A2UI v1.0, Google's agent-to-UI format:
- Components sit in a flat list with stable ids, and exactly one is `root`.
- Parents reference children by id, through `Child` (one) or `ChildList` (several).
- An agent may only use components that a catalog declares.

We pin `protocolVersion: "1.0"` and keep copies of Google's schemas at a fixed commit, because v1.0 is a release candidate that Google plans to finalize in Q4 2026.

Our own catalog declares the Material 3 Expressive components the skill teaches, mirroring the material3 Compose APIs:
- **Named slots are `Child` props.** For example, `Scaffold` has `topBar`, `content`, and `floatingActionButton`. That keeps a document the same shape as the Compose call, so export is a direct translation.
- **Token props are `$defs` enums** (color roles, type roles, shapes, spacing names), so a proposal cannot carry raw values. A node may name a style from `material.styles`.
- **Names align with Google's where they overlap.** Where our components overlap the basic catalog that androidx `material3-a2ui` implements (`Text`, `Button`, `Card`, `Row`, `Column`, `Icon`, `Image`, `Divider`, `TextField`, `CheckBox`, `Slider`, `Tabs`, `Modal`), we reuse its component and prop names. Values stay Material tokens rather than its `h1` or `primary` variants.
- **Our own identity.** The catalog has its own id and never reuses the basic catalog's id. Ids follow A2UI's identifier rule, so they use underscores, not hyphens.

The app is the catalog's single source of truth and serves it over MCP (`get_catalog`), so the skill cannot drift from the installed app. The first catalog covers what `compose-mapping.md` already verified. Supporting a new component is an app change and a rebuild, not something a proposal can improvise.

### Responsive layout and navigation

**Responsive layout.** One document per screen adapts to every window size. Layout nodes include `Grid` and `FlexBox`, and width rules at 600 and 840 dp switch arrangements, the same breakpoints the skill teaches. The device buttons only resize the frame; the document reacts.

**Navigation.** A proposal is a navigation graph of screens. The app runs it with Navigation 3, using Material 3's `ListDetailSceneStrategy` and `SupportingPaneSceneStrategy`, so at expanded widths a list and its detail appear side by side. That is the same stack the `android-dev` skill uses for real apps.

**Interaction.** Interaction stays inside the renderer. The catalog declares local functions (`navigate(screen)`, `openSheet(sheet)`, `dismiss()`), which components call through A2UI `functionCall`, never through events to the agent. Sheets, dialogs, and menus open over a screen, and selection and tabs switch locally. The navigation edges are exactly these `navigate` calls, which the app collects and checks against the declared screens, so the graph has one source of truth.

### MCP

**One process per session.** The harness launches the app as its MCP server over stdio, so each session gets its own process and window. Verified on this Mac:
- Two instances answered independently, and `lsof` showed neither listening on any port.
- When the client exits or its stdin closes, the app exits within about 45 ms and leaves no orphan process.
- Launching the packaged app's binary directly, the server answers `initialize` in 180 to 190 ms warm and about 0.9 s cold. The window appears 0.6 to 1.4 s after launch.

**Protocol rules the app must keep:**
- stdout carries only JSON-RPC. The app keeps the real stdout for the transport, redirects `System.out` to stderr, and runs with `-XX:+DisplayVMOutputToStderr`, because HotSpot prints warnings to stdout by default.
- It exits on transport close (`transport.onClose`). `Server.onClose` only fires on an explicit server close, so it can't be used for this.

**Tools:**
- `open_proposal(path)`
- `get_tree(screen?, view?)`
- `set_view(device, brightness, seed, style)`
- `show_preview()`
- `get_catalog()`
- `export_kotlin(screen?)`
- `status()`

The agent never edits proposals through MCP; the file on disk is the source of truth.

**Closing the window** hides it and keeps the process serving. A hidden window stops laying out (measured: tree reads go stale), so while hidden, `get_tree` comes from the headless renderer. The Dock icon or `show_preview` brings the window back.

**Harness setup:**
- **Claude Code:** registered as a stdio server with the app binary as its command. Verified end to end.
- **Codex:** registered under `mcp_servers.<name>` with `default_tools_approval_mode = "approve"`. Without that, `approval_policy = "never"` refuses the call. Verified end to end.
- **Antigravity:** registered with `agy mcp add <name> <binary>`, or through its `mcp_config.json`. Not yet run end to end.

The skill checks that the server is connected before it starts and, if not, tells the user how to register it.

### Layout tree

The agent reads a flat list of nodes plus findings. Each node carries:
- its proposal id, type, and parent
- its bounds and touch bounds in dp
- for text: the line count, overflow, and whether it was truncated
- the color, type, and shape roles it used
- its accessibility role, label, and actions

Everything comes from public APIs; no internal Compose APIs (`PlatformContext` and `ComposeScene` are `@InternalComposeUiApi`):

| Source | Gives | Used for |
|---|---|---|
| The renderer measuring itself (`onGloballyPositioned`, `onTextLayout`) | Ids, bounds, touch bounds (a 40 dp button reports a 48 dp touch area), text overflow and truncation | The live window, the main source |
| `javax.accessibility` on the live window | Role, label, states, and bounds, as a screen reader sees them. It updates after changes. | Accessibility facts, matched to nodes by bounds |
| `runDesktopComposeUiTest` | The full Compose semantics tree with ids as test tags | Headless use: a hidden window, evals |

Color and type roles come from the proposal and theme, so the app computes contrast from resolved roles instead of sampling pixels.

```json
{ "screen": "plates", "view": { "device": "phone", "width": 411, "height": 923 },
  "nodes": [ { "id": "open_plate", "type": "Button", "parent": "body",
    "bounds": [16, 108, 84, 40], "touch": [16, 104, 84, 48],
    "text": { "value": "Cook", "lines": 1, "overflow": false },
    "roles": { "container": "primary", "content": "onPrimary", "type": "labelLarge" },
    "a11y": { "role": "Button", "label": "Cook", "actions": ["click"] } } ],
  "findings": [ { "id": "desc", "rule": "text-truncated" } ] }
```

The finding rules carry over from v2's runtime checks:
- touch targets of at least 48 dp
- Google Sans Flex on every plain-text node
- clipped or overflowing text
- contrast of each text and background pair
- the primary action as the largest interactive element
- at most one FAB

**Screenshots** never enter the agent's loop. The owner judges hierarchy, character, and color balance in the window. Evals are the one exception: their grader gets the tree plus one headless PNG per view, because "distinctive design" cannot be judged from structure alone.

### Export to Compose

Export walks the proposal and emits Kotlin with component parameters and modifiers, plus Navigation 3 entries and scene strategies for the graph. Named styles become parameters and modifiers, not the experimental Styles API, which material3 components do not accept yet and which would require an alpha foundation and an opt-in in the target app.

Fidelity is checked by rendering the exported code with `compose-preview` and comparing its semantics with the app's tree through `diff_semantics`. Pixels are not compared.

### Distribution

- **The owner** builds the app locally. A locally built app carries no quarantine flag, and a copy moved to another folder still starts as an MCP server (verified).
- **Everyone else** gets an unsigned `.dmg` built with Compose's `packageDmg`, ad-hoc signed and not notarized. There is no paid Apple Developer account. Users must clear macOS's quarantine flag before the app's first launch: a quarantined copy launched by a harness gives no response, and stays broken even after the flag is removed.

### What v3 replaces

v3 replaces the HTML proposal runtime: `@m3e/web`, `gallery/runtime-src/`, `gallery/public/`, the lint, `view.html`, the proposal starter, and the HTML half of `compose-mapping.md`. It also moves the evals onto the app's headless renderer.

`android-design` is unchanged: it still owns every design decision. `android-design-explore` becomes the workflow that connects to the app, writes proposals, reads trees, and hands an approved proposal to the export.

## Open questions

- Antigravity has not run the app end to end. Its registration format is known, but testing it means adding the server to Antigravity's global config.

## Links

- [Visual design exploration v2](visual-design-exploration-v2.md), the HTML design this replaces
- [Compose Multiplatform proposal fidelity research](../research/compose-multiplatform-proposal-fidelity.md)
- [A2UI specification v1.0](https://github.com/a2ui-project/a2ui/tree/main/specification/v1_0)
- [AndroidX A2UI Compose releases](https://developer.android.com/jetpack/androidx/releases/a2ui-compose)
- [DESIGN.md specification](https://github.com/google-labs-code/design.md/blob/main/docs/spec.md)
- [MCP Kotlin SDK](https://kotlin.sdk.modelcontextprotocol.io)
- [Compose Multiplatform changelog](https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md)
- [Compose Hot Reload v1.2.0, with its MCP server](https://github.com/JetBrains/compose-hot-reload/releases/tag/v1.2.0)
- [Compose Styles API skill](https://developer.android.com/agents/skills/jetpack-compose/theming/styles/skill)
