# android-design evals

Briefs, rendering, code checks, and rubric judgments are independent of any agent provider. Run the evals after changing the skill, not on a schedule. The current chat, a human, or an optional CLI adapter can supply implementations and judgments.

## Use the current chat or another agent

From this directory:

```bash
python3 run.py prepare --out /tmp/design-eval --cases music
```

Read `/tmp/design-eval/music/prompt.md` in the agent session you want to test. Have that agent implement the brief in `music/project/` using the copied skill. The output directory must be new; preparation never overwrites existing work. Omit `--cases` to prepare all four active phone cases. Wear and widget cases remain parked unless explicitly named.

After implementation, render and check the artifacts without launching an agent:

```bash
python3 run.py evaluate --out /tmp/design-eval --no-open
```

This restores the fixed harness, re-renders, runs deterministic checks, and writes a report plus `<case>/judge-request.json`. No provider CLI is required for either command. Rendering requires the Android SDK and JDK; no emulator is needed. The initial dependency resolution requires network access.

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

Imported judgments are bound to image and project-file hashes. If either changes, evaluate and judge again. A preferred direction or successful build is not a visual pass. Missing judgments are explicitly **pending**, never successful. Exit codes are `0` for all passed, `1` for failures or execution errors, and `2` for incomplete visual assessment.

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

Agents vary across runs. A failure is a signal; inspect repeated results before attributing it to a skill change. Keep briefs, assets, skill snapshot, rendering configuration, generator, and judge settings fixed for comparisons. Preparation snapshots the skill, case specifications, and rubric. Record the actual model separately when relying on a CLI's configured default.

## Verification and files

Run `python3 -m unittest discover evals` from the repository root to test adapters, artifact contracts, ballot validation, and preference persistence without model calls. Also run the [exploration workflow checks](exploration.md) for the isolated starter. Those separate rendering/isolation checks from agent routing behavior.

| Path | Responsibility |
| --- | --- |
| `evals.json`, `agent-prompt.md` | Briefs, assertion IDs, implementation prompt |
| `run.py`, `backends.py` | Artifact lifecycle and optional CLI launchers |
| `checks.py`, `judge.py`, `rubric.md` | Deterministic checks and provider-independent rubric judgments |
| `calibration/`, `baseline/` | Labeled renders and explicitly accepted baselines |
| `template/`, `overlays/`, `preference/` | Fixed rendering harness and owner preference rounds |

Renders are offscreen: Compose Preview Screenshot Testing for phone/Wear and Robolectric for Glance. They do not verify motion, and widget corner clipping is not drawn. Check device behavior on an emulator when it matters.
