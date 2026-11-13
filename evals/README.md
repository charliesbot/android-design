# android-design evals

Briefs, rendering, code checks, and rubric judgments are independent of any agent provider. Run the evals after changing the skill, not on a schedule. The current chat, a human, or an optional CLI adapter can supply implementations and judgments.

An eval case is the skill's current output: one proposal for the previewer app, `design/proposal.proposal.json` plus a root `DESIGN.md` (see [exploration workflow checks](exploration.md)). Requirements: macOS and the previewer app installed (`./gradlew :app:installApp` from `app/`). No Node.js, Chrome, or Android SDK. The runner finds the app through `app_binary()` in `preview_app.py`: `$ANDROID_DESIGN_PREVIEW` (path to the executable), then `~/Applications/AndroidDesignPreview.app`, then the repo's build output, and stops with the install command when none exists.

## Use the current chat or another agent

From this directory:

```bash
python3 run.py prepare --out /tmp/design-eval --cases music
```

Preparation makes `music/` a fresh Git repository fixture (one empty commit) with `design/` holding the brief's assets in `design/assets/`, and a snapshot of both skills (`skills/android-design` and `skills/android-design-explore`). There is no starter and no `DESIGN.md`: the agent writes `DESIGN.md` at the fixture root and the proposal at `design/proposal.proposal.json`. `music/case.json` records the proposal path and title, and the command prints the proposal path.

Read `/tmp/design-eval/music/prompt.md` in the agent session you want to test. Have that agent design the brief using the copied skills. The agent connects to the previewer app over MCP as the server `android_design_preview` and checks its work with `check_proposal` and `get_tree`, then renders each screen twice with `render_view` (`light` and `desktop`, or `watch` and `watch-large` for Wear); the runner renders again headlessly. The output directory must be new; preparation never overwrites existing work. Omit `--cases` to prepare all eight cases: the phone cases music, finance, settings, trek, and recipes, and the Wear cases workout, player, and reminder.

## Surfaces

Each case in `evals.json` has a `surface`. A proposal uses one surface, and the app picks the views from its catalog; the runner reads them from `report.json` and does not hardcode them.

| Surface | Cases | Views | Checks | Rubric |
| --- | --- | --- | --- | --- |
| `phone` | music, finance, settings, trek, recipes | five, below | `C-valid`, `C-review`, `C-palette` | General items plus the case's own |
| `wear` | workout, player, reminder | `watch` (204x204) and `watch-large` (227x227), both round and dark | `C-valid`, `C-review` | `R-hierarchy`, `R-controls`, `R-text`, the five `R-wear-*` items, and the case's own |

Wear is the Wear OS catalog of the previewer app (`docs/design/wear-os-previewer.md`). `agent-prompt.md` is shared; `SURFACE_PROMPT` in `run.py` fills in where the proposal must work and how to read `check_proposal`'s layout summaries for each surface. `C-palette` is skipped for Wear on purpose: it guards the phone theme variant against neon surfaces, while a Wear proposal builds from black through Wear's own color mapping, and `R-wear-color` judges the rendered color directly. `rubric.md` says which items each surface judges. To check a branch's Wear rendering, point the runner at that build: `ANDROID_DESIGN_PREVIEW=/path/to/AndroidDesignPreview python3 run.py evaluate --out ...`.

There is no widget case: widgets still have no catalog in the previewer app, so they need one before they return.

After implementation, render and check the artifacts without launching an agent:

```bash
python3 run.py evaluate --out /tmp/design-eval --no-open
```

For each case this runs `<app> render <proposal> --out <case>/renders` (headless, no window; it passes `--design` when the agent wrote a `DESIGN.md`), reads `renders/report.json`, runs deterministic checks, and writes a report plus `<case>/judge-request.json`. No provider CLI is required for either command.

