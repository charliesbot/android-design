package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.model.WearCatalog

/**
 * The Wear color scheme to write: `0xAARRGGBB` per role of [WearCatalog.colorRoles]. The previewer
 * computes it (Wear's own dynamic scheme over the proposal's seed, with the role overrides on top),
 * so the export carries the exact colors it drew.
 */
class WearColors private constructor(internal val roles: Map<String, String>) {
  companion object {
    private val HEX = Regex("#[0-9A-Fa-f]{6}")

    /** The scheme for [colors] (`#RRGGBB` per role); a missing or malformed role is an error. */
    fun of(colors: Map<String, String>): WearColors {
      val missing = WearCatalog.colorRoles.filter { it !in colors }
      if (missing.isNotEmpty()) {
        throw PreviewException("the Wear color scheme lacks ${missing.joinToString()}")
      }
      val literals =
        WearCatalog.colorRoles.associateWith { role ->
          val hex = colors.getValue(role)
          if (!HEX.matches(hex)) throw PreviewException("not a #RRGGBB color for $role: $hex")
          "0xFF" + hex.substring(1).uppercase()
        }
      return WearColors(literals)
    }
  }
}
