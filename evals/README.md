# android-design evals

Briefs, rendering, code checks, and rubric judgments are independent of any agent provider. Run the evals after changing the skill, not on a schedule. The current chat, a human, or an optional CLI adapter can supply implementations and judgments.

An eval case is the skill's current output: one Compose exploration (`design/proposal.explore.kt` on phone or `design/proposal-wear.explore.kt` on Wear), with no `DESIGN.md` because no direction is approved yet (see [exploration workflow checks](exploration.md)). Requirements: Java 17+ and Android SDK (`cd app && ./gradlew :host:exportBootstrap` exports `server.cp`, `client.cp`, and `robolectric.cp`, or let `plugin/skills/android-design-explore/scripts/android-design-preview` bootstrap on first run). The runner locates the CLI bootstrap script and Gradle bootstrap paths through `app_binary()`, `preview_src()`, `bootstrap_dir()`, and `robolectric_cp()` in `preview_app.py` (`$ANDROID_DESIGN_PREVIEW`, `$ANDROID_DESIGN_PREVIEW_SRC`, and `$ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR`).

## Use the current chat or another agent

From this directory:

```bash
python3 run.py prepare --out /tmp/design-eval --cases music
```

Preparation makes `music/` a fresh Git repository fixture (one empty commit) with `design/` holding the brief's assets in `design/assets/`, and a snapshot of both skills (copied from `plugin/skills/` into `skills/android-design` and `skills/android-design-explore`). There is no starter and no `DESIGN.md`: the agent writes only the exploration at `design/proposal.explore.kt`, keeping its tokens on the board. `music/case.json` records the exploration path and title, and the command prints the target path.

Read `/tmp/design-eval/music/prompt.md` in the agent session you want to test. Have that agent design the brief using the copied skills. The agent verifies its work through the `android-design-preview` CLI (`check` and `tree`); the runner compiles and renders again headlessly. The output directory must be new; preparation never overwrites existing work. Omit `--cases` to prepare all eight cases: the phone cases music, finance, settings, trek, and recipes, and the Wear cases workout, player, and reminder.

## Surfaces

Each case in `evals.json` has a `surface`. A `.explore.kt` file declares `designBoard` (phone) or `wearBoard` (Wear), and the runner reads rendered views from `report.json` rather than hardcoding them.

| Surface | Cases | Views | Checks | Rubric |
| --- | --- | --- | --- | --- |
| `phone` | music, finance, settings, trek, recipes | five, below | `C-valid`, `C-review`, `C-palette` | General items, the four code items, and the case's own |
| `wear` | workout, player, reminder | `watch` (204x204) and `watch-large` (227x227), both round and dark | `C-valid`, `C-review` | `R-hierarchy`, `R-controls`, `R-text`, the five `R-wear-*` items, and the case's own |

Wear renders natively with `androidx.wear.compose.material3` inside the Android `:host` / Robolectric runtime (`docs/design/visual-design-exploration-v6.md`). `agent-prompt.md` is shared; `SURFACE_PROMPT` in `run.py` fills in where the exploration must work and how to interpret `android-design-preview check` layout summaries for each surface. `C-palette` is skipped for Wear on purpose: it guards the phone theme variant against neon surfaces, while a Wear board builds from black through Wear's own color mapping, and `R-wear-color` judges the rendered color directly. `rubric.md` says which items each surface judges. To point eval runs at a custom checkout or bootstrap directory, set `ANDROID_DESIGN_PREVIEW_SRC=/path/to/repo/app` or `ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR=/path/to/bootstrap`.

There is no widget case: widgets still have no preview catalog or eval harness, so they remain out of scope.

After implementation, compile, render, and check the artifacts without launching an agent:

```bash
python3 run.py evaluate --out /tmp/design-eval --no-open
```

For each case this runs `plugin/skills/android-design-explore/scripts/android-design-preview render <explore.kt> --out <case>/renders` (passing `--design` when `DESIGN.md` exists), reads `renders/report.json`, runs deterministic checks, and writes a report plus `<case>/judge-request.json`. No provider CLI is required for either command.

