# Screenshot rubric

The judge sees the fixed renders of one case and answers each applicable item PASS or FAIL with a one-sentence reason. Judge only what is visible. When unsure, FAIL and say why.

Phone renders: `light`, `dark`, `font200` (200% text), `wallpaper` (red dynamic-color wallpaper). Wear renders: `large`, `small`, `font`. Widget renders: `light-2x2`, `light-4x2`, `dark-2x2`, `dark-4x2`.

## General items

- **R-hierarchy:** The screen's primary goal or action is the most prominent thing; the eye lands on it first. FAIL when several elements compete equally or a summary outranks the task.
- **R-fill:** At most one strong saturated fill competes for attention (a hero control, or one coherent group). FAIL when several unrelated saturated fills or cards compete.
- **R-surfaces:** Most of the screen is calm, near-neutral surface; accents sit on elements with a job. FAIL for a neon or heavily tinted look where containers or a tinted background cover most of the screen.
- **R-controls:** Every action looks tappable and reads as its own control. FAIL for text-only buttons floating alone outside a group of actions, or a toolbar whose items merge into one wide pill.
- **R-font:** The hero title or number is clearly Google Sans Flex with strong axis settings: heavy (bold or black), extremely narrow or wide, or visibly rounded. FAIL for a regular or medium weight at a normal or only slightly condensed width: that reads as a generic sans even when it is Google Sans Flex.
- **R-platform:** Android conventions, no iOS habits: no disclosure chevrons on list rows, no "Cancel"/"Done" text in the top bar, content drawn edge to edge.
- **R-text:** No clipped, truncated-without-reason, or overlapping text in any render, including `font200` or `font`. Layouts reflow rather than break.
- **R-dark:** The dark render is fully legible: icons and text are visible against their backgrounds (no black icons on dark surfaces).
- **R-idea:** The screen has a visible design idea specific to its content, beyond a standard component-catalog layout.

## Case items

Each case in `evals.json` adds items with their own text (ids starting `R-<case>-`). Judge them the same way.

## Output

Reply with only this JSON:

```json
{"items": [{"id": "R-hierarchy", "pass": true, "reason": "..."}]}
```
