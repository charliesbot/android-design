package bot.charlies.androiddesign.preview.domain.node

import bot.charlies.androiddesign.preview.model.ComponentDef
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Reads one component's props the way the previewer draws them and the exporter writes them: a
 * stated value, else the catalog default, with numbers moved into the catalog's bounds.
 *
 * [valueOf] gives a prop's value with bindings already resolved, which is the one thing the callers
 * do differently: the previewer reads the live data model, the exporter its sample data.
 */
class PropReader(private val def: ComponentDef, private val valueOf: (String) -> JsonElement?) {
  /** A primitive prop's content, or null when it is absent or `null`. */
  fun string(key: String): String? {
    val primitive = valueOf(key) as? JsonPrimitive ?: return null
    return if (primitive is JsonNull) null else primitive.content
  }

  /** A boolean prop as stated, or null when it is absent or not a boolean. */
  fun flag(key: String): Boolean? {
    val primitive = valueOf(key) as? JsonPrimitive ?: return null
    return primitive.booleanOrNull ?: primitive.content.toBooleanStrictOrNull()
  }

  /** A boolean prop: its value, else the catalog default, else false. */
  fun bool(key: String): Boolean = flag(key) ?: def.defaultOf(key)?.booleanOrNull ?: false

  /** An enum prop: its value, else the catalog default, which every enum read this way has. */
  fun enum(key: String): String = string(key) ?: requiredDefault(key).content

  /** An enum prop parsed by [parse]; a value [parse] rejects falls back to the catalog default. */
  fun <E : Any> enum(key: String, parse: (String) -> E?): E =
    string(key)?.let(parse)
      ?: parse(requiredDefault(key).content)
      ?: error("${def.name}.$key has a default that is not one of its values")

  /**
   * A finite number prop, moved into the bounds the catalog gives it. A bound value skips
   * validation, so this is what keeps one out of range from reaching a Compose API that throws or
   * hangs on it. `NaN` and `Infinity`, which a bound string can hold, are not numbers.
   */
  fun number(key: String): Double? {
    val value =
      (valueOf(key) as? JsonPrimitive)?.doubleOrNull?.takeIf { it.isFinite() } ?: return null
    return def.coerceNumber(key, value.toFloat().toDouble())
  }

  /** A number prop with a catalog default: [number], else the default. */
  fun numberOrDefault(key: String): Double = number(key) ?: requiredDefault(key).content.toDouble()

  private fun requiredDefault(key: String): JsonPrimitive =
    def.defaultOf(key) ?: error("${def.name}.$key has no catalog default")
}
