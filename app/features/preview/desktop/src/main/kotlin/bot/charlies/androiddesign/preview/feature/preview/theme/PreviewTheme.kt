package bot.charlies.androiddesign.preview.feature.preview.theme

import bot.charlies.androiddesign.preview.model.DesignSystem

/**
 * What the renderer needs of a resolved theme whichever catalog the proposal is in. The phone's
 * [ResolvedTheme] and the Wear
 * [bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearTheme] are the two.
 */
interface PreviewTheme {
  val design: DesignSystem
  val dark: Boolean
}
