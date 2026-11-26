| Case | Surface | Status | Code checks | Rubric | Gen (s) | Turns | Out tokens | Retries | Skill bytes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| music | phone | passed | 3/3 | 15/15 | 202.0 | 23 | 30543 | 0 | 133581 |
| finance | phone | failed | 3/3 | 14/15 | 201.0 | 18 | 31796 | 0 | 133581 |
| settings | phone | passed | 3/3 | 15/15 | 220.0 | 23 | 36910 | 0 | 116689 |
| trek | phone | passed | 3/3 | 15/15 | 195.0 | 17 | 28059 | 0 | 115944 |
| recipes | phone | failed | 3/3 | 14/15 | 296.0 | 23 | 51452 | 0 | 105995 |
| workout | wear | failed | 1/2 | 9/9 | 301.0 | 35 | 45628 | 0 | 60577 |
| player | wear | failed | 2/2 | 8/9 | 203.0 | 21 | 33331 | 0 | 60577 |
| reminder | wear | failed | 2/2 | 8/9 | 237.0 | 26 | 36435 | 0 | 103059 |

Generator medians: wall 211.5s, turns 23, input_tokens 137327.5, output_tokens 34883, check_calls 7.5, compile_retries 0, skill_bytes_read 110969.5 (model: gemini-4-hydrogen-high)

Failures or pending judgments:
- finance R-finance-categories: In RecentTransactionsSection, all transaction list badges render in uniform gray (surfaceContainerHighest) instead of keeping each category's badge color from the By category cards, and the four category card badges (Home primaryContainer, Transport secondaryContainer, Food tertiaryContainer, Fun surfaceContainerHighest) are not all distinguishable by hue.
- recipes R-text: In main-light, main-dark, main-tablet, and main-desktop, squeezing three compact featured cards into one narrow row causes the second card title to truncate mid-word as "Miso mushroom ra...".
- workout C-review: main+end-confirm/watch: primary-largest; main+end-confirm/watch-large: primary-largest
- player R-wear-glance: The non-scrolling screen presents five separate action buttons at once (skip back, pause, skip forward, volume down, volume up), exceeding the ~3-action glanceability limit and vertically crowding the layout so the bottom volume buttons are compressed into ovals against the progress ring.
- reminder R-wear-hero: On main/watch and main/watch-large, Today is set in titleMedium (nearly the same visual size as the labelMedium reminder rows) and the key count status (3 left and 1/4) sits in labelMedium/labelSmall rather than a Display/Numeral style or clearly largest hero element.
