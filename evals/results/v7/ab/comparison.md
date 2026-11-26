# A/B 8-Case Gate Comparison (`android` vs `layoutlib`)

- **Model:** `gemini-4-hydrogen-high` (Antigravity harness, fresh subagents per case, blind `eval-judge` subagents)
- **Date:** 2026-11-25

## Summary Table

| Metric | `android` | `layoutlib` | Delta | Gate Bar (`#188` Task 4) |
| --- | --- | --- | --- | --- |
| Cases passing every rubric item | `4 / 8` (`music`, `trek`, `workout`, `reminder`) | `4 / 8` (`music`, `settings`, `trek`, `workout`) | `0` | `layoutlib >= android` (PASS) |
| Total rubric items passed | `98 / 102` | `98 / 102` | `0` | Informational (EQUAL) |
| Deterministic checks passed | `21 / 21` | `20 / 21` (`workout` failed `C-review`) | `-1` | `layoutlib >= android` (FAIL) |
| Overall cases passed (checks + rubric) | `4 / 8` | `3 / 8` (`workout` passed `9/9` rubric, failed `C-review`) | `-1` | Informational |
| Median generator wall time (s) | `266.5` | `211.5` | `-20.6%` | Informational |
| Total generator wall time (s) | `2195.0` | `1855.0` | `-15.5%` | Informational |
| Median output tokens | `44380.5` | `34883` | `-21.4%` | Informational |
| Median check calls | `8` | `7.5` | `-0.5` | Informational |
| Total compile retries | `0` | `0` | `0` | Informational |

## Deterministic Check Failure Note (`workout` on `layoutlib`)

- `workout` on `layoutlib` failed `C-review` (`main+end-confirm/watch: primary-largest; main+end-confirm/watch-large: primary-largest`) after hitting the prompt's 2-round unscoped check cap.
- Cause: the generated `EndRunConfirmOverlay` placed an overlay button slightly taller than the underlying `main` screen's `ad:primary` control (`primaryHeight` 50/52 dp vs 50 dp confirm icon button), which `FindingRules.PrimaryLargest` flags identically on both backends.
- Owner decision (2026-11-25): the single `workout` `C-review` miss (`20/21` vs `21/21`, `9/9` rubric passed) is waived as non-backend-specific agent variance under the 2-round check cap, clearing `#188` to ship and proceed to `#189`.
