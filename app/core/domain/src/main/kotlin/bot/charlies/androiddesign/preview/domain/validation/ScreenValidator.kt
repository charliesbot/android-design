package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.validation.Dynamic.asString
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isBoolean
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isDynamic
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isNumber
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.number
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.string
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Validates one screen of a proposal against the catalog and the merged design system. */
internal class ScreenValidator(
  private val sink: ErrorSink,
  private val proposal: Proposal,
  private val design: DesignSystem,
  private val screenId: String,
  private val screen: Screen,
) {
  private val root = "/screens".child(screenId)
  private val componentsPath = root.child("components")
  private val byId: Map<String, ComponentNode> =
    screen.components.filter { it.id.isNotEmpty() }.reversed().associateBy { it.id }

  fun validate() {
    if (!IDENTIFIER.matches(screenId)) {
      sink.add(root, "screen id '$screenId' must match ${IDENTIFIER.pattern}")
    }
    if (screen.catalogId != M3ExpressiveCatalog.ID) {
      sink.add(
        root.child("catalogId"),
        "catalogId must be the m3-expressive catalog: ${M3ExpressiveCatalog.ID}",
      )
    }
    validateIdentity()
    screen.components.forEachIndexed { index, node ->
      val def = M3ExpressiveCatalog.find(node.component) ?: return@forEachIndexed
      val path = componentsPath.child(index)
      validateProps(path, def, node.props, overrides = false)
    }
    detectCycles()
  }

  private fun validateIdentity() {
    val seen = mutableSetOf<String>()
    screen.components.forEachIndexed { index, node ->
      val path = componentsPath.child(index)
      when {
        node.id.isEmpty() -> sink.add(path.child("id"), "component is missing an id")
        !IDENTIFIER.matches(node.id) ->
          sink.add(
            path.child("id"),
            "id '${node.id}' must be an identifier matching ${IDENTIFIER.pattern}",
          )
        !seen.add(node.id) -> sink.add(path.child("id"), "duplicate id '${node.id}'")
      }
      when {
        node.component.isEmpty() ->
          sink.add(path.child("component"), "component is missing its type")
        M3ExpressiveCatalog.find(node.component) == null ->
          sink.add(path.child("component"), "unknown component '${node.component}'")
      }
    }
    if (screen.components.none { it.id == ComponentNode.ROOT_ID }) {
      sink.add(componentsPath, "a screen needs exactly one component with id \"root\"")
    }
  }

  private fun validateProps(
    path: String,
    def: ComponentDef,
    props: JsonObject,
    overrides: Boolean,
  ) {
    val declared = (def.props + M3ExpressiveCatalog.commonProps).associateBy { it.name }
    for ((key, value) in props) {
      when {
        key == M3ExpressiveCatalog.SHOW_IN && !overrides -> validateShowIn(path.child(key), value)
        key == M3ExpressiveCatalog.SIZE_CLASS && !overrides ->
          validateSizeClass(path.child(key), def, value, props)
        key == M3ExpressiveCatalog.STYLE -> validateStyleName(path.child(key), value)
        else -> {
          val prop = declared[key]
          if (prop == null) {
            val known =
              (declared.keys +
                  M3ExpressiveCatalog.SHOW_IN +
                  M3ExpressiveCatalog.SIZE_CLASS +
                  M3ExpressiveCatalog.STYLE)
                .sorted()
                .joinToString()
            sink.add(path.child(key), "unknown prop '$key' on ${def.name} (known: $known)")
          } else {
            validateValue(path.child(key), prop, value)
          }
        }
      }
    }
    if (!overrides) {
      for (prop in def.props) {
        if (prop.required && prop.name !in props) {
          sink.add(path, "${def.name} requires prop '${prop.name}'")
        }
      }
      validateRelations(path, def, props, changed = null)
    }
  }

  /**
   * Checks props of one component against each other: a slider's range runs upward and holds its
   * value. [props] are the ones in effect (the size class overrides laid over the base props),
   * [changed] are the keys the object at [path] sets, or null for the base props. Only a check that
   * involves a changed key is reported, so one mistake is reported once, where it is written.
   */
  private fun validateRelations(
    path: String,
    def: ComponentDef,
    props: JsonObject,
    changed: Set<String>?,
  ) {
    fun touched(vararg keys: String) = changed == null || keys.any { it in changed }
    /** Where to report a problem between [first] and the range: the first if it is set here. */
    fun at(first: String) = path.child(if (touched(first)) first else "range")
    if (def.name != "Slider" && def.name != "RangeSlider") return
    val (min, max) = rangeOf(props) ?: return
    if (min >= max) {
      if (touched("range")) sink.add(path.child("range"), rangeOrderMessage(min, max))
      return
    }
    if (def.name == "Slider") {
      val value = props["value"]?.number() ?: return
      if (value !in min..max && touched("value", "range")) {
        sink.add(
          at("value"),
          "value ${format(value)} is outside the range [${format(min)}, ${format(max)}]",
        )
      }
    } else {
      val (start, end) = numbersOf(props["value"]) ?: return
      if (!touched("value", "range")) return
      when {
        start > end ->
          sink.add(path.child("value"), "start ${format(start)} must not exceed end ${format(end)}")
        start < min || end > max ->
          sink.add(
            at("value"),
            "value [${format(start)}, ${format(end)}] is outside the range [${format(min)}, ${format(max)}]",
          )
      }
    }
  }

  /** The `range` prop as two numbers; [0, 1] when absent; null when it is not two numbers. */
  private fun rangeOf(props: JsonObject): Pair<Double, Double>? =
    if ("range" !in props) 0.0 to 1.0 else numbersOf(props["range"])

  private fun numbersOf(value: JsonElement?): Pair<Double, Double>? {
    val array = value as? JsonArray ?: return null
    if (array.size != 2) return null
    val first = array[0].number() ?: return null
    val second = array[1].number() ?: return null
    return first to second
  }

  private fun rangeOrderMessage(min: Double, max: Double) =
    "range min must be less than max, got [${format(min)}, ${format(max)}]"

  private fun format(value: Double): String =
    if (value == Math.rint(value) && kotlin.math.abs(value) < 1e15) value.toLong().toString()
    else value.toString()

  private fun validateShowIn(path: String, value: JsonElement) {
    val array = value as? JsonArray
    if (array == null) {
      sink.add(path, "showIn must be an array of size classes")
      return
    }
    array.forEachIndexed { index, item ->
      val name = item.asString()
      if (name == null || SizeClass.fromWire(name) == null) {
        sink.add(
          path.child(index),
          "'${name ?: item}' is not a size class (compact, medium, expanded)",
        )
      }
    }
  }

  private fun validateSizeClass(
    path: String,
    def: ComponentDef,
    value: JsonElement,
    base: JsonObject,
  ) {
    val obj = value as? JsonObject
    if (obj == null) {
      sink.add(path, "sizeClass must be an object keyed by size class")
      return
    }
    for ((key, overrides) in obj) {
      val size = SizeClass.fromWire(key)
      if (size == null || size == SizeClass.Compact) {
        sink.add(path.child(key), "'$key' is not an overridable size class (medium, expanded)")
        continue
      }
      val props = overrides as? JsonObject
      if (props == null) {
        sink.add(path.child(key), "a size class override must be an object of props")
        continue
      }
      validateProps(path.child(key), def, props, overrides = true)
      // The props at this size are the base ones with the overrides up to it laid on top.
      val effective =
        LinkedHashMap(base).apply {
          putAll(ComponentNode("", def.name, base).sizeOverridesAt(size))
        }
      validateRelations(path.child(key), def, JsonObject(effective), changed = props.keys)
    }
  }

  private fun validateStyleName(path: String, value: JsonElement) {
    val name = value.asString()
    if (name == null || name !in design.styles) {
      sink.add(path, "style '${name ?: value}' is not defined in material.styles")
    }
  }

  private fun validateValue(path: String, prop: PropDef, value: JsonElement) {
    when (val kind = prop.kind) {
      PropKind.Child -> validateReference(path, prop, value)
      PropKind.ChildList -> validateChildList(path, prop, value)
      PropKind.Text,
      PropKind.Icon,
      PropKind.Image -> validateText(path, kind, value)
      PropKind.Bool ->
        if (!value.isBoolean() && !isDynamic(value)) sink.add(path, "must be true or false")
      is PropKind.Number -> validateNumber(path, kind, value)
      PropKind.NumberRange -> validateRange(path, value)
      is PropKind.Enum ->
        if (value.asString() !in kind.values) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not one of: ${kind.values.joinToString()}",
          )
        }
      PropKind.ColorRole ->
        if (value.asString()?.let(ColorRole::fromWire) == null) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a color role (such as primaryContainer)",
          )
        }
      PropKind.TypeRole ->
        if (value.asString()?.let(TypeRole::fromWire) == null) {
          sink.add(path, "'${value.asString() ?: value}' is not a type role (such as bodyLarge)")
        }
      PropKind.Shape ->
        if (value.asString()?.let(ThemeValidator::isShape) != true) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a shape token (such as large) or a MaterialShapes name",
          )
        }
      PropKind.Spacing ->
        if (value.asString() !in design.spacing) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a spacing name: ${design.spacing.keys.joinToString()}",
          )
        }
      PropKind.Action -> validateAction(path, value)
      PropKind.Binding ->
        if (!Dynamic.isBinding(value))
          sink.add(path, "must be a binding such as {\"@path\": \"/items\"}")
    }
  }

  private fun validateText(path: String, kind: PropKind, value: JsonElement) {
    val text = value.asString()
    when {
      text == null && !isDynamic(value) -> sink.add(path, "must be a string or a binding")
      text != null && kind == PropKind.Icon && !ICON_NAME.matches(text) ->
        sink.add(
          path,
          "'$text' is not a Material Symbols name (lowercase letters, digits and underscores)",
        )
      text != null && kind == PropKind.Image && text.isEmpty() ->
        sink.add(path, "must not be empty")
    }
  }

  private fun validateNumber(path: String, kind: PropKind.Number, value: JsonElement) {
    if (isDynamic(value)) return
    val number = value.number()
    val bounds = kind.bounds
    when {
      number == null -> sink.add(path, "must be a number")
      bounds != null && number !in bounds ->
        sink.add(path, "must be ${bounds.description}, got ${format(number)}")
    }
  }

  private fun validateRange(path: String, value: JsonElement) {
    val array = value as? JsonArray
    if (array == null || array.size != 2 || !array.all { it.isNumber() }) {
      sink.add(path, "must be an array of two numbers")
    }
  }

  private fun validateReference(path: String, prop: PropDef, value: JsonElement) {
    val id = value.asString()
    if (id == null) {
      sink.add(path, "must be the id of a component")
      return
    }
    val target = byId[id]
    val allowed = prop.allowedChildren
    when {
      target == null -> sink.add(path, "references unknown component '$id'")
      allowed != null && target.component !in allowed ->
        sink.add(
          path,
          "'$id' is a ${target.component}, but ${prop.name} takes ${allowed.joinToString(" or ")}",
        )
    }
  }

  private fun validateChildList(path: String, prop: PropDef, value: JsonElement) {
    when (value) {
      is JsonArray ->
        value.forEachIndexed { index, item -> validateReference(path.child(index), prop, item) }
      is JsonObject -> {
        validateReference(
          path.child("componentId"),
          prop,
          value["componentId"] ?: JsonObject(emptyMap()),
        )
        if (value.string("path") == null)
          sink.add(path.child("path"), "a template needs a data model path")
        value.keys
          .filter { it != "componentId" && it != "path" }
          .forEach {
            sink.add(path.child(it), "unknown key '$it' in a child template")
          }
      }
      else -> sink.add(path, "must be an array of component ids or a template {componentId, path}")
    }
  }

  private fun validateAction(path: String, value: JsonElement) {
    val action = value as? JsonObject
    if (action == null) {
      sink.add(path, "must be an object {\"functionCall\": {...}}")
      return
    }
    if ("event" in action) {
      sink.add(
        path.child("event"),
        "event actions are not supported: the renderer never calls the agent; use functionCall",
      )
    }
    val call = action["functionCall"] as? JsonObject
    if (call == null) {
      if ("event" !in action) sink.add(path, "an action needs a functionCall")
      return
    }
    val callPath = path.child("functionCall")
    val name = call.string("@call")
    val function = name?.let(ActionFunction::fromWire)
    if (function == null) {
      sink.add(
        callPath.child("@call"),
        "unknown function '${name ?: ""}' (use ${ActionFunction.entries.joinToString { it.wire }})",
      )
      return
    }
    val argName = function.argument ?: return
    val argsPath = callPath.child("args").child(argName)
    val argValue = (call["args"] as? JsonObject)?.string(argName)
    if (argValue == null) {
      sink.add(argsPath, "${function.wire} needs the $argName as a string")
      return
    }
    when (function) {
      ActionFunction.Navigate ->
        if (argValue !in proposal.screens) sink.add(argsPath, "screen '$argValue' does not exist")
      ActionFunction.Open -> {
        val target = byId[argValue]
        when {
          target == null ->
            sink.add(argsPath, "overlay '$argValue' is not a component of this screen")
          target.component !in M3ExpressiveCatalog.overlayNames ->
            sink.add(
              argsPath,
              "'$argValue' is a ${target.component}; an overlay must be a BottomSheet, Dialog or Menu",
            )
        }
      }
      ActionFunction.Back,
      ActionFunction.Dismiss -> Unit
    }
  }

  /**
   * A component that contains itself would make the renderer recurse forever. A `Menu.anchor` names
   * the component the menu hangs off; it is a reference, not a child, so it never forms a cycle.
   */
  private fun detectCycles() {
    val childIds = screen.components.associate { node -> node.id to referencedIds(node) }
    val done = mutableSetOf<String>()
    val reported = mutableSetOf<String>()
    fun visit(id: String, stack: List<String>) {
      if (id in done) return
      val onStack = stack.indexOf(id)
      if (onStack >= 0) {
        val current = stack.last()
        if (reported.add(current)) {
          val index = screen.components.indexOfFirst { it.id == current }
          sink.add(
            componentsPath.child(index),
            "cycle: ${(stack.drop(onStack) + id).joinToString(" -> ")}",
          )
        }
        return
      }
      for (next in childIds[id].orEmpty()) visit(next, stack + id)
      done += id
    }
    screen.components.forEach { visit(it.id, emptyList()) }
  }

  private fun referencedIds(node: ComponentNode): List<String> {
    val def = M3ExpressiveCatalog.find(node.component) ?: return emptyList()
    val sources = buildList {
      add(node.props)
      (node.props[M3ExpressiveCatalog.SIZE_CLASS] as? JsonObject)?.values?.forEach {
        (it as? JsonObject)?.let(::add)
      }
    }
    return sources
      .flatMap { props ->
        def.props.flatMap { prop ->
          if (node.component == MENU && prop.name == ANCHOR) return@flatMap emptyList()
          val value = props[prop.name] ?: return@flatMap emptyList()
          when (prop.kind) {
            PropKind.Child -> listOfNotNull(value.asString())
            PropKind.ChildList ->
              when (value) {
                is JsonArray -> value.mapNotNull { it.asString() }
                is JsonObject -> listOfNotNull(value.string("componentId"))
                else -> emptyList()
              }
            else -> emptyList()
          }
        }
      }
      .filter { it in byId }
  }

  companion object {
    val IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    private val ICON_NAME = Regex("^[a-z0-9_]+$")
    private const val MENU = "Menu"
    private const val ANCHOR = "anchor"
  }
}
