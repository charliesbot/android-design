package bot.charlies.androiddesign.preview.domain

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * What a `badge` value draws. The renderer and the exporter both read it, so a value gives the same
 * badge in the preview and in the exported code.
 */
sealed interface BadgeContent {
  /** A 6dp dot. */
  data object Small : BadgeContent

  /** A badge that holds [label]. */
  data class Large(val label: String) : BadgeContent

  companion object {
    /**
     * The badge [value] asks for, or null for none: `true` is small, a number or a text is large,
     * and `false`, a blank text or no value draws nothing.
     */
    fun of(value: JsonPrimitive?): BadgeContent? {
      if (value == null || value is JsonNull) return null
      if (value.isString) return value.content.takeIf { it.isNotBlank() }?.let(::Large)
      value.booleanOrNull?.let {
        return if (it) Small else null
      }
      val number = value.doubleOrNull?.takeIf { it.isFinite() } ?: return null
      return Large(if (number == Math.rint(number)) number.toLong().toString() else value.content)
    }
  }
}
