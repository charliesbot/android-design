package bot.charlies.androiddesign.preview.domain

/**
 * The icon in a switch's thumb, for the renderer and the exporter alike. Material: "Use icons that
 * clearly communicate whether the switch is on or off, such as an X and a checkmark".
 */
object SwitchSpec {
  const val CHECK = "check"
  const val CLOSE = "close"

  /**
   * The Material Symbols name of the thumb icon when the switch is [checked], for `icons` of
   * `none`, `selected` (a check when on) or `both` (a close when off too); null for no icon.
   */
  fun iconAt(icons: String, checked: Boolean): String? =
    when {
      checked && icons != "none" -> CHECK
      !checked && icons == "both" -> CLOSE
      else -> null
    }
}
