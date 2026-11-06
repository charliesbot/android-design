package bot.charlies.androiddesign.preview.domain

/**
 * What Material's search guidance fixes, in dp and counts. The renderer, the exporter and the
 * validator all read it, so the preview, the exported code and the rules agree.
 */
object SearchSpec {
  /** The size of the avatar a search bar or search app bar may end with. */
  const val AVATAR_DP = 30f

  /** The square an avatar sits in, so it lines up with the 48dp icon buttons beside it. */
  const val AVATAR_BOX_DP = 48f

  /**
   * The most trailing elements inside a search bar: "one or two trailing icons or icon buttons",
   * and an avatar combines "with up to one other trailing icon button", so the avatar counts as
   * one.
   */
  const val MAX_BAR_TRAILING = 2

  /** "Up to two trailing icons on mobile", an avatar coming in addition. */
  const val MAX_APP_BAR_ACTIONS_COMPACT = 2

  /** "Up to four trailing icons on larger screens", which is medium windows and up. */
  const val MAX_APP_BAR_ACTIONS_LARGE = 4
}
