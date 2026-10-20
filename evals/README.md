# android-design evals

Briefs, rendering, code checks, and rubric judgments are independent of any agent provider. Run the evals after changing the skill, not on a schedule. The current chat, a human, or an optional CLI adapter can supply implementations and judgments.

An eval case is the skill's current output: one HTML proposal built on the proposal runtime (see [exploration workflow checks](exploration.md)). Requirements: Node.js 22+ and Google Chrome (or set `CHROME_PATH`). No Android SDK, JDK, or Gradle.

## Use the current chat or another agent

From this directory:

```bash
python3 run.py prepare --out /tmp/design-eval --cases music
```

Preparation makes `music/` a fresh Git repository fixture (one empty commit) with a proposal folder `design-explorations/proposal/` (a copy of `assets/proposal-starter/` plus the brief's assets), and a snapshot of both skills (`skills/android-design` and `skills/android-design-explore`). `music/case.json` records the proposal folder and title, and the command prints the proposal folder.

Read `/tmp/design-eval/music/prompt.md` in the agent session you want to test. Have that agent design the brief as the proposal in `design-explorations/proposal/` using the copied skills. It renders with `cli.mjs render` to check its work; the runner renders again. The output directory must be new; preparation never overwrites existing work. Omit `--cases` to prepare all four cases (music, finance, settings, trek).

The Wear OS and widget cases were removed: they have no HTML harness. They need a new harness before they return.

After implementation, render and check the artifacts without launching an agent:

```bash
python3 run.py evaluate --out /tmp/design-eval --no-open
```

For each case this runs `gallery/cli.mjs render` on the proposal folder (headless, no server), copies its screenshots (the five renders below) and `checks.json`, runs deterministic checks, and writes a report plus `<case>/judge-request.json`. No provider CLI is required for either command.

The five renders: `light` and `dark` at phone size 411x923; `foldable` (933x704), `tablet` (1280x800), and `desktop` (1920x1080) in light.

The deterministic checks are `C-lint` (the render succeeded, so the lint passed), `C-review` (every render's `runtimeChecks` result passes; an incomplete check counts as a failure, reported as unverified), and `C-palette` (the proposal's `<m3e-theme>` variant is not `expressive` or `vibrant`).

For visual assessment, give each `judge-request.json` to your chosen reviewer. They must inspect every listed screenshot against its included rubric. Prefer a separate session for a blind judgment; the current chat is useful for interactive checks but is not a controlled independent evaluation.

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

Imported judgments are bound to image and proposal-file hashes. If either changes, evaluate and judge again. A preferred direction or successful build is not a visual pass. Missing judgments are explicitly **pending**, never successful. Exit codes are `0` for all passed, `1` for failures or execution errors, and `2` for incomplete visual assessment.

## Optional unattended automation

Select generation and judging independently; neither has an implicit provider:

```bash
python3 run.py run --out /tmp/design-eval-auto --generator codex --judge claude --no-open
# Judge an already implemented run without regenerating it:
python3 run.py evaluate --out /tmp/design-eval --judge codex --no-open
```

`--generator-model` and `--judge-model` set independent model overrides. Otherwise each installed CLI uses its configured model. Both `claude` and `codex` adapters normalize their replies behind `backends.py`; adding another launcher does not change the artifact contract. `--parallel` defaults to 3. `--judge-runs` must be a positive odd number; the default is one ballot. Cost and runtime depend on the chosen providers, models, and local build cache.

Automation requires the selected CLIs to be installed, authenticated, and permitted to work in the case directories. The runner never bypasses permission prompts or sandboxes: configure your execution environment separately, or use the artifact-only path. Codex generation uses workspace-write and judging uses read-only; Claude judging exposes only its Read tool. These are tooling restrictions, not guarantees that global configuration or the host filesystem is invisible to the model.

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

Agents vary across runs. A failure is a signal; inspect repeated results before attributing it to a skill change. Keep briefs, assets, skill snapshot, render sizes, generator, and judge settings fixed for comparisons. Preparation snapshots both skills, case specifications, and rubric. Record the actual model separately when relying on a CLI's configured default.

## Trigger evals

`triggers/android-design.json` and `triggers/android-design-explore.json` hold realistic prompts with `should_trigger`, for checking that each skill's description reaches its own requests and stays quiet on near-misses. Run them after changing a skill's `description`, with the skill-creator skill's runner (it calls `claude -p`, so it needs a signed-in Claude Code CLI and measures Claude Code only):

```bash
python3 <skill-creator>/scripts/run_eval.py --eval-set evals/triggers/android-design.json --skill-path skills/android-design --runs-per-query 3
```

The runner counts a trigger only when Claude reads its temporary copy of the skill, so uninstall the same-named skill first (or count the installed one too); with both present, every query reads as a miss. Pass bar: every `should_trigger: true` query triggers in at least two of three runs, and every near-miss in at most one.

## Compose port check

The evals judge HTML proposals, so they cannot show whether an approved proposal survives the port to Compose. Check that by hand after changing `skills/android-design-explore/references/compose-mapping.md` or the pinned material3 version, not on every skill change.

1. Take one approved proposal from an eval run that passed, with its renders.
2. Copy `evals/compose-starter/` to a scratch folder. It is the Android sandbox the port lands in.
3. Have an agent that has not seen the proposal's renders port it to Compose using only the proposal source and `compose-mapping.md`, then render it by running `node render.mjs app` in the scratch folder.
4. Compare the Compose render with the HTML phone render. Differences the HTML determined (layout, hierarchy, color roles, type roles, component choice) are mapping bugs to fix in `compose-mapping.md`. Small style choices the HTML left open are expected.
5. To mark a `compose-mapping.md` row `verified`, render its component from the verify page with `node skills/android-design-explore/gallery/cli.mjs render evals/verify` and compare the Compose render of the same component with the section in `evals/verify/renders/light.png` or `view.html`. The page lists every registered component with its variants; its checks fail by design (many FABs, small targets), so only the render matters. Delete `evals/verify/renders/` afterwards (it is gitignored).

## Verification and files

Run `python3 -m unittest discover evals` from the repository root to test adapters, artifact contracts, ballot validation, and preference persistence without model calls. Also run the [exploration workflow checks](exploration.md). Those separate rendering/isolation checks from agent routing behavior. `node --test tests/evals/*.test.mjs` covers the runtime checks and renderer.

| Path | Responsibility |
| --- | --- |
| `evals.json`, `agent-prompt.md` | Briefs, assertion IDs, implementation prompt |
| `run.py`, `backends.py` | Artifact lifecycle and optional CLI launchers |
| `checks.py`, `judge.py`, `rubric.md` | Deterministic checks and provider-independent rubric judgments |
| `calibration/`, `baseline/` | Labeled renders and explicitly accepted baselines |
| `verify/` | One section per `compose-mapping.md` component with its variants, rendered for the Compose port check |
| `skills/android-design-explore/gallery/render.mjs` | Headless Chrome renders of a proposal plus its in-page `runtimeChecks` (what `cli.mjs render` runs) |
| `preference/` | Owner preference rounds |

Renders come from headless Chrome against the proposal runtime. They do not verify motion or the Compose port; check device behavior in the ported app when it matters. A proposal's `runtimeChecks` can report `complete: false` when it cannot resolve a check (for example a contrast backdrop); that counts as unverified, not as a pass.
