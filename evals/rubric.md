# Screenshot rubric

The judge sees the fixed renders of one case and answers each applicable item PASS or FAIL with a one-sentence reason. Judge only what is visible. When unsure, FAIL and say why.

Every case has six renders of the HTML proposal. Three are phone size (411x923): `light`, `dark`, and `wallpaper` (the theme color set to red, standing in for a red dynamic-color wallpaper). Three show larger windows in light: `foldable` (933x704, expanded), `tablet` (1280x800, large), and `desktop` (1920x1080, extra large). Items about dark or wallpaper look at that render; the rest look at all of them.

## General items

- **R-hierarchy:** The screen's primary goal, established by the brief, receives the strongest visual emphasis. That goal may be understanding content or performing an action. A summary may lead when understanding it is the primary task. When the primary task is acting on a list, summaries must not overshadow actionable items. FAIL when unrelated elements compete equally or supporting content obscures the primary task. The main action must remain easy to identify. Source: [Expressive hierarchy guidance](../sources/m3.material.io/pages/building-with-m3-expressive.md), especially prominence for important content, tasks, or actions.
- **R-fill:** At most one strong saturated fill competes for attention (a hero control, or one coherent group). FAIL when several unrelated saturated fills or cards compete.
- **R-surfaces:** Surfaces establish a clear backdrop and grouping; accents emphasize content, actions, or state. Calm, near-neutral surfaces are the default, not a requirement for every screen. A coherent full-screen content-color treatment can PASS when tonal separation keeps content and controls distinct; judge the visible relationships, not the amount of tinted area. FAIL when widespread strong fills flatten the hierarchy or unrelated colored containers compete with the task. A screenshot cannot prove color extraction or token usage. Source: [content-color guidance](../sources/m3.material.io/pages/styles/color/advanced.md), especially full-screen content-color moments and visible source content.
- **R-controls:** Every action looks tappable and reads as its own control. FAIL for text-only buttons floating alone outside a group of actions, or a toolbar whose items merge into one wide pill.
- **R-font:** The hero title or number has a deliberate voice. PASS when it is Google Sans Flex with strong axis settings (heavy bold or black, extremely narrow or wide, or visibly rounded), or one bundled brand face (Fraunces, Bricolage Grotesque, Big Shoulders Display, or Doto) used at display or headline size with clear intent (its own weight, width, or other axes suited to the direction). Body and label text stays Google Sans Flex, and Google Sans Code may carry numbers and data. FAIL for a regular or medium weight at a normal or only slightly condensed width: that reads as a generic sans even when it is Google Sans Flex, and a brand face swapped in at regular weight with no intent is the same failure.
- **R-platform:** Android conventions, no iOS habits: no disclosure chevrons on list rows, no "Cancel"/"Done" text in the top bar, content drawn edge to edge.
- **R-text:** No clipped, truncated-without-reason, or overlapping text in any render shown. Layouts reflow rather than break.
- **R-dark:** The dark render is fully legible: icons and text are visible against their backgrounds (no black icons on dark surfaces).
- **R-adaptive:** The layout adapts across the size classes shown: compact phone, expanded foldable, large tablet, and extra large desktop. Moving up a size class reveals more (a second pane, an expanded rail, more detail) rather than only enlarging the phone layout, and navigation moves from a bar to a rail with the margins and panes the layout guidance gives. PASS needs at least one larger render to show more at once than the compact render's first screen: a second pane of content side by side (like an inbox beside the open conversation), more of a list, or more detail ("What should be revealed?"). Content the phone only reaches by scrolling counts as revealed when the larger render shows it alongside. FAIL when every larger render shows the same single view as the phone, only rearranged or enlarged (for example art beside controls with nothing added), or when a larger render is a stretched or centered phone layout, or its structure breaks (a pane overflows the window, panes overlap, or a large empty void). Judge the layout structure only: awkward text wrapping or cramped text inside an otherwise adapted pane belongs to R-text, not here. Source: [window size classes and layout](../sources/m3.material.io/pages/foundations/layout/breakpoints.md), applied through [references/layout.md](../skills/android-design/references/layout.md).
- **R-idea:** The screen has a visible design idea specific to its content, beyond a standard component-catalog layout.

## Case items

Each case in `evals.json` adds items with their own text (ids starting `R-<case>-`). Judge them the same way.

## Output

Reply with only this JSON:

```json
{"items": [{"id": "R-hierarchy", "pass": true, "reason": "..."}]}
```
