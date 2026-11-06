package bot.charlies.androiddesign.preview.domain

/**
 * What Material's search guidance fixes, in dp and counts. The renderer, the exporter and the
 * validator all read it, so the preview, the exported code and the rules agree.
 */
object SearchSpec {
  /** The size of the avatar a search bar or search app bar may end with. */
  const val AVATAR_DP = 30f

  /**
   * The most trailing elements inside a search bar: "one or two trailing icons or icon buttons",
   * and an avatar combines "with up to one other trailing icon button", so the avatar counts as
   * one.
   */
  const val MAX_BAR_TRAILING = 2

  /** The most trailing icon buttons a search app bar has beside an avatar. */
  const val MAX_APP_BAR_ACTIONS_WITH_AVATAR = 2

  /** The most trailing icon buttons a search app bar has at all, which only large screens fit. */
  const val MAX_APP_BAR_ACTIONS = 4
}
