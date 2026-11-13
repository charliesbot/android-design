# Visual design exploration v3

**Date:** 2026-10-30

## TL;DR

Design exploration moves from HTML proposals to a native macOS previewer app built with Compose Multiplatform. The agent writes a proposal as an A2UI v1.0 file against our own Material 3 Expressive catalog. The app watches that file and draws it with real Compose Material 3 components in a live window, redrawing about 130 ms after a save with no compile step. The agent checks its work from a layout tree it reads over a stdio MCP server, never from screenshots, while the owner judges taste in the live window. Speed and likeness to native Compose are the two goals, and every earlier version traded one for the other.

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
2. The app notices the change, parses and validates it, runs a quick headless pre-check, and updates Compose state. The window then redraws in its next frame.
3. The agent calls `check_proposal`, which checks every screen at all five views and returns only the errors and a layout summary. It reads a full tree with `get_tree` only where it needs geometry.
4. The owner looks at the window and replies in chat.

Measured on this Mac with Compose Multiplatform 1.13.0-beta01:
- From saving the file to the window adopting it takes about 130 ms warm (median of 10). About 70 ms of that is noticing the change: the watcher polls every 20 ms and waits 50 ms for the writes to settle. About 40 ms is the pre-check. Adopting the new state then reaches the screen in one 60 Hz frame (6 to 17 ms).
- `check_proposal` takes about 0.2 s for one screen at five views, and `get_tree` about 40 to 100 ms.

In a timed benchmark on 2026-11-01 (the `music` eval brief, the default Claude model, three runs, traced by event timestamps), the tools added about 2.5 s to a median 160 s agent run; the rest was model time. The same benchmark on the HTML version took a median 281 s. Three runs on one brief is a small sample.

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

The desktop material3 covers the skill's component guidance; [component-coverage.md](component-coverage.md) tracks which of its components the catalog draws so far. Every name that `skills/android-design-explore/references/compose-mapping.md` relies on exists in the desktop jar. The only miss is `isDark`, a false match, because the mapping uses `isDark` as a MaterialKolor parameter. The 16 names the desktop jar lacks are Android-only APIs: wallpaper dynamic color, secure window flags, and carousel keyline internals.

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

The device sizes are 411×923, 704×933, 1280×800, and 1920×1080 dp, one view per size class: compact phone, medium foldable, and the expanded and larger tablet and desktop. The foldable is the open Fold8 held upright. v2 turned it to landscape (933×704, expanded), which left the medium layout unchecked. The viewer itself uses `FlexBox`, `Grid`, and `MediaQuery`, all present in Compose Multiplatform 1.13.0-beta01 as experimental APIs, so its panel reflows when the window narrows.

### Files

Proposals are tracked files in the app's repository:
- `DESIGN.md` at the app's root holds the shared design system.
- Each proposal is `design/<slug>.proposal.json`.

The v2 rule of git-ignoring proposals is gone. Derived data, such as the tree and findings, is never stored.

**`DESIGN.md`** follows Google's format at `version: alpha`. The app parses and validates it itself; Google's lint CLI is not run, because it would bring back the Node tooling v3 removed. The spec allows custom top-level keys, and its linter warns only on near misses of its own key names, so the keys below stay clear of those. Material settings that the spec cannot express go under one custom key, `material:`:
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
- `navigation`: the start screen, each screen's pane role, optionally the screen a list pane's empty detail pane shows (`placeholders`), and the app's top-level `destinations` (3 to 5, with `railExpanded`), drawn once as a bar at compact and a rail from medium up, outside the panes
- `screens`: one A2UI `createSurface` body per screen, with its components and its sample `dataModel`

The app only reads the proposal; the agent owns the file and writes it one component per line, so diffs stay readable.

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

**Interaction.** Interaction stays inside the renderer. The catalog declares local functions (`navigate(screen)`, `back()`, `open(overlay)`, `dismiss()`), which components call through A2UI `functionCall`, never through events to the agent. Sheets, dialogs, and menus open over a screen, and selection and tabs switch locally. The navigation edges are exactly these `navigate` calls, which the app collects and checks against the declared screens, so the graph has one source of truth.

### MCP

