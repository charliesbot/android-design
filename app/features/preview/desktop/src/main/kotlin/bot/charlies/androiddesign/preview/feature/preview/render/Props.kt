package bot.charlies.androiddesign.preview.feature.preview.render

import bot.charlies.androiddesign.preview.domain.node.ChildItems
import bot.charlies.androiddesign.preview.domain.node.childId
import bot.charlies.androiddesign.preview.domain.node.childItems
import bot.charlies.androiddesign.preview.model.ActionFunction
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Where bindings resolve: the screen's data model, and inside a template the current item.
 * `{"@path": "/plates"}` reads from the model root; `{"@path": "name"}` reads from [item].
 */
internal class DataScope(
  private val model: JsonElement?,
  private val item: JsonElement?,
  /** Template indices from the outermost template to the innermost. */
  val indices: List<Int>,
) {
  fun resolve(path: String): JsonElement? {
    val absolute = path.startsWith("/")
    val start = if (absolute) model else item ?: model
    return walk(start, path.trim('/').split('/').filter { it.isNotEmpty() })
  }

  /** The scope of item [index] of the array at [path]. */
  fun itemScope(path: String, index: Int): DataScope {
    val array = resolve(path) as? JsonArray
    return DataScope(model, array?.getOrNull(index), indices + index)
  }

  fun itemCount(path: String): Int = (resolve(path) as? JsonArray)?.size ?: 0

  /** The suffix that keeps template instances apart: `[0]`, `[1][2]`. */
  val instanceSuffix: String
    get() = indices.joinToString("") { "[$it]" }

  private fun walk(start: JsonElement?, segments: List<String>): JsonElement? {
    var current = start
    for (segment in segments) {
      current =
        when (current) {
          is JsonObject -> current[segment]
          is JsonArray -> segment.toIntOrNull()?.let { current.getOrNull(it) }
          else -> null
        }
    }
    return current
  }

  companion object {
    fun root(model: JsonElement?) = DataScope(model, null, emptyList())
  }
}

/** A child to render: the component id and the data scope it renders in. */
internal data class ChildRef(val id: String, val scope: DataScope)

/** An action the renderer runs itself. Nothing ever reaches the agent. */
internal sealed interface ActionCall {
  data class Navigate(val screen: String) : ActionCall

  data object Back : ActionCall

  data class Open(val overlay: String) : ActionCall

  data object Dismiss : ActionCall
}

/** A component's props with bindings resolved against a [DataScope]. */
internal class Props(private val json: JsonObject, private val scope: DataScope) {
  operator fun contains(key: String): Boolean = key in json

  /** The raw value, with bindings and `@index` already replaced. */
  fun value(key: String): JsonElement? = resolve(json[key])

  fun string(key: String): String? {
    val primitive = value(key) as? JsonPrimitive ?: return null
    return if (primitive is JsonNull) null else primitive.content
  }

  fun bool(key: String): Boolean? {
    val primitive = value(key) as? JsonPrimitive ?: return null
    return primitive.booleanOrNull ?: primitive.content.toBooleanStrictOrNull()
  }

  /** A finite number. `NaN` and `Infinity`, which a bound string can hold, are not numbers. */
  fun float(key: String): Float? =
    (value(key) as? JsonPrimitive)?.doubleOrNull?.takeIf { it.isFinite() }?.toFloat()

  /** A `[min, max]` pair. */
  fun range(key: String): Pair<Float, Float>? {
    val array = value(key) as? JsonArray ?: return null
    val numbers = array.mapNotNull {
      (it as? JsonPrimitive)?.doubleOrNull?.takeIf(Double::isFinite)?.toFloat()
    }
    return if (numbers.size == 2) numbers[0] to numbers[1] else null
  }

  /** The id a `Child` or `Anchor` prop points at. */
  fun childId(key: String): String? = json[key].childId()

  /** The children of a `ChildList` prop: listed ids, or one per item of a template's array. */
  fun children(key: String): List<ChildRef> =
    when (val items = json[key].childItems()) {
      is ChildItems.Listed -> items.ids.map { ChildRef(it, scope) }
      is ChildItems.Template ->
        (0 until scope.itemCount(items.path)).map {
          ChildRef(items.componentId, scope.itemScope(items.path, it))
        }
      null -> emptyList()
    }

  fun action(key: String): ActionCall? {
    val call = (json[key] as? JsonObject)?.get("functionCall") as? JsonObject ?: return null
    val function =
      (call["@call"] as? JsonPrimitive)?.content?.let(ActionFunction::fromWire) ?: return null
    val args = call["args"] as? JsonObject
    fun arg(name: String) = (args?.get(name) as? JsonPrimitive)?.content
    return when (function) {
      ActionFunction.Navigate -> arg("screen")?.let(ActionCall::Navigate)
      ActionFunction.Back -> ActionCall.Back
      ActionFunction.Open -> arg("overlay")?.let(ActionCall::Open)
      ActionFunction.Dismiss -> ActionCall.Dismiss
    }
  }

  private fun resolve(element: JsonElement?): JsonElement? {
    val obj = element as? JsonObject ?: return element
    obj["@path"]?.let { path ->
      return (path as? JsonPrimitive)?.content?.let(scope::resolve)
    }
    if ((obj["@call"] as? JsonPrimitive)?.content == "@index") {
      return scope.indices.lastOrNull()?.let { JsonPrimitive(it) }
    }
    return element
  }
}
