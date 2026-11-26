| Case | Surface | Status | Code checks | Rubric | Gen (s) | Turns | Out tokens | Retries | Skill bytes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| music | phone | passed | 3/3 | 15/15 | 275.0 | 25 | 40788 | 0 | 133581 |
| finance | phone | failed | 3/3 | 14/15 | 344.0 | 31 | 56360 | 0 | 133581 |
| settings | phone | failed | 3/3 | 14/15 | 224.0 | 22 | 35610 | 0 | 113899 |
| trek | phone | passed | 3/3 | 15/15 | 385.0 | 34 | 50368 | 0 | 110524 |
| recipes | phone | failed | 3/3 | 14/15 | 258.0 | 19 | 47973 | 0 | 115241 |
| workout | wear | passed | 2/2 | 9/9 | 237.0 | 25 | 39388 | 0 | 85220 |
| player | wear | failed | 2/2 | 8/9 | 286.0 | 27 | 48124 | 0 | 70385 |
| reminder | wear | passed | 2/2 | 9/9 | 186.0 | 23 | 28922 | 0 | 60577 |

Generator medians: wall 266.5s, turns 25, input_tokens 140915, output_tokens 44380.5, check_calls 8, compile_retries 0, skill_bytes_read 112211.5 (model: gemini-4-hydrogen-high)

Failures or pending judgments:
- finance R-finance-categories: Fun uses neutral surfaceContainerHighest for both its badge and its LinearProgressIndicator color on a surfaceContainerHighest track, making its 13% progress segment invisible against the track and lacking a distinct hue alongside Transport's muted secondaryContainer green.
- settings R-adaptive: While tablet and desktop adapt into a two-pane layout with the 'Live preview & sync' inspector, settings-foldable.png renders a centered 420dp phone-width column with roughly 142dp empty margins on both sides misaligned with the left-aligned top app bar, pushing 'Last synced' and 'Back up now' below the fold.
- recipes R-text: In discover-desktop.png, the second featured card in the This week carousel shows a caption pill whose title truncates mid-word to "Miso mushro..." because maxLines = 1 clips it inside the 440dp browse pane.
- player R-wear-glance: The non-scrolling screen displays five simultaneous circular action buttons (skip back, pause, skip forward, volume down, and volume up), exceeding the rubric threshold of about three actions at once on a non-scrolling Wear screen.
