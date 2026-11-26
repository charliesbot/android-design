# Multi-turn revision benchmark (v7)

- Backend: `antigravity` | Model: `gemini-4-hydrogen-high` | Commit: `b8306e8668f7419ee0c4a657f44632596ac01350`
- Turns: 10 | Final checks pass: `True`
- Wall time: total `409.0s`, mean/turn `40.9s`, median/turn `38.0s`
- Tokens/turn (mean): total `527612.7`, input+cache `523488.0`, output `4124.7`
- Check calls: total `10`, compile retries: `0`
- Skill bytes read: `63229` across `4` reads (`4` unique files)

| Turn | Revision | Wall (s) | In+Cache tok | Out tok | Total tok | Checks | Retries | Skill B |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Layout shift (Editorial split) | 58.0 | 341917 | 9957 | 351874 | 1 | 0 | 63229 |
| 2 | Typography emphasis (Editorial split) | 36.0 | 375820 | 2563 | 378383 | 1 | 0 | 0 |
| 3 | Targeted element tweak (Editorial split) | 34.0 | 411592 | 3049 | 414641 | 1 | 0 | 0 |
| 4 | Theme and contrast adjustment (shared) | 31.0 | 440561 | 1481 | 442042 | 1 | 0 | 0 |
| 5 | Layout shift (Quick pantry) | 45.0 | 486557 | 5588 | 492145 | 1 | 0 | 0 |
| 6 | Targeted element tweak (Quick pantry) | 50.0 | 543132 | 5376 | 548508 | 1 | 0 | 0 |
| 7 | Adaptive fix (Editorial split detail pane) | 40.0 | 588332 | 3863 | 592195 | 1 | 0 | 0 |
| 8 | Typography emphasis (Quick pantry) | 30.0 | 625484 | 1778 | 627262 | 1 | 0 | 0 |
| 9 | Theme and contrast adjustment (Editorial split) | 36.0 | 668367 | 2784 | 671151 | 1 | 0 | 0 |
| 10 | Final polish and full verification | 49.0 | 753118 | 4808 | 757926 | 1 | 0 | 0 |