A phone proposal renders each screen at five views: `light` and `dark` at phone size 411x923; `foldable` (704x933), `tablet` (1280x800), and `desktop` (1920x1080) in light. `render` writes one PNG and one layout tree per screen and view, named `<screen>-<view>`, and `report.json` lists them. Each overlay component of a screen (sheet, dialog, menu, search view, tooltip, snackbar, picker; on Wear an AlertDialog or ConfirmationDialog) is one more render of that screen with it open, named `<screen>+<overlay>-<view>` and listed as the screen `<screen>+<overlay>`; its failures are only those the plain screen at that view does not already report (matched by rule and component id), so an overlay entry that fails names something the overlay caused. A scrolling Wear screen is drawn at the top of its list with a tree of the whole list, and also writes `<screen>-<view>-end.png` and `-end.tree.json`, the list scrolled to its end, listed as `endPng` and `endTree` on its entry. The runner takes the renders from that list, so a proposal with more screens, overlays, or long Wear lists has more images; each end-of-list PNG is its own image, labeled `screen/view (end of list)`.

The deterministic checks all read `report.json`:

| Check | Passes when |
| --- | --- |
| `C-valid` | `valid` is true. The detail carries the first validation errors. |
| `C-review` | Every render passes the app's finding rules, overlay renders included. The detail lists each failure as `screen/view: rule` (`screen+overlay/view: rule` for an overlay render). No renders is a failure. |
| `C-palette` | The merged theme's `variant` is not `expressive` or `vibrant`. Phone cases only. |

For visual assessment, give each `judge-request.json` to your chosen reviewer. They must inspect every listed screenshot (each is labeled `screen/view`) against its included rubric. The layout trees stay out of the request: the judge sees one PNG per screen and view, one per overlay and view, and the end of each scrolling Wear list. Prefer a separate session for a blind judgment; the current chat is useful for interactive checks but is not a controlled independent evaluation.

The reviewer writes `<case>/judge-ballot.json`:

```json
{
  "request_id": "copy the exact request_id from judge-request.json",
  "items": [
    {"id": "R-example", "pass": true, "reason": "Specific evidence visible in the screenshots"}
  ]
}
```

Include every requested rubric ID exactly once, with a JSON boolean and a nonempty reason. Import all case ballots with an attribution:

```bash
python3 run.py score --out /tmp/design-eval --judge-label 'reviewer and model or human' --no-open
```

Imported judgments are bound to the image, proposal, and `DESIGN.md` hashes. If any changes, evaluate and judge again. A preferred direction or successful build is not a visual pass. Missing judgments are explicitly **pending**, never successful. Exit codes are `0` for all passed, `1` for failures or execution errors, and `2` for incomplete visual assessment.

## Optional unattended automation

Select generation and judging independently; neither has an implicit provider:

```bash
python3 run.py run --out /tmp/design-eval-auto --generator codex --judge claude --no-open
# Judge an already implemented run without regenerating it:
python3 run.py evaluate --out /tmp/design-eval --judge codex --no-open
```

`--generator-model` and `--judge-model` set independent model overrides. Otherwise each installed CLI uses its configured model. Both `claude` and `codex` adapters normalize their replies behind `backends.py`; adding another launcher does not change the artifact contract. `--parallel` defaults to 3. `--judge-runs` must be a positive odd number; the default is one ballot. Cost and runtime depend on the chosen providers, models, and local build cache.

