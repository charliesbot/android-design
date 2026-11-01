# android-design evals

Briefs, rendering, code checks, and rubric judgments are independent of any agent provider. Run the evals after changing the skill, not on a schedule. The current chat, a human, or an optional CLI adapter can supply implementations and judgments.

An eval case is the skill's current output: one proposal for the previewer app, `design/proposal.proposal.json` plus a root `DESIGN.md` (see [exploration workflow checks](exploration.md)). Requirements: macOS and the previewer app installed (`./gradlew :app:installApp` from `app/`). No Node.js, Chrome, or Android SDK. The runner finds the app through `app_binary()` in `preview_app.py`: `$ANDROID_DESIGN_PREVIEW` (path to the executable), then `~/Applications/AndroidDesignPreview.app`, then the repo's build output, and stops with the install command when none exists.

## Use the current chat or another agent

From this directory:

```bash
python3 run.py prepare --out /tmp/design-eval --cases music
```

Preparation makes `music/` a fresh Git repository fixture (one empty commit) with `design/` holding the brief's assets in `design/assets/`, and a snapshot of both skills (`skills/android-design` and `skills/android-design-explore`). There is no starter and no `DESIGN.md`: the agent writes `DESIGN.md` at the fixture root and the proposal at `design/proposal.proposal.json`. `music/case.json` records the proposal path and title, and the command prints the proposal path.

Read `/tmp/design-eval/music/prompt.md` in the agent session you want to test. Have that agent design the brief using the copied skills. The agent connects to the previewer app over MCP as the server `android_design_preview` and checks its work with `check_proposal` and `get_tree`, not screenshots; the runner renders again headlessly. The output directory must be new; preparation never overwrites existing work. Omit `--cases` to prepare all four cases (music, finance, settings, trek).

The Wear OS and widget cases were removed: the previewer app has no Wear or widget catalog. They need one before they return.

After implementation, render and check the artifacts without launching an agent:

```bash
python3 run.py evaluate --out /tmp/design-eval --no-open
```

For each case this runs `<app> render <proposal> --out <case>/renders` (headless, no window; it passes `--design` when the agent wrote a `DESIGN.md`), reads `renders/report.json`, runs deterministic checks, and writes a report plus `<case>/judge-request.json`. No provider CLI is required for either command.

Each screen of the proposal renders at five views: `light` and `dark` at phone size 411x923; `foldable` (933x704), `tablet` (1280x800), and `desktop` (1920x1080) in light. `render` writes one PNG and one layout tree per screen and view, named `<screen>-<view>`, and `report.json` lists them. The runner takes the renders from that list, so a proposal with more screens has more images.

The deterministic checks all read `report.json`:

| Check | Passes when |
| --- | --- |
| `C-valid` | `valid` is true. The detail carries the first validation errors. |
| `C-review` | Every render passes the app's finding rules. The detail lists each failure as `screen/view: rule`. No renders is a failure. |
| `C-palette` | The merged theme's `variant` is not `expressive` or `vibrant`. |

For visual assessment, give each `judge-request.json` to your chosen reviewer. They must inspect every listed screenshot (each is labeled `screen/view`) against its included rubric. The layout trees stay out of the request: the judge sees one PNG per screen and view. Prefer a separate session for a blind judgment; the current chat is useful for interactive checks but is not a controlled independent evaluation.

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

Automation requires the selected CLIs to be installed, authenticated, and permitted to work in the case directories, and the previewer app to be installed. The generator reaches the app over MCP, registered for that one run: Claude gets `--mcp-config <temporary json> --strict-mcp-config` and an allowance for `mcp__android_design_preview__*`; Codex gets `mcp_servers.android_design_preview` through `-c` with `default_tools_approval_mode="approve"`, so `approval_policy = "never"` does not refuse the calls. Judges never see the app. The runner never bypasses permission prompts or sandboxes: configure your execution environment separately, or use the artifact-only path. Codex generation uses workspace-write and judging uses read-only; Claude judging exposes only its Read tool. These are tooling restrictions, not guarantees that global configuration or the host filesystem is invisible to the model.

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

Calibration and eval artifacts use the same judging prompt, including the screen brief as task context rather than proof of implementation. Keep calibration labels out of the judging session. Manual calibration imports one ballot per image; automatic calibration defaults to three. Promotion is explicit:

```bash
python3 promote.py /tmp/design-eval
```

Agents vary across runs. A failure is a signal; inspect repeated results before attributing it to a skill change. Keep briefs, assets, skill snapshot, app version, render sizes, generator, and judge settings fixed for comparisons. Preparation snapshots both skills, case specifications, and rubric. Record the actual model separately when relying on a CLI's configured default.

## Trigger evals

`triggers/android-design.json` and `triggers/android-design-explore.json` hold realistic prompts with `should_trigger`, for checking that each skill's description reaches its own requests and stays quiet on near-misses. Run them after changing a skill's `description`, with the skill-creator skill's runner (it calls `claude -p`, so it needs a signed-in Claude Code CLI and measures Claude Code only):

```bash
python3 <skill-creator>/scripts/run_eval.py --eval-set evals/triggers/android-design.json --skill-path skills/android-design --runs-per-query 3
```

The runner counts a trigger only when Claude reads its temporary copy of the skill, so uninstall the same-named skill first (or count the installed one too); with both present, every query reads as a miss. Pass bar: every `should_trigger: true` query triggers in at least two of three runs, and every near-miss in at most one.

## Export check

The evals judge proposals rendered by the previewer app, so they cannot show whether an approved proposal survives export to Compose. Check that by hand after changing the app's exporter or the pinned material3 version, not on every skill change.

1. Take one approved proposal from an eval run that passed, with its `DESIGN.md` and `renders/<screen>-<view>.tree.json` files. To verify a `compose-mapping.md` row, take `evals/verify/verify.proposal.json` instead: it uses every catalog component.
2. Run `<app> export <proposal> --design <DESIGN.md> --screen <screen>` and copy the Kotlin it prints into a scratch copy of `evals/compose-starter/`. It is the Android sandbox the export lands in.
3. Render the sandbox with `node render.mjs app` (this step needs Node.js 22+, JDK 17+, and the Android SDK; see the starter's README).
4. Compare the Compose render with the app's phone render of the same screen, and the exported layout with the proposal's tree. Differences the proposal determined (layout, hierarchy, color roles, type roles, component choice) are exporter bugs or mapping bugs to fix in `compose-mapping.md`. Small style choices the proposal left open are expected.

## Verification and files

Run `python3 -m unittest discover evals` from the repository root to test adapters, artifact contracts, ballot validation, binary resolution, and preference persistence without model calls. Also run the [exploration workflow checks](exploration.md). Those separate rendering and isolation checks from agent routing behavior.

| Path | Responsibility |
| --- | --- |
| `evals.json`, `agent-prompt.md` | Briefs, assertion IDs, implementation prompt |
| `run.py`, `backends.py`, `preview_app.py` | Artifact lifecycle, optional CLI launchers, and the previewer app's location |
| `checks.py`, `judge.py`, `rubric.md` | Deterministic checks on `report.json` and provider-independent rubric judgments |
| `calibration/`, `baseline/` | Labeled renders and explicitly accepted baselines |
| `compose-starter/` | The Android sandbox the export check lands in |
| `preference/` | Owner preference rounds |

Renders come from the previewer app's headless renderer, the same composables as its window. They do not verify motion or the export; check device behavior in the exported app when it matters. The app's layout checks come from its own tree, not from pixels, so contrast and truncation findings are exact; hierarchy and character are what the judge sees in the PNGs.
