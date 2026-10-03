# Preference rounds

The scored evals check the skill's rules. Preference rounds check taste: the owner picks the best and worst of several unsteered variants per brief, and the differences between picked and rejected variants become skill edits.

## Run a round

```bash
cd evals
python3 preference/generate.py --generator codex  # explicitly choose codex or claude
python3 preference/serve.py ~/.cache/android-design-evals/preference-<timestamp>
```

Generation uses the selected CLI and its configured model, unless `--generator-model` is supplied. It does not bypass permissions. Cost and runtime depend on that configuration. Finished variants are skipped on resume; incomplete directories are preserved and reported instead of deleted.

`serve.py` opens a local page on 127.0.0.1. Pick a best and a worst per brief (keys 1 to 3, Shift for worst, N for none good, L/D/F/W to switch renders), tag the reasons, and add a note when a reason chip is not enough. Changes are queued per brief and saved to `selections.json` in the round folder. Wait for **All changes saved** in the header before closing the tab. If saving fails, keep the tab open and use **Retry saving**. Pending edits live in the tab until the server confirms them, not in browser storage. The picker warns before leaving with unsaved edits, but cannot protect them if the browser is forcibly closed. Run one picker server per round. Concurrent requests to that server are serialized and the selections file is replaced atomically.

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

## Verify save behavior

From the repository root, run `python3 -m unittest discover evals` and
`node --test evals/preference/test_picker.mjs` (Node.js 20 or newer, no npm packages).
These tests cover cross-brief edits, edits during a save, HTTP and network failures,
retry, pending-change warnings, concurrent server writes, and failed file replacement.
