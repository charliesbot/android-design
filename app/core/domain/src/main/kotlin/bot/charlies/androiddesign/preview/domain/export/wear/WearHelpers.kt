package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.RawDecl
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.model.WearCatalog

/** The helpers a Wear export writes once when a screen calls them. */
internal object WearHelpers {
  /** Wear's large-display breakpoint as a function, so a screen reads `isLargeDisplay()`. */
  fun largeDisplay(file: FileContext): Decl {
    file.use(Sym.Composable)
    val configuration = file.use(WearSym.LocalConfiguration)
    return RawDecl(
      listOf(
        "/** True on a large Wear display: Wear's breakpoint is ${WearCatalog.LARGE_MIN_DP}dp wide. */",
        "@Composable",
        "private fun isLargeDisplay(): Boolean =",
        "  $configuration.current.screenWidthDp >= ${WearCatalog.LARGE_MIN_DP}",
      ),
    )
  }

  /**
   * The scroll facts of a body that does not scroll. A scaffold sizes its edge button to the room
   * below the last item; with room for the whole button the body stops above it, as it does at the
   * end of a list.
   */
  fun fixedScrollInfoProvider(file: FileContext): Decl {
    val provider = file.use(WearSym.ScrollInfoProvider)
    return RawDecl(
      listOf(
        "/**",
        " * The scroll facts of a body that does not scroll. The scaffold sizes the edge button to the",
        " * room below the last item, so a body with no list reports room for the whole button.",
        " */",
        "private object FixedScrollInfoProvider : $provider {",
        "  override val isScrollAwayValid = false",
        "  override val isScrollable = false",
        "  override val isScrollInProgress = false",
        "  override val anchorItemOffset = 0f",
        "  override val lastItemOffset = 4096f",
        "}",
      ),
    )
  }
}