A phone exploration renders each screen at five views: `light` and `dark` at phone size 411x923; `foldable` (704x933), `tablet` (1280x800), and `desktop` (1920x1080) in light. `render` writes one PNG and one layout tree per screen and view, named `<screen>-<view>`, and `report.json` lists them. Each `overlay(...)` declaration of a screen is one more render with that overlay open, named `<screen>+<overlay>-<view>` and listed as `<screen>+<overlay>`; its failures are only those the plain screen at that view does not already report (matched by rule and component id), so an overlay entry that fails names something the overlay caused. A scrolling Wear screen is drawn at the top of its list with a tree of the whole list, and also writes `<screen>-<view>-end.png` and `-end.tree.json`, the list scrolled to its end, listed as `endPng` and `endTree` on its entry. The runner takes the renders from that list, so an exploration with more screens, overlays, or long Wear lists has more images; each end-of-list PNG is its own image, labeled `screen/view (end of list)`.

The deterministic checks all read `report.json`:

| Check | Passes when |
| --- | --- |
| `C-valid` | `valid` is true. The detail carries the first lint, compile, or `DESIGN.md` validation errors. |
| `C-review` | Every render passes the worker's finding rules, overlay renders included. The detail lists each failure as `screen/view: rule` (`screen+overlay/view: rule` for an overlay render). No renders is a failure. |
| `C-palette` | The merged theme's `variant` is not `expressive` or `vibrant`. Phone cases only. |

For visual assessment, give each `judge-request.json` to your chosen reviewer. They must inspect every listed screenshot (each is labeled `screen/view`) against its included rubric. The layout trees stay out of the request: the judge sees one PNG per screen and view, one per overlay and view, and the end of each scrolling Wear list. A phone request also carries the proposal's `.explore.kt` source (a request carries it only when it asks for a code item, so Wear requests do not), which the rubric's code items (`R-semantics`, `R-fields`, `R-visibility`, `R-blur`) are judged from, because semantics, text field state, and how content is hidden never show in a screenshot. Prefer a separate session for a blind judgment; the current chat is useful for interactive checks but is not a controlled independent evaluation.

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

Imported judgments are bound to the image, `.explore.kt`, and `DESIGN.md` hashes. If any changes, evaluate and judge again. A preferred direction or successful compile is not a visual pass. Missing judgments are explicitly **pending**, never successful. Exit codes are `0` for all passed, `1` for failures or execution errors, and `2` for incomplete visual assessment.

## Optional unattended automation

Select generation and judging independently; neither has an implicit provider:

```bash
python3 run.py run --out /tmp/design-eval-auto --generator agy --judge agy --no-open
# Judge an already implemented run without regenerating it:
python3 run.py evaluate --out /tmp/design-eval --judge agy --no-open
```

`--generator-model` and `--judge-model` set independent model overrides. Otherwise each installed CLI uses its configured model. `agy`, `claude`, and `codex` adapters normalize their replies behind `backends.py`, writing `<case>/generator-metrics.json` (wall-clock seconds, turns, tokens, model, and check/retry counts) so generation time is tracked separately from render plus judging time (`eval_minutes`); adding another launcher does not change the artifact contract. `--parallel` defaults to 3. `--judge-runs` must be a positive odd number; the default is one ballot. Cost and runtime depend on the chosen providers, models, and local build cache.

