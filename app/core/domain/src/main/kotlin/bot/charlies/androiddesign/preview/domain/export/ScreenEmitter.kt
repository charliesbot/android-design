package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The key under which a named style's `container` role is kept among a component's props. */
internal const val CONTAINER_KEY = "\u0000container"

/**
 * The key under which a named style's `shape` is kept for a component without a `shape` prop, such
 * as a layout, whose container the previewer clips to it.
 */
internal const val STYLE_SHAPE_KEY = "\u0000shape"

/**
 * A template loop variable: `plates.forEach { plate -> ... }` makes `plate` the scope. [items] are
 * the elements the loop runs over in the sample data, across every item of the enclosing loops.
 */
internal class Scope(
  val parent: Scope?,
  val variable: String,
  val index: String?,
  val items: List<JsonElement>,
) {
  /** The first item, which a template body's bindings are written against. */
  val sample: JsonElement?
    get() = items.firstOrNull()

  fun names(): Set<String> =
    generateSequence(this) { it.parent }
      .flatMap { sequenceOf(it.variable, it.index.orEmpty()) }
      .toSet()

  fun depth(): Int = generateSequence(this) { it.parent }.count()
}

/**
 * Where the segmented `List` that holds a `ListItem` is: the item's position and the item count.
 */
internal class ListPosition(val segmented: Boolean, val index: String, val count: String)

/** A `ChipGroup` with single selection: one state variable holds the selected chip id. */
internal class ChipSelection(val state: String)

/**
 * A Material Symbol name: written in the proposal, or bound to data. A bound name is a Kotlin
 * [expr] and every name it takes across the sample data, which the file's `symbolDrawable` helper
 * maps to drawables.
 */
internal sealed interface IconName {
  class Literal(val name: String) : IconName

  class Bound(val expr: String, val names: Set<String>) : IconName
}

/** What a component needs to know about where it is emitted. */
internal data class Place(
  val scope: Scope? = null,
  /** The component type of the parent, which decides whether `weight` applies. */
  val parentType: String? = null,
  /**
   * True when the parent lets its children stretch to its width (the catalog's
   * [bot.charlies.androiddesign.preview.model.ComponentDef.stretchesChildren]). The screen's root
   * counts as stretched, as it does in the previewer.
   */
  val parentStretches: Boolean = true,
  /**
   * True when the component drawn here stretches to the parent's width: it gets `fillMaxWidth()`.
   */
  val fillWidth: Boolean = false,
  /**
   * True for a child of a row whose `verticalAlignment` is `stretch`. A Column, Row, or Box here
   * gets `fillMaxHeight()`; every other component ignores it.
   */
  val fillHeight: Boolean = false,
  /** `Modifier`, or `modifier` for the screen's root. */
  val modifierBase: String = "Modifier",
  /** Modifier calls to put first, such as `padding(innerPadding)`. */
  val leading: List<String> = emptyList(),
  val listPosition: ListPosition? = null,
  val railExpanded: Boolean = false,
  /** `ButtonGroupDefaults.connectedMiddleButtonShapes()` for a child of a connected group. */
  val groupShapes: String? = null,
  val chipSelection: ChipSelection? = null,
  /** True for a floating toolbar placed over content in a `Box`. */
  val floating: Boolean = false,
  val scrollBehavior: Boolean = false,
) {
  fun forChild(parentType: String) =
    copy(
      parentType = parentType,
      parentStretches = M3ExpressiveCatalog.find(parentType)?.stretchesChildren ?: false,
      fillWidth = false,
      fillHeight = false,
      modifierBase = "Modifier",
      leading = emptyList(),
      listPosition = null,
      groupShapes = null,
      chipSelection = null,
      floating = false,
      scrollBehavior = false,
    )
}

/**
 * Writes one screen as a `@Composable` function. The per-component calls live in
 * `ComponentEmitters.kt`; this class holds what they share: properties, bindings, actions,
 * modifiers, tokens and children.
 */
