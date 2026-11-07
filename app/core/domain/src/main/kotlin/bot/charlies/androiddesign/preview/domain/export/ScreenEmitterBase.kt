package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.domain.node.ChildGraph
import bot.charlies.androiddesign.preview.domain.node.PropReader
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Where an emitter is in the template loops, which decides what a relative binding reads. */
internal interface Scoped {
  val scope: Scope?
}

/**
 * What a phone and a Wear screen emitter share, read from the [catalog] of the proposal: the
 * screen's components and overlays, the navigation and actions its components call, the state it
 * declares, the data model with the bindings and templates over it, the typed reads of a
 * component's props and the modifier imports.
 */
internal abstract class ScreenEmitterBase(
  val file: FileContext,
  val design: DesignSystem,
  val screenId: String,
  val screen: Screen,
  val data: ScreenData,
  protected val catalog: Catalog,
) {
  val byId: Map<String, ComponentNode> = screen.components.associateBy { it.id }

  /** The overlay components (sheets, dialogs, menus, snackbars), in document order. */
  val overlays: List<ComponentNode> =
    screen.components.filter { it.component in catalog.overlayNames }

  val functionName: String = Naming.pascal(screenId) + "Screen"

  /** Screens this screen navigates to. */
  val navigateTargets: List<String> =
    screen.components.flatMap { NavigationGraph.navigateTargets(it, catalog) }.distinct()

  val usesBack: Boolean = screen.components.any { node -> calls(node).any { it.name == "back" } }

  /** The overlay each component sits in, so `dismiss()` knows which one to close. */
  private val overlayOf: Map<String, String> = buildOverlayMap()

  fun callbackName(target: String) = "onNavigateTo${Naming.pascal(target)}"

  // region properties

  fun JsonObject.literal(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  /** These props of [component], read with its catalog defaults and bounds. */
  fun JsonObject.reader(component: String): PropReader = PropReader(def(component), ::get)

  private fun def(component: String) =
    checkNotNull(catalog.find(component)) { "$component is not a ${catalog.key} component" }

  /** An enum prop of [component]: the stated value, else its catalog default. */
  fun JsonObject.choice(component: String, key: String): String = reader(component).enum(key)

  fun JsonObject.flag(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toBooleanStrictOrNull()

  protected fun catalogDefault(component: String, key: String): String? =
    catalog.find(component)?.defaultOf(key)?.content

  // endregion

  // region sample data

  /** The `private object` that holds this screen's sample values, or null without a dataModel. */
  fun sampleDeclaration(): Decl? =
    if (data.params.isEmpty()) null
    else
      ObjectDecl(
        "private ",
        data.objectName,
        data.params.map { ValDecl("", it.name, it.sample) },
      )

  /** The screen function's parameters: the modifier, the data model, the callbacks. */
  fun parameters(): List<String> = buildList {
    add("modifier: ${file.use(Sym.Modifier)} = ${file.use(Sym.Modifier)}")
    data.params.forEach { add("${it.name}: ${it.type} = ${data.objectName}.${it.name}") }
    if (usesBack) add("onBack: () -> Unit = {}")
    navigateTargets.forEach { add("${callbackName(it)}: () -> Unit = {}") }
  }

  // endregion

  // region data bindings

  /**
   * The value prop [key] has in the sample data, as the previewer resolves it: a literal as is, a
   * binding as its sample value (null when nothing is there), and the loop index as a number whose
   * value no width rule reads. A template body is written once, so a binding inside it reads the
   * sample of the first item.
   */
  fun JsonObject.sampleValue(key: String, place: Scoped): JsonElement? {
    val element = this[key] ?: return null
    if (isIndex(element)) return place.scope?.let { JsonPrimitive(0) }
    val path = bindingPath(element) ?: return element
    return resolve(path, place.scope).sample
  }

  /** A flag that picks the Compose call or the modifier: its [sampleValue] as a boolean. */
  fun JsonObject.sampleFlag(key: String, place: Scoped): Boolean? =
    (sampleValue(key, place) as? JsonPrimitive)?.content?.toBooleanStrictOrNull()

  /**
   * A number of [component] that picks the Compose call: its [sampleValue] as the previewer reads
   * it, a finite `Float` moved into the catalog's bounds
   * ([bot.charlies.androiddesign.preview.model.ComponentDef.coerceNumber]).
   */
  fun JsonObject.sampleNumber(component: String, key: String, place: Scoped): Double? =
    sampleReader(component, place).number(key)

  /** These props of [component] read with their sample values, as the previewer reads them. */
  fun JsonObject.sampleReader(component: String, place: Scoped): PropReader =
    PropReader(def(component)) { sampleValue(it, place) }

  /** A string prop as a Kotlin expression: a literal, a bound value, or the loop index. */
  fun JsonObject.text(key: String, place: Scoped): Expr? = textOf(this[key], place)

  fun textOf(element: JsonElement?, place: Scoped): Expr? =
    when {
      element == null || element is JsonNull -> null
      element is JsonPrimitive -> Raw(kotlinString(element.content))
      isIndex(element) -> Raw(place.scope?.index?.let { "$it.toString()" } ?: "\"0\"")
      else -> bindingPath(element)?.let { Raw(boundString(it, place.scope)) }
    }

  /** A bound value as a `String` expression: a number or flag gets `.toString()`. */
  private fun boundString(path: String, scope: Scope?): String {
    val bound = resolve(path, scope)
    return if (bound.sample == null || bound.isString) bound.expr else "${bound.expr}.toString()"
  }

  /** A string prop bound to data: the Kotlin [expr] and every non-blank value it takes. */
  class BoundText(val expr: String, val values: Set<String>)

  /** [key] as a [BoundText] with the values it takes across the sample data, or null if unbound. */
  fun JsonObject.boundText(key: String, place: Scoped): BoundText? {
    val path = this[key]?.let(::bindingPath) ?: return null
    val values =
      resolveAll(path, place.scope).mapNotNullTo(linkedSetOf()) {
        (it as? JsonPrimitive)?.content?.takeIf(String::isNotBlank)
      }
    return BoundText(boundString(path, place.scope), values)
  }

  /** The Material Symbol name of [key]: a binding, else its literal, or null when it is absent. */
  fun JsonObject.iconName(key: String, place: Scoped): IconName? =
    boundText(key, place)?.let { IconName.Bound(it.expr, it.values) }
      ?: literal(key)?.let { IconName.Literal(it) }

  /** A boolean prop: its literal, or the bound value. */
  fun JsonObject.boolExpr(key: String, place: Scoped): String? {
    val element = this[key] ?: return null
    flag(key)?.let {
      return it.toString()
    }
    return bindingPath(element)?.let { resolve(it, place.scope).expr }
  }

  fun bindingPath(element: JsonElement): String? {
    val obj = element as? JsonObject ?: return null
    if (obj.keys != setOf("@path")) return null
    return (obj["@path"] as? JsonPrimitive)?.takeIf { it.isString }?.content
  }

  private fun isIndex(element: JsonElement): Boolean =
    (element as? JsonObject)?.get("@call")?.let { (it as? JsonPrimitive)?.content } == "@index"

  class Bound(val expr: String, val sample: JsonElement?) {
    val isString: Boolean
      get() = (sample as? JsonPrimitive)?.isString == true
  }

  /** `/plate/name` is `plate.name`; a relative `name` reads the innermost template item. */
  fun resolve(path: String, scope: Scope?): Bound {
    val segments = path.trim('/').split('/').filter { it.isNotEmpty() }
    if (segments.isEmpty()) return Bound("\"\"", null)
    if (path.startsWith("/") || scope == null) {
      val root = data.root?.get(segments.first()) ?: return Bound("\"\"", null)
      return walk(Naming.camel(segments.first()), root, segments.drop(1))
    }
    return walk(scope.variable, scope.sample, segments)
  }

  /**
   * Every value [path] takes in the sample data: one for an absolute path or a path outside a
   * template, else one per item of the innermost template that has something at [path].
   */
  fun resolveAll(path: String, scope: Scope?): List<JsonElement> {
    val segments = path.trim('/').split('/').filter { it.isNotEmpty() }
    if (segments.isEmpty()) return emptyList()
    if (path.startsWith("/") || scope == null) {
      val root = data.root?.get(segments.first()) ?: return emptyList()
      return listOfNotNull(valueAt(root, segments.drop(1)))
    }
    return scope.items.mapNotNull { valueAt(it, segments) }
  }

  private fun valueAt(start: JsonElement, segments: List<String>): JsonElement? =
    segments.fold<String, JsonElement?>(start) { current, segment ->
      val index = segment.toIntOrNull()
      if (index != null && current is JsonArray) current.getOrNull(index)
      else (current as? JsonObject)?.get(segment)
    }

  private fun walk(start: String, sample: JsonElement?, segments: List<String>): Bound {
    var expr = start
    var current = sample
    for (segment in segments) {
      val index = segment.toIntOrNull()
      if (index != null && current is JsonArray) {
        expr += "[$index]"
        current = current.getOrNull(index)
      } else {
        expr += "." + Naming.camel(segment)
        current = (current as? JsonObject)?.get(segment)
      }
    }
    return Bound(expr, current)
  }

  /** A template's loop variable and index over the array at its path. */
  class TemplateScope(val list: Bound, val scope: Scope, val withIndex: Boolean)

  fun templateScope(
    componentId: String,
    path: String,
    place: Scoped,
    indexed: Boolean,
  ): TemplateScope {
    val list = resolve(path, place.scope)
    val items = resolveAll(path, place.scope).flatMap { (it as? JsonArray).orEmpty() }
    val taken = place.scope?.names().orEmpty() + data.params.map { it.name }
    val base = Naming.camel(Naming.singular(path.trim('/').substringAfterLast('/'))).trim('`')
    val variable =
      generateSequence(0) { it + 1 }
        .map { if (it == 0) base else "item${if (it == 1) "" else it}" }
        .first { it !in taken }
    val withIndex = indexed || usesIndex(componentId)
    val depth = (place.scope?.depth() ?: 0) + 1
    val index = if (withIndex) (if (depth == 1) "index" else "index$depth") else null
    return TemplateScope(list, Scope(place.scope, variable, index, items), withIndex)
  }

  /** True when [id] or anything under it shows the template index. */
  private fun usesIndex(id: String, seen: MutableSet<String> = mutableSetOf()): Boolean {
    if (!seen.add(id)) return false
    val node = byId[id] ?: return false
    if (node.props.values.any { containsIndex(it) }) return true
    return childIds(node).any { usesIndex(it, seen) }
  }

  private fun containsIndex(element: JsonElement): Boolean =
    when (element) {
      is JsonObject -> isIndex(element) || element.values.any(::containsIndex)
      is JsonArray -> element.any(::containsIndex)
      else -> false
    }

  // endregion

  // region icons

  /** The drawable [name] is: `R.drawable.star`, or a call of the lookup for a bound name. */
  fun drawable(name: IconName, filled: Boolean = false): String =
    when (name) {
      is IconName.Literal -> file.drawable(Naming.snake(name.name) + if (filled) "_filled" else "")
      is IconName.Bound -> "${file.symbolLookup(name.names, filled)}(${name.expr})"
    }

  fun painter(name: IconName, filled: Boolean = false): String =
    "${file.use(Sym.painterResource)}(${drawable(name, filled)})"

  /**
   * The drawable of an image: named after the file, or after the node for a `data:` URI. A bound
   * `src` is a call of the lookup over every source the sample data gives it; when several distinct
   * data URIs occur, the node's name is numbered in the order they first appear. With [asImages]
   * the drawables are listed as images, not Material Symbols.
   */
  fun imageDrawable(
    node: ComponentNode,
    p: JsonObject,
    place: Scoped,
    key: String = "src",
    asImages: Boolean = false,
  ): String {
    fun symbol(src: String) =
      if (src.startsWith("data:")) node.id else src.substringAfterLast('/').substringBeforeLast('.')
    val bound =
      p.boundText(key, place)
        ?: Naming.snake(symbol(p.literal(key).orEmpty())).let {
          return if (asImages) file.image(it) else file.drawable(it)
        }
    val data = bound.values.filter { it.startsWith("data:") }
    val symbols =
      bound.values.associateWith { src ->
        if (src.startsWith("data:") && data.size > 1) "${node.id}_${data.indexOf(src) + 1}"
        else symbol(src)
      }
    return "${file.imageLookup(symbols, asImages)}(${bound.expr})"
  }

  // endregion

  // region tokens

  /** A spacing token as dp: `lg` is `16.dp` when the design system says 16. */
  fun spacing(name: String): String = dp(design.spacing[name] ?: 0f)

  fun dp(value: Float): String = "${number(value)}.${file.use(Sym.dp)}"

  // endregion

  // region modifiers

  /** Imports the extension behind a modifier call written as `name(...)` and returns the call. */
  fun useModifier(call: String): String {
    val name = call.substringBefore('(')
    MODIFIER_IMPORTS[name]?.let { file.use(it) }
    return call
  }

  /**
   * The height a `FlexBox` item is measured with, unbounded again. FlexBox hands a wrapped item the
   * largest height `Constraints` can hold, and a Material component that grows its semantics bounds
   * (material3's `IncreaseVerticalSemanticsBounds`: linear progress indicators and vertical
   * sliders) adds to that and throws. The previewer and the exporter restore the height for the
   * same items, those [growsSemanticsBounds] names; the others stay plain.
   */
  fun unboundedHeight(): String {
    file.use(Sym.Dp)
    return useModifier("requiredHeightIn(max = Dp.Infinity)")
  }

  // endregion

  // region actions

  class ActionCall(val name: String, val args: JsonObject?)

  /** Every `functionCall` in the action props of [node], size class overrides included. */
  fun calls(node: ComponentNode): List<ActionCall> {
    val def = catalog.find(node.component) ?: return emptyList()
    val names = def.props.filter { it.kind == PropKind.Action }.map { it.name }
    val sources = buildList {
      add(node.props)
      (node.props[Catalog.SIZE_CLASS] as? JsonObject)?.values?.forEach {
        (it as? JsonObject)?.let(::add)
      }
    }
    return sources.flatMap { source -> names.mapNotNull { callOf(source[it]) } }
  }

  private fun callOf(action: JsonElement?): ActionCall? {
    val call = (action as? JsonObject)?.get("functionCall") as? JsonObject ?: return null
    val name = (call["@call"] as? JsonPrimitive)?.content ?: return null
    return ActionCall(name, call["args"] as? JsonObject)
  }

  /** The statements an `action` runs; [componentId] is where it sits, for `dismiss()`. */
  fun actionStatements(action: JsonElement?, componentId: String): List<Stmt> {
    val call = callOf(action) ?: return emptyList()
    return when (ActionFunction.fromWire(call.name)) {
      ActionFunction.Navigate -> {
        val target = (call.args?.get("screen") as? JsonPrimitive)?.content ?: return emptyList()
        listOf(stmt(Call(callbackName(target))))
      }
      ActionFunction.Back -> listOf(stmt(Call("onBack")))
      ActionFunction.Open -> {
        val overlay = (call.args?.get("overlay") as? JsonPrimitive)?.content ?: return emptyList()
        listOf(openOverlay(overlay))
      }
      ActionFunction.Dismiss -> {
        val enclosing = overlayOf[componentId]
        val targets = if (enclosing != null) listOf(enclosing) else overlays.map { it.id }
        targets.map(::closeOverlay)
      }
      null -> emptyList()
    }
  }

  /** `{ ... }` for an action: the statements, or an empty lambda. */
  fun actionLambda(action: JsonElement?, componentId: String): Lambda =
    lambda(actionStatements(action, componentId))

  fun overlayVar(id: String) = "show${Naming.pascal(id)}"

  /** The id of the overlay that holds [componentId], or null when it is in none. */
  fun enclosingOverlay(componentId: String): String? = overlayOf[componentId]

  /** What `open(id)` does: raise the overlay's flag. */
  protected open fun openOverlay(id: String): Stmt = Assign(overlayVar(id), "=", Raw("true"))

  /** What `dismiss()` does to overlay [id]: lower its flag. */
  protected open fun closeOverlay(id: String): Stmt = Assign(overlayVar(id), "=", Raw("false"))

  /**
   * `var name by remember { mutableStateOf(value) }`; [factory] is `mutableStateOf`,
   * `mutableFloatStateOf` or `mutableIntStateOf`.
   */
  fun stateVar(
    name: String,
    value: String,
    factory: String = Sym.mutableStateOf,
    key: String? = null,
  ): Stmt {
    file.use(Sym.getValue)
    file.use(Sym.setValue)
    val state = file.use(factory)
    return Assign(
      "var $name",
      "by",
      Call(
        file.use(Sym.remember),
        listOfNotNull(key?.let(::pos)),
        lambda(stmt(Raw("$state($value)"))),
      ),
    )
  }

  fun stateName(id: String, suffix: String) = Naming.camel(id).trim('`') + suffix

  private fun buildOverlayMap(): Map<String, String> {
    val map = linkedMapOf<String, String>()
    for (overlay in screen.components.filter { it.component in catalog.overlayNames }) {
      fun visit(id: String) {
        if (id in map || id == overlay.id) return
        map[id] = overlay.id
        val node = byId[id] ?: return
        childIds(node).forEach(::visit)
      }
      childIds(overlay).forEach(::visit)
    }
    return map
  }

  /** Ids of the components [node] references through child props, templates included. */
  fun childIds(node: ComponentNode): List<String> = ChildGraph.childIds(node, catalog)

  // endregion

  private companion object {
    val MODIFIER_IMPORTS =
      mapOf(
        "padding" to Sym.padding,
        "fillMaxSize" to Sym.fillMaxSize,
        "fillMaxWidth" to Sym.fillMaxWidth,
        "fillMaxHeight" to Sym.fillMaxHeight,
        "width" to Sym.width,
        "height" to Sym.height,
        "size" to Sym.size,
        "heightIn" to Sym.heightIn,
        "requiredHeightIn" to Sym.requiredHeightIn,
        "widthIn" to Sym.widthIn,
        "offset" to Sym.offset,
        "aspectRatio" to Sym.aspectRatio,
        "background" to Sym.background,
        "clickable" to Sym.clickable,
        "verticalScroll" to Sym.verticalScroll,
        "clip" to Sym.clip,
        "nestedScroll" to Sym.nestedScroll,
        "semantics" to Sym.semantics,
        "selectable" to Sym.selectable,
        "selectableGroup" to Sym.selectableGroup,
      )
  }
}