**One instance, many sessions.** Agents never run the instance, because a harness stops its MCP child when it exits and that would end every other agent's session. The harness still launches the app as a stdio MCP server, but that process is always a relay: it runs headless (no Dock icon), connects to the Unix domain socket at `~/Library/Application Support/AndroidDesignPreview/mcp.sock`, copies stdin to the socket and the socket to stdout, and exits when either side ends. When nothing answers on the socket, the first agent's process spawns the instance detached with the `--instance` argument (through `open -a` for the packaged app, which hands the process to launchd; as a relaunched JVM when run from Gradle), polls the socket every 50 ms for up to 10 s, then relays. The instance owns the window and opens one session per connection, each with its own viewer, controller and MCP server, so agents never see each other's proposal, device or view. The proposal repository, the headless renderer and the window are shared. A stale socket file left by a crash is replaced on the next start. The environment variable `ANDROID_DESIGN_PREVIEW_SOCKET` replaces the socket path (for the relay and the instance, including one started through `open --env`), so a build can run its own instance beside an installed one; the evals set it per generator run.

The owner can also launch the app from Finder or `open`. Those start from launchd (parent pid 1) and run the instance directly: the app shows the window at once and stays up with no session. A second launch of the same bundle is routed by macOS to the running app.

**Protocol rules the app must keep:**
- stdout carries only JSON-RPC. The app keeps the real stdout for the transport, redirects `System.out` to stderr, and runs with `-XX:+DisplayVMOutputToStderr`, because HotSpot prints warnings to stdout by default.
- A session ends on its transport's close (`transport.onClose`). `Server.onClose` only fires on an explicit server close, so it can't be used for this.

