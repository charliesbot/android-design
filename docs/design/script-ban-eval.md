# Script ban eval baseline

## TL;DR

This records the baseline run that was meant to be the control arm of an A/B on the script ban. Only the baseline ran, so the ban still has no measured reason. It stays.

## The ban and the planned A/B

`skills/android-design-explore/references/proposal-format.md` (line 94) tells the agent to "never generate it with a script". The claude generator in `evals/backends.py` (lines 41-44) enforces the same thing: it is allowed only the previewer MCP tools and Edit inside its case folder, so it has no shell. The A/B would have compared that against a generator allowed `Bash(python3:*)`. Claude Code's auto-mode permission classifier denied granting the spawned generator `Bash(python3:*)` ("Create Unsafe Agents"), so the treatment arm never ran.

## Baseline

The run used the build at ff37a7f (after #103 to #107) with `python3 evals/run.py run`, the claude generator and the claude judge (claude CLI 2.1.322, default model; the envelope records model=null). It is the skill-eval run that #103 to #107 deferred to. #108 changed the skill text (`proposal-format.md`) and added the `arrangement-no-space` finding after the baseline ran, so this baseline does not cover #108.

| Case | Surface | Code checks | Rubric | Cost ($) | Turns |
|---|---|---|---|---|---|
| music | phone | 3/3 | 11/11 | 1.57 | 29 |
| finance | phone | 3/3 | 9/11 (fails R-idea, R-finance-categories) | 1.74 | 36 |
| settings | phone | 3/3 | 11/11 | 1.33 | 22 |
| trek | phone | 3/3 | 10/11 (fails R-text) | 2.01 | 29 |
| recipes | phone | 3/3 | 11/11 | 1.54 | 22 |
| workout | wear | 2/2 | 9/9 | 1.01 | 25 |
| player | wear | 2/2 | 9/9 | 1.09 | 27 |
| reminder | wear | 2/2 | 9/9 | 0.91 | 17 |

Six of 8 cases pass and every code check passes. Total cost is $11.19 over 207 turns. The runner's `minutes` field averaged 0.575 per case, but it measures only render and judge. The cases ran three at a time (`--parallel` defaults to 3). Each case's generation took roughly 2 to 4 minutes, and the whole run took about 9 minutes of wall time (20:46 to 20:55), from the run's start and end timestamps.

## Failures

Finance R-idea: apart from the condensed hero number and wavy progress bar, the layout is a standard card grid and list, and two screens are empty placeholders. Finance R-finance-categories: all four categories share one blue-gray, so they cannot be told apart by hue.

Trek R-text: on tablet and desktop the "Route" header and its "Up to 5,364 m" label touch. The root cause is #103 working as intended: the unweighted itinerary pane Column wraps its content, as Compose does, so its spaceBetween header Row has no free space. #108 now reports this as the info finding `arrangement-no-space` in `check_proposal`.

## Judge noise

`python3 evals/calibrate.py` exited 1 with calibration agreeing on 30 of 31 items. The one miss was R-surfaces on the finance-v3-neon image (1 of 3 ballots), a different image and a different item from the baseline's failures. An earlier calibration run passed that same item with 2 of 3 ballots and exited 0. Eval runs use one judge ballot per item (`--judge-runs` defaults to 1) while calibration uses three, so single-ballot results carry some noise. R-finance-categories agreed on both of its calibration images, and R-idea has no calibration image. `python3 -m unittest discover evals` exited 0 (76 tests).

## Harness follow-ups

The generator has no shell, so an A/B on scripts needs an opt-in tool flag. No transcripts are kept. The envelope has no model or token fields. `minutes` excludes generation time.
