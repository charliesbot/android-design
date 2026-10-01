# Preference rounds

The scored evals check the skill's rules. Preference rounds check taste: the owner picks the best and worst of several unsteered variants per brief, and the differences between picked and rejected variants become skill edits.

## Run a round

```bash
cd evals
python3 preference/generate.py                  # 10 briefs x 3 variants, about 40 minutes and $55 at --parallel 4
python3 preference/serve.py ~/.cache/android-design-evals/preference-<timestamp>
```

`serve.py` opens a local page on 127.0.0.1. Pick a best and a worst per brief (keys 1 to 3, Shift for worst, N for none good, L/D/F/W to switch renders), tag the reasons, and add a note when a reason chip is not enough. Every change saves to `selections.json` in the round folder.

## Turn picks into skill changes

1. For each brief, compare the picked and rejected variants' source (`<case>/v<n>/project/`) and their final messages (`variant.json`), which name the skill sections that drove each decision.
2. A pattern that separates winners from losers across several briefs becomes a skill edit. A pattern seen once is a signal, not a rule.
3. Every edit still needs a Google source (see the repo `AGENTS.md`). A pick that has no source behind it is the owner's standard and the skill must say so, as it does for Google Sans Flex.
4. Winners and rejects that show a rule clearly are candidates for `calibration/`.
5. Rerun the same briefs after the skill changes and compare against the previous round.

## Files

| File | Job |
| --- | --- |
| `briefs.json` | Briefs used only for preference rounds; the scored phone cases in `evals.json` run too |
| `generate.py` | Builds the variants with the scored evals' agent runner, runs the code checks, skips the judge |
| `serve.py`, `picker.html` | The local picking page and the endpoint that saves picks |