**Tools:**
- `open_proposal(path)`
- `check_proposal(screens?, views?)`: every screen at all five views by default, tree-only, returning pass or fail per view, each error grouped by rule with its node ids, `notes` (the info findings, by rule with ids, never a failure), and a `layout` summary per view (navigation shown, panes, and each pane's main regions as `Type id width`, under 300 bytes) so the agent confirms adaptation without full trees. A view equal to an earlier one of the screen is only `{"same": "<view>"}`, and a top-level `colors` gives the hex of each color role the renders use, for light and dark, so contrast and fill strength are judged without a tree. About 0.2 s for one screen at five views.
- `get_tree(screen?, view?)`: the full tree of one view
- `set_view(device?, dark?, seed?, variant?, screen?)`: changes the window only; `screen` moves it to a screen as the side panel does
- `show_preview()`
- `get_catalog(component?)`: a compact text catalog (about 11K characters, one line per component) by default, or the full JSON Schema of the named components. The full catalog exceeds MCP result limits, so it is never sent whole.
- `export_kotlin(screen?)`
- `status()`

An earlier round of that benchmark showed why `check_proposal` exists: with one view per `get_tree`, agents made a median 13 check calls per run.

The agent never edits proposals through MCP; the file on disk is the source of truth.

**Exit rule and closing the window.** A session is removed when its transport closes. A spawned instance exits when its last session closes, within about 45 ms of the client going away, and removes the socket file; an instance the owner started stays up. Closing the window hides it while any agent is connected and quits otherwise. The Dock icon or `show_preview` brings the window back, and a tool call that shows the window also selects the session that made it.

**App Nap.** macOS throttles an idle background app about 30 s after its last activity, which slowed a check from about 0.2 s to 1 to 2.6 s. The `Info.plist` opt-out keys are ignored for a binary an MCP client launches directly, so at launch the app sets `NSAppSleepDisabled` in its own preferences domain whenever it is not already on. The timings in this document depend on it.

**The device frame** is its own Compose scene (a `ComposePanel`), scaled by lowering its density. The proposal still lays out at the device's dp size, and sheets, dialogs, and menus open inside the frame instead of over the viewer.

**A proposal that crashes while composing** cannot take the window down:
- **Validation first.** Numeric props carry bounds in the catalog. The validator rejects literals outside them, and the renderer clamps bound data to the same bounds.
- **A quick pre-check.** Before the window adopts a new version, the app renders what it is about to show headlessly, tree only: the current screen at the current view, with the window's back stack and open overlays replayed. If that throws, the last good version stays on screen.
- **A backstop.** Anything that throws later, on another screen or after a device switch, is caught by the frame's own exception handler. The frame then drops to an error panel and rebuilds on the next good version.

Every case is reported through the tool result and `status`, as validation errors are.

**Harness setup:**
- **Claude Code:** registered as a stdio server with the app binary as its command. Verified end to end.
- **Codex:** registered under `mcp_servers.<name>` with `default_tools_approval_mode = "approve"`. Without that, `approval_policy = "never"` refuses the call. Verified end to end.
- **Antigravity:** registered with `agy mcp add <name> <binary>`, or through its `mcp_config.json`. Verified end to end. Interactive sessions ask before each tool call. Headless runs (`agy -p`) cannot ask, so they need `mcp(<name>/<tool>)` allow rules in the global `settings.json`, because a project-level `.agents/settings.json` is not read for this.

The skill checks that the server is connected before it starts and, if not, tells the user how to register it.

### Layout tree

The agent reads a flat list of nodes plus findings. Each node carries:
- its proposal id, type, and parent
- its bounds and touch bounds in dp, plus the unclipped layout box when it reaches past the bounds; a carousel item's bounds are the rect its mask shows (`carouselItemDrawInfo.maskRect`, read when the tree is taken), since the mask crops a large item on both sides, and its layout box is kept in `layout`
- for text: the line count, overflow, and whether it was truncated
- the color, type, and shape roles it used
- its accessibility role, label, and actions

Everything comes from public APIs; no internal Compose APIs (`PlatformContext` and `ComposeScene` are `@InternalComposeUiApi`). Every tree comes from a headless render with `runDesktopComposeUiTest`:
- The renderer measures each proposal node itself with `onGloballyPositioned` and `onTextLayout`: ids, bounds, and text overflow and truncation.
- Compose's semantics tree, keyed by the node ids as test tags, adds touch bounds (a 40 dp button reports a 48 dp touch area) and the accessibility role, label, and actions.

For `get_tree` without arguments, the headless render replays the window's back stack and interaction state (open overlays, selections, typed text), so the tree describes exactly what the window shows, whether the window is visible or hidden. With arguments, it renders that screen and view fresh. `check_proposal` and `render` add one render per overlay component of a screen (sheet, dialog, menu, search view, tooltip, snackbar, picker) with that overlay open, named `screen+overlay`, at the same views and under the same findings; an overlay opens as the window would open it, on the screen's own back stack. A scrolling Wear list is scrolled from top to end in the headless render, so its tree and findings cover every item. In a list-detail scene, nodes from the other pane carry their screen as a prefix (`plates/new_plate`), and per-pane rules such as `primary-largest` judge each pane on its own.

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

The finding rules carry over from v2's runtime checks, with v2's thresholds:
- touch targets of at least 48 dp
- the bundled families per type role: Google Sans Flex for plain text, a brand face only on display, headline, and large title (Doto on display only)
- clipped text, and truncated or wrapped labels on buttons, chips, and navigation items
- contrast of each text and background pair
- the primary action as the largest interactive element in its pane
- at most one FAB per pane
- nothing wider than the view (`offscreen`)
- nothing laid out wider than its parent or past the view's edge (`overflow`): bounds come from semantics and clip to the window, so the tree adds a node's unclipped `layout` box when it reaches past `bounds`; the children of a carousel or tab row, which scroll sideways, are exempt
- no text past the visible edges of its carousel item (`text-clipped`): the item's mask crops it, which the unmasked layout box would hide, so a text at the item's edge with no padding is reported with the distance it is cut by
- no text or control past the arc of a rounded corner that clips it (`corner-clipped`): a Card, or a container its style gives a `container` and a corner `shape`, records `clip: {radius, outset}` in the tree (the outset is a styled container's padding, since its bounds are the box inside that padding), and a text is judged by its drawn lines and a control by its bounds, as on Wear's round edge
- no weighted child that measures zero along its parent's axis (`weight-collapsed`): the tree records a node's `weight` as `{value, axis, size}` (`width` in a Row, `height` in a Column; `size` is the measured size in dp, uncut by the window or a scroll viewport, unlike `bounds`), and a zero size there means the parent gave it nothing to share, as a scrolling Column does once its content is taller than the screen, or when it does not fill its parent; a `Spacer` is exempt, since taking what is left (possibly nothing) is its job
- no arrangement without room (`arrangement-no-space`, a note): a Row or Column whose `arrangement` needs free space (`spaceBetween`, `spaceAround`, `spaceEvenly`, `center`, `end`) but wraps its content, so it is exactly as wide (or tall) as its children and the arrangement does nothing. The tree records `arrangement: {name, axis, free, wraps, children}`: `free` is the measured main-axis size less the children and gaps, uncut by the window; `wraps` is true when the container is neither stretched along that axis nor weighted and no child is weighted. The note gives the cause and generic guidance and names no node: for a Row, weight the pane or column that wraps it in its parent row; for a Column, weight it in its parent column, or give the row that holds it a height and `verticalAlignment: stretch`. One child spread between is not reported

A view passes when it has no error findings. Intentional truncation is reported as information, not an error.

**Screenshots** never drive the agent's loop. The owner judges hierarchy, character, and color balance in the window. The agent gets one look: once `check_proposal` passes, `render_view` returns a headless PNG of a screen at a view (1 px per dp), and the skill allows one per screen at the phone and widest views. It exists because agents left without a way to see the UI took screen control of the Mac to capture the window, which is slow, prompts the user, and captures whatever the window happens to show. A sanctioned render also exposes previewer drawing bugs the tree cannot show, such as the padding that once drew outside its container. Fixes still come from the tree. Evals' grader also gets one headless PNG per screen and view, because "distinctive design" cannot be judged from structure alone, while the tree drives the deterministic checks.

### Export to Compose

Export walks the proposal and emits Kotlin with component parameters and modifiers, plus Navigation 3 entries and scene strategies for the graph. Named styles become parameters and modifiers, not the experimental Styles API, which material3 components do not accept yet and which would require an alpha foundation and an opt-in in the target app.

The export is checked in two ways. Golden tests export a two-screen fixture and `evals/verify/verify.proposal.json`, which uses every catalog component, and compare the output byte for byte. Both goldens compile and assemble in a copy of `evals/compose-starter`, the Android sandbox pinned to material3 1.5.0-alpha29, which proves the exported names exist in the Android library and not only on desktop. That compile step is manual, run after changing the exporter.

These checks prove the export is stable and compiles. They do not prove the exported screen looks like the preview: nothing compares the two automatically. Where a prop is omitted, the exporter and the renderer read the same catalog default, which removes one class of drift. The rest is the manual export check in `evals/README.md`, which compares an exported screen with its preview by eye.

**Command line.** The same binary has two subcommands for scripts. `render <proposal.json> --out <dir>` writes headless PNGs, trees, and a `report.json` for the evals. `export <proposal.json>` prints the Kotlin for the export check.

### Distribution

- **The owner** builds the app locally. A locally built app carries no quarantine flag, and a copy moved to another folder still starts as an MCP server (verified).
- **Everyone else** gets an unsigned `.dmg` built with Compose's `packageDmg`, ad-hoc signed and not notarized. There is no paid Apple Developer account. Users must clear macOS's quarantine flag before the app's first launch: a quarantined copy launched by a harness gives no response, and stays broken even after the flag is removed.

### What v3 replaces

v3 replaces the HTML proposal runtime: `@m3e/web`, `gallery/runtime-src/`, `gallery/public/`, the lint, `view.html`, the proposal starter, and the HTML half of `compose-mapping.md`. It also moves the evals onto the app's headless renderer.

`android-design` is unchanged: it still owns every design decision. `android-design-explore` becomes the workflow that connects to the app, writes proposals, reads trees, and hands an approved proposal to the export.

## Links

- [Visual design exploration v2](../archive/visual-design-exploration-v2.md), the HTML design this replaces
- [Compose Multiplatform proposal fidelity research](../research/compose-multiplatform-proposal-fidelity.md)
- [A2UI specification v1.0](https://github.com/a2ui-project/a2ui/tree/main/specification/v1_0)
- [AndroidX A2UI Compose releases](https://developer.android.com/jetpack/androidx/releases/a2ui-compose)
- [DESIGN.md specification](https://github.com/google-labs-code/design.md/blob/main/docs/spec.md)
- [MCP Kotlin SDK](https://kotlin.sdk.modelcontextprotocol.io)
- [Compose Multiplatform changelog](https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md)
- [Compose Hot Reload v1.2.0, with its MCP server](https://github.com/JetBrains/compose-hot-reload/releases/tag/v1.2.0)
- [Compose Styles API skill](https://developer.android.com/agents/skills/jetpack-compose/theming/styles/skill)
