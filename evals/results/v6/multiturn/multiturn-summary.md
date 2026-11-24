# Multi-turn revision benchmark (v6)

- Backend: `claude` | Model: `claude-fable-5-2` | Commit: `2e679a870de5c0de68e5fbe549aae3b9d566b1de`
- Turns: 10 | Final checks pass: `True`
- Wall time: total `350.32s`, mean/turn `35.03s`, median/turn `31.36s`
- Tokens/turn (mean): total `371003.0`, input+cache `368357.4`, output `2645.6`
- Check calls: total `12`, compile retries: `0`
- Skill bytes read: `49806` across `3` reads (`3` unique files)

| Turn | Revision | Wall (s) | In+Cache tok | Out tok | Total tok | Checks | Retries | Skill B |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Layout shift (Editorial split) | 72.5 | 469838 | 6050 | 475888 | 2 | 0 | 49806 |
| 2 | Typography emphasis (Editorial split) | 39.08 | 461378 | 2774 | 464152 | 2 | 0 | 0 |
| 3 | Targeted element tweak (Editorial split) | 26.32 | 295206 | 1821 | 297027 | 1 | 0 | 0 |
| 4 | Theme and contrast adjustment (shared) | 16.73 | 305776 | 955 | 306731 | 1 | 0 | 0 |
| 5 | Layout shift (Quick pantry) | 37.83 | 319694 | 3146 | 322840 | 1 | 0 | 0 |
| 6 | Targeted element tweak (Quick pantry) | 30.04 | 333878 | 2249 | 336127 | 1 | 0 | 0 |
| 7 | Adaptive fix (Editorial split detail pane) | 32.68 | 353803 | 2737 | 356540 | 1 | 0 | 0 |
| 8 | Typography emphasis (Quick pantry) | 14.81 | 365522 | 629 | 366151 | 1 | 0 | 0 |
| 9 | Theme and contrast adjustment (Editorial split) | 20.83 | 373153 | 1116 | 374269 | 1 | 0 | 0 |
| 10 | Final polish and full verification | 59.5 | 405326 | 4979 | 410305 | 1 | 0 | 0 |
