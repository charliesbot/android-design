| Case | Surface | Status | Code checks | Rubric | Gen (s) | Turns | Out tokens | Retries | Skill bytes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| music | phone | failed | 2/3 | 11/15 | 237.23 | 30 | 22582 | 0 | 63625 |
| finance | phone | failed | 3/3 | 14/15 | 190.93 | 30 | 17573 | 1 | 90904 |
| settings | phone | failed | 3/3 | 13/15 | 293.83 | 47 | 29166 | 2 | 65089 |
| trek | phone | passed | 3/3 | 15/15 | 158.34 | 18 | 15922 | 1 | 63625 |
| recipes | phone | failed | 3/3 | 14/15 | 289.23 | 38 | 28573 | 1 | 63625 |
| workout | wear | failed | 1/2 | 9/9 | 117.52 | 21 | 11038 | 0 | 61015 |
| player | wear | passed | 2/2 | 9/9 | 143.69 | 21 | 13869 | 0 | 61015 |
| reminder | wear | failed | 1/2 | 8/9 | 143.07 | 20 | 14018 | 0 | 61015 |

Generator medians: wall 174.6s, turns 25.5, input_tokens 29, output_tokens 16747.5, check_calls 6.5, compile_retries 0.5, skill_bytes_read 63625 (model: claude-opus-5-5)

Failures or pending judgments:
- music C-review: player+lyrics/light: offscreen; player+lyrics/light: overflow; player+lyrics/dark: offscreen; player+lyrics/dark: overflow; player+lyrics/foldable: overflow; player+lyrics/foldable: overflow; player+queue/light: offscreen; player+queue/light: overflow; player+queue/dark: offscreen; player+queue/dark: overflow; player+queue/foldable: overflow; player+queue/foldable: overflow; player+credits/light: offscreen; player+credits/light: overflow; player+credits/dark: offscreen; player+credits/dark: overflow; player+credits/foldable: overflow; player+credits/foldable: overflow
- music R-controls: Lyrics, Queue, and Credits are borderless text buttons inside one tonal pill, so the toolbar reads as a single wide pill rather than three separate controls.
- music R-font: 'Golden Hour' uses Fraunces at its default regular weight with no visible axis treatment, which reads as a brand face swapped in without intent.
- music R-idea: Apart from the wavy progress bar and the highlighted lyric line, this is a standard catalog now-playing layout with no specific design idea.
- music R-music-actions: The three actions are icon+label TextButtons with no individual containers inside one tonal pill, so they read as one wide pill rather than distinct icon buttons.
- finance R-finance-categories: All four categories share the same tertiary yellow badge and olive bar, and the recent list icons are neutral, so the categories cannot be told apart by hue.
- settings R-font: The hero 'Settings' title and '2 min ago' use Fraunces at what looks like a regular weight with no visible axis treatment, which the rubric treats as a brand face swapped in without intent.
- settings R-semantics: The section titles and 'Preview' carry heading() and the switch rows are single Role.Switch targets with null callbacks, but the 'Notifications' label that groups the notification previews in PreviewPane has no heading().
- recipes R-font: Fraunces is used for 'Simmer' and the recipe titles at a regular weight with no axis settings, so the brand face shows no deliberate weight or width choice.
- workout C-review: run+end-confirm/watch: contrast; run+end-confirm/watch: contrast
- reminder C-review: list/watch: primary-largest; list/watch-large: primary-largest
- reminder R-wear-hero: The list's "3" is a strong hero, but on the saved confirmation the hero check mark is black on a dark brown circle, so the key status is hard to see on both watches.