Automation requires the selected CLIs to be installed, authenticated, and permitted to work in the case directories, plus `:host:exportBootstrap` outputs available. Each generator receives the explore skill's `scripts/` on `PATH` with `ANDROID_DESIGN_NO_OPEN=1` and the v6 bootstrap environment (`ANDROID_DESIGN_PREVIEW_SRC`, `ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR`, `ANDROID_DESIGN_PREVIEW_SERVER_CP`, `ANDROID_DESIGN_PREVIEW_ROBOLECTRIC_CP`, `ANDROID_DESIGN_PREVIEW_BACKEND`): Claude gets `--allowedTools 'Bash(android-design-preview:*)' 'Edit(/<case>/**)' 'Write(/<case>/**)' 'Read' --add-dir ~/.cache/android-design-preview`; Antigravity (`agy`) allows `read_file(*)`, `write_file(<case>)`, and `command(*)` (denying `mcp(*)`, `read_url(*)`, `execute_url(*)`) and `backends.py` audits its transcript so any shell command other than a single `android-design-preview` invocation fails the case; Codex uses `workspace-write` with `sandbox_workspace_write.network_access=true` and `~/.cache/android-design-preview` plus `~/.gradle` in `writable_roots`, and `backends.py` audits its transcript to allow only `android-design-preview` plus read-only file commands (`cat`, `head`, `tail`, `nl`, `ls`, `rg`, `grep`, `wc`, and `sed` without `-i`). Judges never see the preview CLI or shell access. Antigravity judging allows `read_file` with `write_file`, `command`, `mcp`, and URL tools denied; Codex judging uses read-only; Claude judging exposes only its Read tool.

CLI invocation sends the prompt and any accessed files/images to that CLI's configured service. Obtain the required execution permission before launching it. Preparation and artifact-only evaluation do not start these services.

## Calibration and baselines

Calibrate a judge after rubric changes or when changing the judging backend/model. The rubric must reproduce the known labels before trusting that configuration:

```bash
python3 calibrate.py run --judge agy --runs 3
# Or prepare provider-independent judging requests:
python3 calibrate.py prepare --out /tmp/design-calibration
# Grade each numbered request without reading the expected labels, then import its ballot:
python3 calibrate.py score --out /tmp/design-calibration
```

The Wear calibration images (`calibration/wear-*.png`) are frozen renders matched to `calibration/labels.json`. The `calibration/code-*` labels each pair a light render with the `.explore.kt` it came from (`source` in the label), so the code items are calibrated against real code.

Calibration and eval artifacts use the same judging prompt, including the screen brief as task context rather than proof of implementation. Keep calibration labels out of the judging session. Manual calibration imports one ballot per image; automatic calibration defaults to three. Promotion is explicit:

```bash
python3 promote.py /tmp/design-eval
```

Agents vary across runs. A failure is a signal; inspect repeated results before attributing it to a skill change. Keep briefs, assets, skill snapshot, pinned dependency versions, render sizes, generator, and judge settings fixed for comparisons. Preparation snapshots both skills, case specifications, and rubric. Record the actual model separately when relying on a CLI's configured default.

## Trigger evals

`triggers/android-design.json` and `triggers/android-design-explore.json` hold realistic prompts with `should_trigger`, for checking that each skill's description reaches its own requests and stays quiet on near-misses. Run them after changing a skill's `description` (it calls `claude -p`, so it needs a signed-in Claude Code CLI and measures Claude Code only):

```bash
python3 evals/trigger_eval.py --eval-set evals/triggers/android-design.json
```

Each run happens in a temporary project holding this repo's two skills plus every other skill installed in `~/.claude/skills`. So installed copies of this repo's skills never compete with the repo copies, while their real neighbors (`adaptive`, `compose-animations`, and the rest) still do. The project sits in a neutral temp folder because Claude sees its working directory, and a path naming the skill skews the runs. Claude gets three read-only turns, so loading the skill after a look around still counts. `--description` tests a candidate without editing `SKILL.md`. Pass bar: every `should_trigger: true` query triggers in at least two of three runs, and every near-miss in at most one.

skill-creator's stock `run_eval.py` under-reports here: it registers a temporary copy beside the installed one, and it scores a miss whenever Claude calls any other tool first.

## Verification and files

Run `python3 -m unittest discover evals` from the repository root to test adapters, artifact contracts, ballot validation, and bootstrap/worker resolution without model calls. Also run the [exploration workflow checks](exploration.md). Those separate rendering and isolation checks from agent routing behavior.

| Path | Responsibility |
| --- | --- |
| `evals.json`, `agent-prompt.md` | Briefs, assertion IDs, implementation prompt |
| `run.py`, `backends.py`, `preview_app.py` | Artifact lifecycle, optional CLI launchers, preview CLI and Robolectric worker resolution |
| `checks.py`, `judge.py`, `rubric.md` | Deterministic checks on `report.json` and provider-independent rubric judgments |
| `calibration/`, `baseline/` | Labeled renders and explicitly accepted baselines |

Renders come from the persistent headless Robolectric worker (`RobolectricWorker`), running the same composables used in interactive Android Host APK sessions. Contrast checks sample pixels directly from the rendered bitmap, and hierarchy/layout/truncation checks inspect Compose slot tables and semantics nodes.