Automation requires the selected CLIs to be installed, authenticated, and permitted to work in the case directories, and the previewer app to be installed. The generator reaches the app over MCP, registered for that one run: Claude gets `--mcp-config <temporary json> --strict-mcp-config`, an allowance for `mcp__android_design_preview__*`, and an `Edit` allowance scoped to its case folder, since `claude -p` refuses file writes nobody allowed; Codex gets `mcp_servers.android_design_preview` through `-c` with `default_tools_approval_mode="approve"`, so `approval_policy = "never"` does not refuse the calls. Each generator run also gets its own socket, so it tests the app binary the runner names rather than an already-running installed instance: the runner makes a short temporary directory under `/tmp` and passes `ANDROID_DESIGN_PREVIEW_SOCKET=<dir>/s.sock` to the MCP server (the `env` object of Claude's MCP config, `mcp_servers.android_design_preview.env.*` through `-c` for Codex). That instance exits when its agent disconnects, and the runner removes the directory afterwards. Judges never see the app. The runner never bypasses permission prompts or sandboxes: configure your execution environment separately, or use the artifact-only path. Codex generation uses workspace-write and judging uses read-only; Claude judging exposes only its Read tool. These are tooling restrictions, not guarantees that global configuration or the host filesystem is invisible to the model.

CLI invocation sends the prompt and any accessed files/images to that CLI's configured service. Obtain the required execution permission before launching it. Preparation and artifact-only evaluation do not start these services.

## Calibration and baselines

Calibrate a judge after rubric changes or when changing the judging backend/model. The rubric must reproduce the known labels before trusting that configuration:

```bash
python3 calibrate.py run --judge codex --runs 3
# Or prepare provider-independent judging requests:
python3 calibrate.py prepare --out /tmp/design-calibration
# Grade each numbered request without reading the expected labels, then import its ballot:
python3 calibrate.py score --out /tmp/design-calibration
```

The Wear calibration images (`calibration/wear-*.png`) are renders of the proposals in `calibration/wear-src/`, screen `main` at both watch views. To change one, edit its proposal, render it with `<app> render <file> --out <dir>`, and copy `main-watch.png` and `main-watch-large.png` over `wear-<name>-watch.png` and `wear-<name>-watch-large.png`. A label may list both watch renders in `images`.

Calibration and eval artifacts use the same judging prompt, including the screen brief as task context rather than proof of implementation. Keep calibration labels out of the judging session. Manual calibration imports one ballot per image; automatic calibration defaults to three. Promotion is explicit:

```bash
python3 promote.py /tmp/design-eval
```

Agents vary across runs. A failure is a signal; inspect repeated results before attributing it to a skill change. Keep briefs, assets, skill snapshot, app version, render sizes, generator, and judge settings fixed for comparisons. Preparation snapshots both skills, case specifications, and rubric. Record the actual model separately when relying on a CLI's configured default.

## Trigger evals

`triggers/android-design.json` and `triggers/android-design-explore.json` hold realistic prompts with `should_trigger`, for checking that each skill's description reaches its own requests and stays quiet on near-misses. Run them after changing a skill's `description` (it calls `claude -p`, so it needs a signed-in Claude Code CLI and measures Claude Code only):

```bash
python3 evals/trigger_eval.py --eval-set evals/triggers/android-design.json
```

Each run happens in a temporary project holding this repo's two skills plus every other skill installed in `~/.claude/skills`. So installed copies of this repo's skills never compete with the repo copies, while their real neighbors (`adaptive`, `compose-animations`, and the rest) still do. The project sits in a neutral temp folder because Claude sees its working directory, and a path naming the skill skews the runs. Claude gets three read-only turns, so loading the skill after a look around still counts. `--description` tests a candidate without editing `SKILL.md`. Pass bar: every `should_trigger: true` query triggers in at least two of three runs, and every near-miss in at most one.

skill-creator's stock `run_eval.py` under-reports here: it registers a temporary copy beside the installed one, and it scores a miss whenever Claude calls any other tool first.

## Export check

The evals judge proposals rendered by the previewer app, so they cannot show whether an approved proposal survives export to Compose. Check that by hand after changing the app's exporter or the pinned material3 version, not on every skill change.

1. Take one approved proposal from an eval run that passed, with its `DESIGN.md` and `renders/<screen>-<view>.tree.json` files. To verify a `compose-mapping.md` row, take `evals/verify/verify.proposal.json` instead: it uses every catalog component.
2. Run `<app> export <proposal> --design <DESIGN.md> --screen <screen>` and copy the Kotlin it prints into a scratch copy of `evals/compose-starter/`. It is the Android sandbox the export lands in.
3. Render the sandbox from its folder with `rm -rf app/src/screenshotTestDebug/reference && ./gradlew :app:updateDebugScreenshotTest`, which writes the PNGs under `app/src/screenshotTestDebug/reference/` (this step needs JDK 17+ and the Android SDK; see the starter's README).
4. Compare the Compose render with the app's phone render of the same screen, and the exported layout with the proposal's tree. Differences the proposal determined (layout, hierarchy, color roles, type roles, component choice) are exporter bugs or mapping bugs to fix in `compose-mapping.md`. Small style choices the proposal left open are expected.
5. A proposal with `navigation.destinations` needs a few more steps. The starter has the adaptive dependencies a pane export imports. A still render cannot tap, and the export's initial back stack is `navigation.start`, so set `start` to a screen that is not a destination to render the hidden bar and rail (a copy of `verify.proposal.json` with `start` set to `fabs` works). The destination tap (`backStack.clear(); backStack.add(route)`) is not exercised by a render, so check it in a scratch copy of the starter, never in the committed one. In the copy: set `testOptions { unitTests.isIncludeAndroidResources = true }`; add `testImplementation` of `junit:junit:4.13.2`, `org.robolectric:robolectric:4.16` and `androidx.compose.ui:ui-test-junit4` (version from the BOM), and `debugImplementation` of `androidx.compose.ui:ui-test-manifest`; seed the exported `rememberNavBackStack(HomeRoute)` with a second route, for example `rememberNavBackStack(HomeRoute, DetailRoute)`, and expose the back stack from the copy's export with a top-level variable. Then a `RobolectricTestRunner` test with `@Config(sdk = [35], qualifiers = "w1280dp-h800dp-xhdpi")` (the pin to API 35 is needed because the starter's target is 37), `setContent { VerifyApp() }`, `onAllNodesWithText("Actions")[0].performClick()` and an assertion that the back stack equals `[ActionsRoute]`, run with `./gradlew :app:testDebugUnitTest`. Deleting `backStack.clear()` from the export makes it fail.

### Wear

Steps 1 to 4, with these changes. The proposal is `evals/verify/wear.verify.proposal.json` (every Wear catalog component; a Wear proposal needs no `--design` or `--screen`: `<app> export <proposal>` prints every screen). The sandbox is `evals/wear-starter/`, a scratch copy of which needs Android SDK platform 37.1; its README says where the export lands and how to add one preview per screen at 204 and 227 dp. The comparison is against `<app> render <proposal> --out <dir>` at the same two sizes. Expect two differences that are not bugs: the clock text, and lighter label weights in the sandbox (layoutlib has no Roboto Flex). The previewer merges a scrolling screen's whole list into its tree and writes the end of the list as `<screen>-<view>-end.png`, but the sandbox's screenshot still shows only the rows at the top of the list, so the verify proposal keeps each row group on a screen of its own; keep that when adding components. `render` writes one PNG per overlay, `<screen>+<overlayId>-<view>.png`; layoutlib screenshots do not show a `Dialog` window, so render a dialog by composing `AlertDialogContent` or `ConfirmationDialogContent` with the exported slots. A layoutlib render also freezes `ConfirmationDialog` on its first animation frame, so that row stays unverified.

## Verification and files

Run `python3 -m unittest discover evals` from the repository root to test adapters, artifact contracts, ballot validation, and binary resolution without model calls. Also run the [exploration workflow checks](exploration.md). Those separate rendering and isolation checks from agent routing behavior.

| Path | Responsibility |
| --- | --- |
| `evals.json`, `agent-prompt.md` | Briefs, assertion IDs, implementation prompt |
| `run.py`, `backends.py`, `preview_app.py` | Artifact lifecycle, optional CLI launchers, and the previewer app's location |
| `checks.py`, `judge.py`, `rubric.md` | Deterministic checks on `report.json` and provider-independent rubric judgments |
| `calibration/`, `baseline/` | Labeled renders and explicitly accepted baselines |
| `compose-starter/`, `wear-starter/` | The Android sandboxes the export check lands in (phone and Wear) |

Renders come from the previewer app's headless renderer, the same composables as its window. They do not verify motion or the export; check device behavior in the exported app when it matters. The app's layout checks come from its own tree, not from pixels, so contrast and truncation findings are exact; hierarchy and character are what the judge sees in the PNGs.
