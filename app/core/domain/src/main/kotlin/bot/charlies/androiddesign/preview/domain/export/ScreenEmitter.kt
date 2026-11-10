package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.CarouselItemGate
import bot.charlies.androiddesign.preview.domain.CarouselSpec
import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.SearchBars
import bot.charlies.androiddesign.preview.domain.TooltipSpec
import bot.charlies.androiddesign.preview.domain.layout.growsSemanticsBounds
import bot.charlies.androiddesign.preview.domain.node.ChildItems
import bot.charlies.androiddesign.preview.domain.node.childItems
import bot.charlies.androiddesign.preview.model.CarouselItemSize
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonElement
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

/**
 * A group with single selection (a `ChipGroup`, a `RadioGroup`, the single-select items of a menu
 * group): one state variable holds the id of the selected child.
 */
internal class ChipSelection(val state: String, val byRow: Boolean = false)

/**
 * How the selectable items of one menu group address their remembered state when a data template is
 * involved. A templated group keeps one entry per data row ([groupSuffix], such as `[index]`), and
 * templated items keep one entry per data row of their own, so a row has no id to name them by.
 */
internal class MenuRows(val groupSuffix: String = "", val itemsTemplated: Boolean = false)

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
  override val scope: Scope? = null,
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
  override val modifierBase: String = "Modifier",
  /** Modifier calls to put first, such as `padding(innerPadding)`. */
  val leading: List<String> = emptyList(),
  val listPosition: ListPosition? = null,
  val railExpanded: Boolean = false,
  /** `ButtonGroupDefaults.connectedMiddleButtonShapes()` for a child of a connected group. */
  val groupShapes: String? = null,
  val chipSelection: ChipSelection? = null,
  val menuRows: MenuRows = MenuRows(),
  /** True for an item of a vibrant `Menu`. */
  val menuVibrant: Boolean = false,
  /** True for a floating toolbar placed over content in a `Box`. */
  val floating: Boolean = false,
  val scrollBehavior: Boolean = false,
  /**
   * True for a child of a floating or docked toolbar, whose icon buttons take the toolbar's content
   * color. Kept when a tooltip or menu wraps the child, which makes [parentType] a `Box`.
   */
  val inToolbar: Boolean = false,
) : Placement {
  fun forChild(parentType: String) =
    copy(
      parentType = parentType,
      inToolbar = parentType == "FloatingToolbar" || parentType == "DockedToolbar",
      parentStretches = M3ExpressiveCatalog.find(parentType)?.stretchesChildren ?: false,
      fillWidth = false,
      fillHeight = false,
      modifierBase = "Modifier",
      leading = emptyList(),
      listPosition = null,
      groupShapes = null,
      chipSelection = null,
      menuRows = MenuRows(),
      menuVibrant = false,
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
  file: FileContext,
  design: DesignSystem,
  screenId: String,
  screen: Screen,
  data: ScreenData,
  private val themeName: String,
) : ScreenEmitterBase(file, design, screenId, screen, data, M3ExpressiveCatalog) {

  /** The search bars and search app bars: each holds the `SearchBarState` its view shares. */
  val searchBars: List<ComponentNode> =
    screen.components.filter { it.component == "SearchBar" || SearchBars.isSearchAppBar(it) }

  /** The tooltips, each with the `TooltipState` its `TooltipBox` shares. */
  val tooltips: List<ComponentNode> = screen.components.filter { it.component == "Tooltip" }

  /** Which overlays are written beside a component and which at the end of the screen. */
  val placement = OverlayPlacement(screen)

  var usesSizeClass = false

  // region the function

  /** The screen function and its `@Preview`. */
  fun declarations(): List<Decl> {
    val body = mutableListOf<Stmt>()
    body += overlays.filter { it.component !in STATE_OVERLAYS }.map { overlayState(it) }
    if (placement.snackbars.isNotEmpty()) body += snackbarHostState()
    if (searchBars.isNotEmpty() || tooltips.isNotEmpty()) body += coroutineScopeDecl()
    val root = byId[ComponentNode.ROOT_ID]
    val rootStatements =
      if (root == null) emptyList() else emit(root.id, Place(modifierBase = "modifier"))
    val tail =
      placement.screenLevel.flatMap { overlayStatements(it) } +
        placement.snackbars.flatMap { snackbarStatements(it) }
    // Written after the root, which says whether the states read the window width class.
    val searchStates = searchBars.map { searchStateDecl(it) }
    val tooltipStates = tooltips.map { tooltipStateDecl(it) }
    if (usesSizeClass) body += sizeClassState()
    body += searchStates
    body += tooltipStates
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

  private fun coroutineScopeDecl(): Stmt =
    Assign("val $COROUTINE_SCOPE", "=", Call(file.use(Sym.rememberCoroutineScope)))

  fun searchState(barId: String) = stateName(barId, "SearchState")

  fun tooltipState(id: String) = stateName(id, "TooltipState")

  /**
   * The Kotlin condition for "the view [barId] opens is docked": `true` or `false` when one layout
   * holds at every window width class, else a test of the width class. The layout is read with the
   * size class overrides laid over it, as the previewer draws it.
   */
  fun searchDockedCondition(barId: String): String {
    val view = placement.expandedViewOf(barId)?.let(byId::get) ?: return "false"
    val docked =
      SizeClass.entries
        .filter { effectiveProps(view, it).choice("SearchView", "layout") == "docked" }
        .toSet()
    return when {
      docked.isEmpty() -> "false"
      docked.size == SizeClass.entries.size -> "true"
      else -> condition(docked).also { usesSizeClass = true }
    }
  }

  /** The state a search bar shares with its view: the factory Compose pairs with the layout. */
  private fun searchStateDecl(bar: ComponentNode): Stmt {
    val docked = file.use(Sym.m3("rememberSearchBarWithGapState"))
    val contained = file.use(Sym.m3("rememberContainedSearchBarState"))
    val factory =
      when (val condition = searchDockedCondition(bar.id)) {
        "true" -> "$docked()"
        "false" -> "$contained()"
        else -> "if ($condition) $docked() else $contained()"
      }
    return Assign("val ${searchState(bar.id)}", "=", Raw(factory))
  }

  /**
   * A tooltip with buttons is persistent, so it stays until acted on; Material advises against a
   * persistent tooltip otherwise ("avoid persistent rich tooltips on icon buttons").
   */
  private fun tooltipStateDecl(tooltip: ComponentNode): Stmt {
    experimentalApi()
    val persistent =
      SizeClass.entries.any { size ->
        val p = effectiveProps(tooltip, size)
        p.reader("Tooltip").enum("variant", TooltipSpec::variant) == TooltipSpec.Variant.Rich &&
          hasChildren(p, "actions")
      }
    return Assign(
      "val ${tooltipState(tooltip.id)}",
      "=",
      Call(
        file.use(Sym.m3("rememberTooltipState")),
        if (persistent) listOf(arg("isPersistent", "true")) else emptyList(),
      ),
    )
  }

  /** Whether the `ChildList` prop [key] holds anything, a template counting as something. */
  fun hasChildren(props: JsonObject, key: String): Boolean =
    when (val items = props[key].childItems()) {
      is ChildItems.Listed -> items.ids.isNotEmpty()
      is ChildItems.Template -> true
      null -> false
    }

  private fun experimentalApi() = file.optIn(Sym.ExperimentalMaterial3Api)

  /** `coroutineScope.launch { [call] }`: a suspend call on a state, from an action. */
  private fun launching(call: String): Stmt =
    stmt(Call("$COROUTINE_SCOPE.${file.use(Sym.launch)}", lambda = lambdaOf(Raw(call))))

  /** What `open(id)` does: raise the overlay's flag, expand a search view, show a tooltip. */
  override fun openOverlay(id: String): Stmt {
    val node = byId[id]
    return when (node?.component) {
      "SearchView" ->
        placement.barOf(id)?.let { launching("${searchState(it)}.animateToExpanded()") }
      "Tooltip" -> launching("${tooltipState(id)}.show()")
      else -> null
    } ?: Assign(overlayVar(id), "=", Raw("true"))
  }

  /** What `dismiss()` does to overlay [id]. */
  override fun closeOverlay(id: String): Stmt {
    val node = byId[id]
    return when (node?.component) {
      "SearchView" ->
        placement.barOf(id)?.let { launching("${searchState(it)}.animateToCollapsed()") }
      "Tooltip" -> launching("${tooltipState(id)}.dismiss()")
      else -> null
    } ?: Assign(overlayVar(id), "=", Raw("false"))
  }

  /** The `SnackbarHostState` the Scaffold's `SnackbarHost` shows and each snackbar posts to. */
  private fun snackbarHostState(): Stmt =
    Assign(
      "val $SNACKBAR_HOST_STATE",
      "=",
      Call(
        file.use(Sym.remember),
        lambda = lambda(stmt(Call(file.use(Sym.m3("SnackbarHostState"))))),
      ),
    )

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
    val body = perSize(node) { props -> emitComponent(node, props, place) }
    // A node inside a carousel item that shows only at larger item sizes. The item scope's draw
    // info says which size the item is at, but it changes on every scroll frame: the check is a
    // derivedStateOf, so the item recomposes only when the answer flips.
    val minimum = node.carouselItemSize
    if (minimum == null || body.isEmpty()) return body
    val shown = stateName(node.id, "Shown")
    val smallPx = stateName(node.id, "SmallPx")
    val needsSmall = minimum == CarouselItemSize.Medium
    val smallItem =
      if (needsSmall) {
        val density = file.use(Sym.LocalDensity)
        listOf<Stmt>(
          Assign(
            "val $smallPx",
            "=",
            Raw("with($density.current) { ${dp(CarouselSpec.SMALL_ITEM_DP)}.toPx() }"),
          ),
        )
      } else {
        emptyList()
      }
    val condition = CarouselItemGate.expression(minimum, "carouselItemDrawInfo", smallPx)
    file.use(Sym.getValue)
    val derived = file.use(Sym.derivedStateOf)
    val check =
      Assign(
        "val $shown",
        "by",
        Call(
          file.use(Sym.remember),
          if (needsSmall) listOf(pos(smallPx)) else emptyList(),
          lambda(stmt(Call(derived, lambda = lambda(stmt(Or(condition.split(" || "))))))),
        ),
      )
    return smallItem + check + IfStmt(listOf(shown to body), null)
  }

  /**
   * [draw] for [node] at the size classes it is shown at: once when one set of props holds at all
   * of them, else one branch per distinct set, chosen by the window width class. Nothing when the
   * node is shown at none.
   */
  fun perSize(node: ComponentNode, draw: (JsonObject) -> List<Stmt>): List<Stmt> {
    val visible = linkedMapOf<JsonObject, MutableSet<SizeClass>>()
    for (size in SizeClass.entries) {
      if (!node.isShownAt(size)) continue
      visible.getOrPut(effectiveProps(node, size)) { linkedSetOf() } += size
    }
    if (visible.isEmpty()) return emptyList()
    val single = visible.entries.singleOrNull()
    if (single != null && single.value.size == SizeClass.entries.size) {
      return draw(single.key)
    }
    usesSizeClass = true
    return listOf(IfStmt(visible.map { (props, sizes) -> condition(sizes) to draw(props) }, null))
  }

  /** True when [node] or anything under it grows its semantics bounds at some size class. */
  fun needsUnboundedHeight(node: ComponentNode): Boolean =
    growsSemanticsBounds(node, byId, catalog) { current ->
      SizeClass.entries.map { effectiveProps(current, it) }
    }

  fun condition(sizes: Set<SizeClass>): String {
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

  /**
   * An enum prop of [component] the proposal states with a value other than its catalog default,
   * else null: Compose needs code only for a choice that is not its own default.
   */
  fun JsonObject.nonDefault(component: String, key: String): String? =
    literal(key)?.takeIf { it != catalogDefault(component, key) }

  /** A number prop of [component]: its sample value as the previewer reads it, else its default. */
  fun JsonObject.number(component: String, key: String, place: Place): Double =
    sampleReader(component, place).numberOrDefault(key)

  /** A boolean prop of [component]: the stated literal, else its catalog default. */
  fun JsonObject.option(component: String, key: String): Boolean =
    flag(key)
      ?: checkNotNull(catalogDefault(component, key)?.toBooleanStrictOrNull()) {
        "$component.$key has no default"
      }

  fun JsonObject.number(key: String): Double? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toDoubleOrNull()

  /** A number prop as a `Float` expression. */
  fun JsonObject.floatExpr(key: String, place: Place): String? {
    val element = this[key] ?: return null
    number(key)?.let {
      return floatLiteral(it)
    }
    return bindingPath(element)?.let { "${resolve(it, place.scope).expr}.toFloat()" }
  }

  fun floatLiteral(value: Double): String = "${number(value.toFloat())}f"

  // endregion

  // region bindings

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

  // endregion

  // region modifiers

  /**
   * The `modifier` argument: the place's leading calls, the weight inside a row or column, the
   * stretch to the parent's width, the stretch to its height when [fillHeight] says so, any [extra]
   * calls, and the padding last. Null when there is nothing to say. A component that pads its own
   * content inside its container (a Card) sets [padded] to false and writes the padding there.
   */
  fun modifier(
    place: Place,
    props: JsonObject,
    vararg extra: String?,
    fillHeight: Boolean = false,
    padded: Boolean = true,
  ): Arg? {
    val parts = mutableListOf<String>()
    parts += place.leading.map(::useModifier)
    if (place.parentType == "Row" || place.parentType == "Column") {
      props.floatExpr("weight", place)?.let { parts += "weight($it)" }
    }
    props.floatExpr(Catalog.MAX_WIDTH, place)?.let {
      parts += "${useModifier("widthIn")}(max = ${it.removeSuffix("f")}.${file.use(Sym.dp)})"
    }
    if (place.fillWidth) parts += useModifier("fillMaxWidth()")
    if (fillHeight) parts += useModifier("fillMaxHeight()")
    parts += extra.filterNotNull().map(::useModifier)
    if (padded) {
      props.literal("padding")?.let { parts += "${useModifier("padding")}(${spacing(it)})" }
    }
    return modifierArg(place, parts)
  }

  // endregion

  // region actions

  private fun overlayState(node: ComponentNode): Stmt = stateVar(overlayVar(node.id), "false")

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
    return when (val items = props[key].childItems()) {
      is ChildItems.Listed ->
        items.ids.flatMapIndexed { i, id ->
          emit(id, shape(place.forChild(parentType), i.toString(), items.ids.size.toString()))
        }
      is ChildItems.Template ->
        listOf(loop(items.componentId, items.path, place, parentType, indexed, shape))
      null -> emptyList()
    }
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
    when (val items = props[key].childItems()) {
      is ChildItems.Listed -> {
        val bodies = items.ids.map { emit(it, shape(child)) }
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
        return ByIndex(items.ids.size.toString(), if (bodies.size > 1) "index" else "_", body)
      }
      is ChildItems.Template -> {
        val template = templateScope(items.componentId, items.path, place, indexed = true)
        val scope = template.scope
        val index = checkNotNull(scope.index)
        val item = Assign("val ${scope.variable}", "=", Raw("${template.list.expr}[$index]"))
        val body = listOf<Stmt>(item) + emit(items.componentId, shape(child.copy(scope = scope)))
        return ByIndex("${template.list.expr}.size", index, body)
      }
      null -> return ByIndex("0", "_", emptyList())
    }
  }

  // endregion

  companion object {
    /** The name of the `SnackbarHostState` variable the Scaffold and the snackbars share. */
    const val SNACKBAR_HOST_STATE = "snackbarHostState"

    /** The scope that runs the suspend calls on search and tooltip states. */
    const val COROUTINE_SCOPE = "coroutineScope"

    /** Overlays whose open state is a Compose state object, not a flag. */
    private val STATE_OVERLAYS = setOf("SearchView", "Tooltip")
  }
}
