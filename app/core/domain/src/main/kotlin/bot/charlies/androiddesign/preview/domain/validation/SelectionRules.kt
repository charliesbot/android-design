package bot.charlies.androiddesign.preview.domain.validation

/**
 * The selection counts Material asks of a group, read from the props a proposal authors. A group
 * whose selection comes from data cannot be judged here, so callers skip it.
 *
 * Radio buttons: "Radio buttons should be vertically listed and have one option always selected."
 * Single-select menus: "can have one item selected at a time."
 */
internal object SelectionRules {
  /** What is wrong with a radio group that has [selected] selected buttons, or null. */
  fun radioGroupProblem(selected: Int): String? =
    if (selected == 1) null
    else
      "a RadioGroup has exactly one selected RadioButton (radio buttons always have one option " +
        "selected), got $selected"

  /** What is wrong with a menu group that has [selected] single-select items selected, or null. */
  fun singleSelectProblem(selected: Int): String? =
    if (selected <= 1) null
    else "single-select menu items select one at a time, got $selected selected in one group"
}