internal class ScreenEmitter(
  val file: FileContext,
  val design: DesignSystem,
  val screenId: String,
  val screen: Screen,
  val data: ScreenData,
  private val themeName: String,
) {
  val byId: Map<String, ComponentNode> = screen.components.associateBy { it.id }

  /** BottomSheet, Dialog and Menu components, in document order. */
  val overlays: List<ComponentNode> =
    screen.components.filter { it.component in M3ExpressiveCatalog.overlayNames }

  /** Which overlays are written beside a component and which at the end of the screen. */
  val placement = OverlayPlacement(screen)

  /**
   * Menus already written beside a component, so one drawn from several variants is not doubled.
   */
  val placedOverlays = mutableSetOf<String>()

  var usesSizeClass = false

  /** The overlay each component sits in, so `dismiss()` knows which one to close. */
  private val overlayOf: Map<String, String> = buildOverlayMap()

  /** Screens this screen navigates to. */
  val navigateTargets: List<String> =
    screen.components.flatMap { NavigationGraph.navigateTargets(it) }.distinct()

  val usesBack: Boolean = screen.components.any { node -> calls(node).any { it.name == "back" } }

  val functionName: String = Naming.pascal(screenId) + "Screen"

  // region the function

  /** The `private object` that holds this screen's sample values, or null without a dataModel. */
  fun sampleDeclaration(): Decl? = if (data.params.isEmpty()) null else sampleObject()

  /** The screen function and its `@Preview`. */
  fun declarations(): List<Decl> {
    val body = mutableListOf<Stmt>()
    body += overlays.map { overlayState(it) }
    val root = byId[ComponentNode.ROOT_ID]
    val rootStatements =
      if (root == null) emptyList() else emit(root.id, Place(modifierBase = "modifier"))
    val tail = placement.screenLevel.flatMap { overlayStatements(it) }
    if (usesSizeClass) body += sizeClassState()
    body += rootStatements
    body += tail
    file.use(Sym.Composable)
    return listOf(
      FunDecl(
        annotations = listOf("@Composable"),
        modifiers = "",
        name = functionName,
        params = parameters(),
        body = body,
      ),
      preview(),
    )
  }

  private fun parameters(): List<String> = buildList {
    add("modifier: ${file.use(Sym.Modifier)} = ${file.use(Sym.Modifier)}")
    data.params.forEach { add("${it.name}: ${it.type} = ${data.objectName}.${it.name}") }
    if (usesBack) add("onBack: () -> Unit = {}")
    navigateTargets.forEach { add("${callbackName(it)}: () -> Unit = {}") }
  }

  fun callbackName(target: String) = "onNavigateTo${Naming.pascal(target)}"

  private fun sampleObject(): Decl =
    ObjectDecl(
      "private ",
      data.objectName,
      data.params.map { ValDecl("", it.name, it.sample) },
    )

  private fun preview(): Decl {
    file.use(Sym.Preview)
    file.use(Sym.Composable)
    return FunDecl(
      annotations = listOf("@Preview(showBackground = true)", "@Composable"),
      modifiers = "private ",
      name = "${functionName}Preview",
      params = emptyList(),
      body =
        listOf(
          stmt(Call(themeName, lambda = lambda(stmt(Call(functionName))))),
        ),
    )
  }

  private fun sizeClassState(): List<Stmt> {
    file.optIn(Sym.ExperimentalMaterial3AdaptiveApi)
    val windowSizeClass = file.use(Sym.WindowSizeClass)
    return listOf(
      Assign(
        "val windowSizeClass",
        "=",
        Raw("${file.use(Sym.currentWindowAdaptiveInfoV2)}().windowSizeClass"),
      ),
      Assign(
        "val atLeastMedium",
        "=",
        Raw(
          "windowSizeClass.isWidthAtLeastBreakpoint($windowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)",
        ),
      ),
      Assign(
        "val atLeastExpanded",
        "=",
        Raw(
          "windowSizeClass.isWidthAtLeastBreakpoint($windowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)",
        ),
      ),
    )
  }

  // endregion

  // region components

  /** The statements that draw component [id] in [place], or none when it is not in this screen. */
  fun emit(id: String, place: Place): List<Stmt> {
    val node = byId[id] ?: return emptyList()
    val visible = linkedMapOf<JsonObject, MutableSet<SizeClass>>()
    for (size in SizeClass.entries) {
      if (size !in showIn(node)) continue
      visible.getOrPut(effectiveProps(node, size)) { linkedSetOf() } += size
    }
    if (visible.isEmpty()) return emptyList()
    val single = visible.entries.singleOrNull()
    if (single != null && single.value.size == SizeClass.entries.size) {
      return emitComponent(node, single.key, place)
    }
    usesSizeClass = true
    return listOf(
      IfStmt(
        visible.map { (props, sizes) -> condition(sizes) to emitComponent(node, props, place) },
        null,
      ),
    )
  }

  private fun condition(sizes: Set<SizeClass>): String {
    val medium = SizeClass.Medium in sizes
    val compact = SizeClass.Compact in sizes
    val expanded = SizeClass.Expanded in sizes
    return when {
      compact && medium && expanded -> "true"
      compact && medium -> "!atLeastExpanded"
      medium && expanded -> "atLeastMedium"
      compact && expanded -> "!atLeastMedium || atLeastExpanded"
      compact -> "!atLeastMedium"
      medium -> "atLeastMedium && !atLeastExpanded"
      else -> "atLeastExpanded"
    }
  }

  private fun showIn(node: ComponentNode): Set<SizeClass> {
    val listed = (node.props[M3ExpressiveCatalog.SHOW_IN] as? JsonArray)
    if (listed == null) return SizeClass.entries.toSet()
    return listed.mapNotNull { (it as? JsonPrimitive)?.content?.let(SizeClass::fromWire) }.toSet()
  }

  /**
   * The props [node] has at [size]: its own props, a named style filled in underneath, and the size
   * class overrides on top ([ComponentNode.sizeOverridesAt]). Explicit props win over the style.
   */
  fun effectiveProps(node: ComponentNode, size: SizeClass): JsonObject {
    val merged = linkedMapOf<String, JsonElement>()
    val def = M3ExpressiveCatalog.find(node.component)
    val styleName = (node.props[M3ExpressiveCatalog.STYLE] as? JsonPrimitive)?.content
    val style = styleName?.let { design.styles[it] }
    if (style != null && def != null) {
      fun fill(key: String, value: String?, kind: PropKind?) {
        if (value != null && (key == "padding" || (kind != null && def.prop(key)?.kind == kind))) {
          merged[key] = JsonPrimitive(value)
        }
      }
      fill("type", style.type, PropKind.TypeRole)
      fill("color", style.color, PropKind.ColorRole)
      fill("shape", style.shape, PropKind.Shape)
      fill("padding", style.padding, null)
      style.container?.let { merged[CONTAINER_KEY] = JsonPrimitive(it) }
      if (def.prop("shape") == null)
        style.shape?.let { merged[STYLE_SHAPE_KEY] = JsonPrimitive(it) }
    }
    node.props.forEach { (key, value) ->
      if (
        key != M3ExpressiveCatalog.SHOW_IN &&
          key != M3ExpressiveCatalog.SIZE_CLASS &&
          key != M3ExpressiveCatalog.STYLE
      ) {
        merged[key] = value
      }
    }
    merged.putAll(node.sizeOverridesAt(size))
    return JsonObject(merged)
  }

  // endregion

  // region properties

  fun JsonObject.literal(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  /** An enum prop of [component]: the stated value, else its catalog default. */
  fun JsonObject.choice(component: String, key: String): String =
    literal(key)
      ?: checkNotNull(catalogDefault(component, key)) { "$component.$key has no default" }

  /**
   * An enum prop of [component] the proposal states with a value other than its catalog default,
   * else null: Compose needs code only for a choice that is not its own default.
   */
  fun JsonObject.nonDefault(component: String, key: String): String? =
    literal(key)?.takeIf { it != catalogDefault(component, key) }

  /** A boolean prop of [component]: the stated literal, else its catalog default. */
  fun JsonObject.option(component: String, key: String): Boolean =
    flag(key)
      ?: checkNotNull(catalogDefault(component, key)?.toBooleanStrictOrNull()) {
        "$component.$key has no default"
      }

  private fun catalogDefault(component: String, key: String): String? =
    M3ExpressiveCatalog.find(component)?.defaultOf(key)?.content

  fun JsonObject.flag(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toBooleanStrictOrNull()

  fun JsonObject.number(key: String): Double? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toDoubleOrNull()

  /**
   * The value prop [key] has in the sample data, as the previewer resolves it: a literal as is, a
   * binding as its sample value (null when nothing is there), and the loop index as a number whose
   * value no width rule reads. A template body is written once, so a binding inside it reads the
   * sample of the first item.
   */
  fun JsonObject.sampleValue(key: String, place: Place): JsonElement? {
    val element = this[key] ?: return null
    if (isIndex(element)) return place.scope?.let { JsonPrimitive(0) }
    val path = bindingPath(element) ?: return element
    return resolve(path, place.scope).sample
  }

  /** A flag that picks the Compose call or the modifier: its [sampleValue] as a boolean. */
  fun JsonObject.sampleFlag(key: String, place: Place): Boolean? =
    (sampleValue(key, place) as? JsonPrimitive)?.content?.toBooleanStrictOrNull()

  /**
   * A number of [component] that picks the Compose call: its [sampleValue] as the previewer reads
   * it, a finite `Float` moved into the catalog's bounds
   * ([bot.charlies.androiddesign.preview.model.ComponentDef.coerceNumber]).
   */
  fun JsonObject.sampleNumber(component: String, key: String, place: Place): Double? {
    val value =
      (sampleValue(key, place) as? JsonPrimitive)?.content?.toDoubleOrNull()?.takeIf {
        it.isFinite()
      } ?: return null
    val def = M3ExpressiveCatalog.find(component) ?: return value
    return def.coerceNumber(key, value.toFloat().toDouble())
  }

  /** A string prop as a Kotlin expression: a literal, a bound value, or the loop index. */
  fun JsonObject.text(key: String, place: Place): Expr? = textOf(this[key], place)

  fun textOf(element: JsonElement?, place: Place): Expr? =
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
  fun JsonObject.boundText(key: String, place: Place): BoundText? {
    val path = this[key]?.let(::bindingPath) ?: return null
    val values =
      resolveAll(path, place.scope).mapNotNullTo(linkedSetOf()) {
        (it as? JsonPrimitive)?.content?.takeIf(String::isNotBlank)
      }
    return BoundText(boundString(path, place.scope), values)
  }

  /** The Material Symbol name of [key]: a binding, else its literal, or null when it is absent. */
  fun JsonObject.iconName(key: String, place: Place): IconName? =
    boundText(key, place)?.let { IconName.Bound(it.expr, it.values) }
      ?: literal(key)?.let { IconName.Literal(it) }

  /** A boolean prop: its literal, or the bound value. */
  fun JsonObject.boolExpr(key: String, place: Place): String? {
    val element = this[key] ?: return null
    flag(key)?.let {
      return it.toString()
    }
    return bindingPath(element)?.let { resolve(it, place.scope).expr }
  }

  /** A number prop as a `Float` expression. */
  fun JsonObject.floatExpr(key: String, place: Place): String? {
    val element = this[key] ?: return null
    number(key)?.let {
      return floatLiteral(it)
    }
    return bindingPath(element)?.let { "${resolve(it, place.scope).expr}.toFloat()" }
  }

  fun floatLiteral(value: Double): String = "${number(value.toFloat())}f"

  private fun bindingPath(element: JsonElement): String? {
    val obj = element as? JsonObject ?: return null
    if (obj.keys != setOf("@path")) return null
    return (obj["@path"] as? JsonPrimitive)?.takeIf { it.isString }?.content
  }

  private fun isIndex(element: JsonElement): Boolean =
    (element as? JsonObject)?.get("@call")?.let { (it as? JsonPrimitive)?.content } == "@index"

  // endregion

  // region bindings

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

  // endregion

  // region tokens

  fun colorRole(name: String): String = "${file.use(Sym.MaterialTheme)}.colorScheme.$name"

  fun typeRole(name: String): String = "${file.use(Sym.MaterialTheme)}.typography.$name"

  fun shapeToken(name: String): String {
    ShapeToken.fromWire(name)?.let { token ->
      return when (token) {
        ShapeToken.None -> file.use(Sym.RectangleShape)
        ShapeToken.Full -> file.use(Sym.CircleShape)
        else -> "${file.use(Sym.MaterialTheme)}.shapes.${token.wire}"
      }
    }
    MaterialShape.fromWire(name)?.let {
      return "${file.use(Sym.MaterialShapes)}.${it.name}.${file.use(Sym.toShape)}()"
    }
    return "${file.use(Sym.MaterialTheme)}.shapes.$name"
  }

  /** A spacing token as dp: `lg` is `16.dp` when the design system says 16. */
  fun spacing(name: String): String {
    val value = design.spacing[name] ?: 0f
    return dp(value)
  }

  fun dp(value: Float): String = "${number(value)}.${file.use(Sym.dp)}"

  // endregion

  // region modifiers

  /**
   * The `modifier` argument: the place's leading calls, the weight inside a row or column, the
   * stretch to the parent's width, the stretch to its height when [fillHeight] says so, any [extra]
   * calls, and the padding last. Null when there is nothing to say.
   */
  fun modifier(
    place: Place,
    props: JsonObject,
    vararg extra: String?,
    fillHeight: Boolean = false,
  ): Arg? {
    val parts = mutableListOf<String>()
    parts += place.leading.map(::useModifier)
    if (place.parentType == "Row" || place.parentType == "Column") {
      props.floatExpr("weight", place)?.let { parts += "weight($it)" }
    }
    if (place.fillWidth) parts += useModifier("fillMaxWidth()")
    if (fillHeight) parts += useModifier("fillMaxHeight()")
    parts += extra.filterNotNull().map(::useModifier)
    props.literal("padding")?.let { parts += "${useModifier("padding")}(${spacing(it)})" }
    return modifierArg(place, parts)
  }

  /** The `modifier = ...` argument for [parts], where each part is a call such as `size(40.dp)`. */
  fun modifierArg(place: Place, parts: List<String>): Arg? {
    if (parts.isEmpty())
      return if (place.modifierBase == "modifier") arg("modifier", "modifier") else null
    return arg("modifier", modifierChain(place, parts))
  }

  fun modifierChain(place: Place, parts: List<String>): Expr {
    val base = if (place.modifierBase == "modifier") "modifier" else file.use(Sym.Modifier)
    return Chain(base, parts)
  }

  /** Imports the extension behind a modifier call written as `name(...)` and returns the call. */
  fun useModifier(call: String): String {
    val name = call.substringBefore('(')
    MODIFIER_IMPORTS[name]?.let { file.use(it) }
    return call
  }

  // endregion

  // region actions

  class ActionCall(val name: String, val args: JsonObject?)

  /** Every `functionCall` in the action props of [node], size class overrides included. */
  fun calls(node: ComponentNode): List<ActionCall> {
    val def = M3ExpressiveCatalog.find(node.component) ?: return emptyList()
    val names = def.props.filter { it.kind == PropKind.Action }.map { it.name }
    val sources = buildList {
      add(node.props)
      (node.props[M3ExpressiveCatalog.SIZE_CLASS] as? JsonObject)?.values?.forEach {
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
        listOf(Assign(overlayVar(overlay), "=", Raw("true")))
      }
      ActionFunction.Dismiss -> {
        val enclosing = overlayOf[componentId]
        val targets = if (enclosing != null) listOf(enclosing) else overlays.map { it.id }
        targets.map { Assign(overlayVar(it), "=", Raw("false")) }
      }
      null -> emptyList()
    }
  }

  /** `{ ... }` for an action: the statements, or an empty lambda. */
  fun actionLambda(action: JsonElement?, componentId: String): Lambda =
    lambda(actionStatements(action, componentId))

  fun overlayVar(id: String) = "show${Naming.pascal(id)}"

  private fun overlayState(node: ComponentNode): Stmt = stateVar(overlayVar(node.id), "false")

  /**
   * `var name by remember { mutableStateOf(value) }`; [factory] is `mutableStateOf` or
   * `mutableFloatStateOf`.
   */
  fun stateVar(name: String, value: String, factory: String = Sym.mutableStateOf): Stmt {
    file.use(Sym.getValue)
    file.use(Sym.setValue)
    val state = file.use(factory)
    return Assign(
      "var $name",
      "by",
      Call(file.use(Sym.remember), lambda = lambda(stmt(Raw("$state($value)")))),
    )
  }

  fun stateName(id: String, suffix: String) = Naming.camel(id).trim('`') + suffix

  private fun buildOverlayMap(): Map<String, String> {
    val map = linkedMapOf<String, String>()
    for (overlay in screen.components.filter { it.component in M3ExpressiveCatalog.overlayNames }) {
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
  fun childIds(node: ComponentNode): List<String> {
    val def = M3ExpressiveCatalog.find(node.component) ?: return emptyList()
    return def.props.flatMap { prop ->
      when (val value = node.props[prop.name]) {
        is JsonPrimitive ->
          if (prop.kind == PropKind.Child && value.isString) listOf(value.content) else emptyList()
        is JsonArray ->
          if (prop.kind == PropKind.ChildList) value.mapNotNull { (it as? JsonPrimitive)?.content }
          else emptyList()
        is JsonObject ->
          if (prop.kind == PropKind.ChildList)
            listOfNotNull((value["componentId"] as? JsonPrimitive)?.content)
          else emptyList()
        else -> emptyList()
      }
    }
  }

  // endregion

  // region children

  /** The statements for a `Child` prop, or none. */
  fun child(props: JsonObject, key: String, place: Place, parentType: String): List<Stmt> {
    val id = props.literal(key) ?: return emptyList()
    return emit(id, place.forChild(parentType))
  }

  /** A slot lambda for a `Child` prop, such as `title = { ... }`; null when the prop is absent. */
  fun slot(props: JsonObject, key: String, place: Place, parentType: String): Lambda? {
    val body = child(props, key, place, parentType)
    return if (props.literal(key) == null) null else lambda(body)
  }

  /**
   * The statements for a `ChildList` prop: each listed child in order, or a loop over the list a
   * template names. [shape] adjusts the place of child `i` of `n` (a list position, a group shape);
   * [indexed] forces a loop to carry its index.
   */
  fun children(
    props: JsonObject,
    key: String,
    place: Place,
    parentType: String,
    indexed: Boolean = false,
    shape: (Place, String, String) -> Place = { p, _, _ -> p },
  ): List<Stmt> {
    when (val value = props[key]) {
      is JsonArray -> {
        val ids = value.mapNotNull { (it as? JsonPrimitive)?.content }
        return ids.flatMapIndexed { i, id ->
          emit(id, shape(place.forChild(parentType), i.toString(), ids.size.toString()))
        }
      }
      is JsonObject -> {
        val componentId = (value["componentId"] as? JsonPrimitive)?.content ?: return emptyList()
        val path = (value["path"] as? JsonPrimitive)?.content ?: return emptyList()
        return listOf(loop(componentId, path, place, parentType, indexed, shape))
      }
      else -> return emptyList()
    }
  }

  /** A template's loop variable and index over the array at its path. */
  private class TemplateScope(val list: Bound, val scope: Scope, val withIndex: Boolean)

  private fun templateScope(
    componentId: String,
    path: String,
    place: Place,
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

  private fun loop(
    componentId: String,
    path: String,
    place: Place,
    parentType: String,
    indexed: Boolean,
    shape: (Place, String, String) -> Place,
  ): Stmt {
    val template = templateScope(componentId, path, place, indexed)
    val scope = template.scope
    val body =
      emit(
        componentId,
        shape(
          place.forChild(parentType).copy(scope = scope),
          scope.index ?: "index",
          "${template.list.expr}.size",
        ),
      )
    val callee = "${template.list.expr}.forEach${if (template.withIndex) "Indexed" else ""}"
    return stmt(
      Call(
        callee,
        lambda =
          Lambda(
            if (scope.index != null) "${scope.index}, ${scope.variable}" else scope.variable,
            body,
          ),
      ),
    )
  }

  /**
   * The children of a `ChildList` prop for an API that asks for them by position, such as a
   * carousel: the item count and the body of a `{ index -> ... }` lambda. Listed children are
   * picked by `index`; a template reads its item from the list at `index`. [shape] adjusts the
   * place of every child, as in [children].
   */
  class ByIndex(val count: String, val param: String, val body: List<Stmt>)

  fun childrenByIndex(
    props: JsonObject,
    key: String,
    place: Place,
    parentType: String,
    shape: (Place) -> Place = { it },
  ): ByIndex {
    val child = place.forChild(parentType)
    when (val value = props[key]) {
      is JsonArray -> {
        val ids = value.mapNotNull { (it as? JsonPrimitive)?.content }
        val bodies = ids.map { emit(it, shape(child)) }
        val body =
          when (bodies.size) {
            0 -> emptyList()
            1 -> bodies.single()
            else ->
              listOf(
                IfStmt(
                  bodies.dropLast(1).mapIndexed { i, statements -> "index == $i" to statements },
                  bodies.last(),
                ),
              )
          }
        return ByIndex(ids.size.toString(), if (bodies.size > 1) "index" else "_", body)
      }
      is JsonObject -> {
        val componentId = (value["componentId"] as? JsonPrimitive)?.content
        val path = (value["path"] as? JsonPrimitive)?.content
        if (componentId != null && path != null) {
          val template = templateScope(componentId, path, place, indexed = true)
          val scope = template.scope
          val index = checkNotNull(scope.index)
          val item = Assign("val ${scope.variable}", "=", Raw("${template.list.expr}[$index]"))
          val body = listOf<Stmt>(item) + emit(componentId, shape(child.copy(scope = scope)))
          return ByIndex("${template.list.expr}.size", index, body)
        }
      }
      else -> {}
    }
    return ByIndex("0", "_", emptyList())
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
        "offset" to Sym.offset,
        "aspectRatio" to Sym.aspectRatio,
        "background" to Sym.background,
        "clickable" to Sym.clickable,
        "verticalScroll" to Sym.verticalScroll,
        "clip" to Sym.clip,
        "nestedScroll" to Sym.nestedScroll,
        "semantics" to Sym.semantics,
      )
  }
}
