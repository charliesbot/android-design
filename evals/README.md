# android-design evals

Checks that agents following the skill still produce screens that meet its rules. Run it after changing the skill, not on a schedule.

## Run

```bash
cd evals
python3 calibrate.py          # once per rubric change: the judge must reproduce every label
python3 run.py                # all cases; or --cases music,finance
python3 promote.py ~/.cache/android-design-evals/<timestamp>   # accept a run as the new baseline
```

A full run is about 6 headless agents plus the judge calls, 10 to 20 minutes with `--parallel 3`. It needs the Android SDK (for Gradle) and the `claude` CLI, but no emulator: screens render offscreen.

## How a case runs

1. `template/` (a pinned `:app` + `:wear` project) and the case's `overlays/<surface>/` are copied to the run folder, plus any `assets/`.
2. A headless agent (`claude -p` with permissions bypassed, so it can run Gradle unattended) gets `agent-prompt.md` with the brief. It works only on copies in the run folder: the template project and `skills/android-design`, so it never sees the repo, the rubric, or the calibration labels. It renders its own work with `./eval-render.sh`.
3. The runner restores the fixed render harness (in case the agent edited it), re-renders, then scores:
   - `checks.py`: deterministic code checks (`C-*` assertions).
   - `judge.py`: a model grades the renders against `rubric.md` (`R-*` assertions).
4. `report.py` writes `report.html` with scores, failures, and renders beside `baseline/`, plus `summary.md` with the scores table and every failure. The runner prints the summary and opens the report (pass `--no-open` to skip it).

Agents design something different on every run, so one failed item on one run is a signal, not a regression. Treat it as a regression only when the same item fails again or first fails right after a skill change, and compare against earlier runs before changing the skill to fix it.

## Files

| File | Job |
| --- | --- |
| `evals.json` | The cases: brief, surface, input files, and assertions (`code` or `rubric`) |
| `agent-prompt.md` | The prompt template every agent gets |
| `rubric.md` | What the judge checks, item by item |
| `calibration/` | Past renders with known verdicts; `calibrate.py` must reproduce them |
| `baseline/` | The last accepted run's primary renders, compared in each report |
| `template/`, `overlays/` | The project each agent starts from and the fixed render harness per surface |

Renders are offscreen (Compose Preview Screenshot Testing for phone and Wear, Robolectric for Glance widgets). They include system bars and dynamic color, but not motion, and widget corner clipping is not drawn. Check edge-to-edge behavior on an emulator when it matters.

When a new regression shows up, add a check or rubric item for it and, if it is visual, a labeled image in `calibration/`.
