# Multi-turn Benchmark Comparison (`android` vs `layoutlib`)

- **Model:** `gemini-4-hydrogen-high` (Antigravity harness, single resumed subagent across 10 revisions)
- **Fixture:** `evals/multiturn_fixture/design/proposal.explore.kt`
- **Date:** 2026-11-25

## Summary Table

| Metric | `android` | `layoutlib` | Delta | Gate Bar (`#188` Task 4) |
| --- | --- | --- | --- | --- |
| Final deterministic checks | `3 / 3` (`True`) | `3 / 3` (`True`) | `0` | Pass parity (PASS) |
| Total wall time (s) | `378.0` | `409.0` | `+8.20%` (`+31.0s`) | `<= +5.0%` (`396.9s`) (FAIL) |
| Mean wall time per turn (s) | `37.8` | `40.9` | `+8.20%` | Tied to total wall |
| Mean total tokens per turn | `525639.9` | `527612.7` | `+0.38%` | `<= +5.0%` (`551921.9`) (PASS) |
| Mean input+cache tokens per turn | `521011.1` | `523488.0` | `+0.47%` | Informational |
| Mean output tokens per turn | `4628.8` | `4124.7` | `-10.89%` | Informational |
| Agent tool steps (10 turns total) | `59` | `61` | `+2` | Informational |
| Total `check` calls | `10` | `10` | `0` | `0` retries each |
| Compile retries | `0` | `0` | `0` | Informational |
| Aggregate CLI `check` execution (s) | `32.0` | `43.0` | `+11.0s` | Warm worker overhead |

## Notes

- Output tokens per turn were lower on `layoutlib` (`4124.7` vs `4628.8`, `-10.89%`) with identical `check` call counts (`10`) and `0` compile retries.
- Exact server-side `compileMs + renderMs` totals extracted from `transcript_full.jsonl` across all 10 `check` calls: `android` `3.32 s` (`2218 ms` compile + `1101 ms` render) vs `layoutlib` `4.16 s` (`2000 ms` compile + `2157 ms` render), delta `+0.84 s` (`0.22%` of total wall time).
- Total wall time variance (`+31.0s`) combined `+20.0s` LLM step variance across `61` vs `59` agent steps and `+11.0s` across the 10 scoped `check` CLI invocations (`32.0s` vs `43.0s`, consistent with `evals/results/v7/speed.json` scoped median `356 ms` plus JVM CLI wrapper/HTTP startup per call).
- Owner decision (2026-11-25): the `+8.20%` single-run wall-time miss is waived as LLM step variance (`+20 s` across `61` vs `59` steps) plus CLI wrapper overhead (`+11 s`), not layoutlib render latency.
