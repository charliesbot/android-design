# Multi-turn revision benchmark (v7)

- Backend: `antigravity` | Model: `gemini-4-hydrogen-high` | Commit: `b8306e8668f7419ee0c4a657f44632596ac01350`
- Turns: 10 | Final checks pass: `True`
- Wall time: total `378.0s`, mean/turn `37.8s`, median/turn `35.0s`
- Tokens/turn (mean): total `525639.9`, input+cache `521011.1`, output `4628.8`
- Check calls: total `10`, compile retries: `0`
- Skill bytes read: `63229` across `4` reads (`4` unique files)

| Turn | Revision | Wall (s) | In+Cache tok | Out tok | Total tok | Checks | Retries | Skill B |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Layout shift (Editorial split) | 65.0 | 354269 | 11707 | 365976 | 1 | 0 | 63229 |
| 2 | Typography emphasis (Editorial split) | 25.0 | 324166 | 3824 | 327990 | 1 | 0 | 0 |
| 3 | Targeted element tweak (Editorial split) | 31.0 | 355923 | 4406 | 360329 | 1 | 0 | 0 |
| 4 | Theme and contrast adjustment (shared) | 21.0 | 379176 | 1515 | 380691 | 1 | 0 | 0 |
| 5 | Layout shift (Quick pantry) | 46.0 | 580315 | 5593 | 585908 | 1 | 0 | 0 |
| 6 | Targeted element tweak (Quick pantry) | 38.0 | 460566 | 5017 | 465583 | 1 | 0 | 0 |
| 7 | Adaptive fix (Editorial split detail pane) | 35.0 | 593106 | 4065 | 597171 | 1 | 0 | 0 |
| 8 | Typography emphasis (Quick pantry) | 28.0 | 625162 | 1400 | 626562 | 1 | 0 | 0 |
| 9 | Theme and contrast adjustment (Editorial split) | 35.0 | 662199 | 2906 | 665105 | 1 | 0 | 0 |
| 10 | Final polish and full verification | 54.0 | 875229 | 5855 | 881084 | 1 | 0 | 0 |
