package bot.charlies.androiddesign.preview.domain.validation

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Reads JSON values the way A2UI dynamic values are written. */
internal object Dynamic {
  /** `{"@path": "..."}`. */
  fun isBinding(value: JsonElement): Boolean {
    val obj = value as? JsonObject ?: return false
    return obj.keys == setOf("@path") && obj.string("@path") != null
  }

  /** `{"@call": "@index", ...}`, the index of the current template item. */
  fun isIndex(value: JsonElement): Boolean = (value as? JsonObject)?.string("@call") == "@index"

  /** A binding or an `@index` call: a value resolved at render time. */
  fun isDynamic(value: JsonElement): Boolean = isBinding(value) || isIndex(value)

  fun JsonObject.string(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  fun JsonElement.asString(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

  /** A JSON number literal. `NaN` and `Infinity` are not numbers a component can use. */
  fun JsonElement.isNumber(): Boolean = number() != null

  /** The finite number a literal holds, or null. */
  fun JsonElement.number(): Double? =
    (this as? JsonPrimitive)
      ?.takeIf { !it.isString }
      ?.content
      ?.toDoubleOrNull()
      ?.takeIf {
        it.isFinite()
      }

  fun JsonElement.isBoolean(): Boolean =
    this is JsonPrimitive && !isString && (content == "true" || content == "false")
}
