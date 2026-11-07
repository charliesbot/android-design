package bot.charlies.androiddesign.preview.domain

/**
 * A radio button row: Compose has no labeled radio button, so the renderer and the exporter both
 * compose one from a `RadioButton` and a `Text` in a `Row` with these numbers. Material gives the
 * 48dp target; the gap is this port's choice.
 */
object RadioSpec {
  /** The row's least height, the touch target of "20dp icon, 48dp target". */
  const val MIN_HEIGHT_DP = 48f

  /** The gap between the button and its label. */
  const val GAP_DP = 12f
}
