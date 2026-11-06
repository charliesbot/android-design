package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.Arg
import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Chain
import bot.charlies.androiddesign.preview.domain.export.Decl
import bot.charlies.androiddesign.preview.domain.export.Expr
import bot.charlies.androiddesign.preview.domain.export.FileContext
import bot.charlies.androiddesign.preview.domain.export.FunDecl
import bot.charlies.androiddesign.preview.domain.export.IfStmt
import bot.charlies.androiddesign.preview.domain.export.Lambda
import bot.charlies.androiddesign.preview.domain.export.Naming
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.Stmt
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.kotlinString
import bot.charlies.androiddesign.preview.domain.export.lambda
import bot.charlies.androiddesign.preview.domain.export.number
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The state one `RadioButton` group shares: which of its parent's children is picked. */
internal class WearRadio(val state: String, val index: Int)

/** What a component needs to know about where it is emitted, as the renderer's `ChildHost` does. */
internal data class WearPlace(
  val parentType: String? = null,
  /** The parent lets its children stretch to its width (`ComponentDef.stretchesChildren`). */
  val parentStretches: Boolean = true,
  /** A child of a fixed screen, or of a row that stretches: layouts fill the height. */
  val fillHeight: Boolean = false,
  /** The parent is a `Column`, `Row` or `Card`, whose scope takes `weight`. */
  val weightable: Boolean = false,
  /** The parent's own scope gives each child `weight(1f)`: a `ButtonGroup`, a dialog's buttons. */
  val scopeWeight: Boolean = false,
  /**
   * A direct child of the scrolling list: it takes the item's height and surface transformation.
   */
  val item: Boolean = false,
  /** The text style in force is the theme's own `bodyLarge`; a card or stepper changes it. */
  val bodyLarge: Boolean = true,
  val radio: WearRadio? = null,
  /** `Modifier`, or `modifier` for the screen's root. */
  val modifierBase: String = "Modifier",
) {
  fun forChild(parentType: String) =
    WearPlace(
      parentType = parentType,
      parentStretches = WearCatalog.find(parentType)?.stretchesChildren ?: false,
      weightable = parentType in WEIGHT_SCOPES,
      bodyLarge = bodyLarge && parentType !in RESTYLING_PARENTS,
    )

  private companion object {
    val WEIGHT_SCOPES = setOf("Column", "Row", "Card")
    val RESTYLING_PARENTS = setOf("Card", "Stepper")
  }
}

/**
 * Writes one Wear screen as a `@Composable` function. The per-component calls live in
 * `WearComponentEmitters.kt`, `WearActionEmitters.kt` and `WearOverlayEmitters.kt`; this class
 * holds what they share: properties, tokens, modifiers, actions, children, and the screen frame.
 */
internal class WearScreenEmitter(
  val file: FileContext,
  val design: DesignSystem,
  val screenId: String,
  val screen: Screen,
) {
  val byId: Map<String, ComponentNode> = screen.components.associateBy { it.id }

  /** AlertDialog and ConfirmationDialog components, in document order. */
  val overlays: List<ComponentNode> =
    screen.components.filter { it.component in WearCatalog.overlayNames }

  /** True once a component differs between a normal and a large display. */
  var usesLarge = false

  /** True once a fixed screen with an edge button needs the scroll provider object. */
  var usesFixedScroll = false

  val functionName: String = Naming.pascal(screenId) + "Screen"

  /** Screens this screen navigates to. */
  val navigateTargets: List<String> =
    screen.components.flatMap { NavigationGraph.navigateTargets(it, WearCatalog) }.distinct()

  val usesBack: Boolean = screen.components.any { node -> calls(node).any { it.name == "back" } }

  /** The overlay each component sits in, so `dismiss()` knows which one to close. */
  private val overlayOf: Map<String, String> = buildOverlayMap()

  fun callbackName(target: String) = "onNavigateTo${Naming.pascal(target)}"

  // region the function

  /** The screen function and its `@Preview`. */
  fun declarations(): List<Decl> {
    val body = mutableListOf<Stmt>()
    body += overlays.map { stateVar(overlayVar(it.id), "false") }
    val root = byId[ComponentNode.ROOT_ID]
    val rootStatements =
      if (root == null) emptyList() else emit(root.id, WearPlace(modifierBase = "modifier"))
    val tail = overlays.flatMap { overlayStatements(it) }
    if (usesLarge) body += Assign("val isLarge", "=", Raw("isLargeDisplay()"))
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
    if (usesBack) add("onBack: () -> Unit = {}")
    navigateTargets.forEach { add("${callbackName(it)}: () -> Unit = {}") }
  }

  /** The preview puts the screen where an app does: inside the theme and the `AppScaffold`. */
  private fun preview(): Decl {
    val preview = file.use(Sym.Preview)
    val devices = file.use(WearSym.Devices)
    file.use(Sym.Composable)
    // The preview shows the clock only when the screen does.
    val clock =
      if (
        byId[ComponentNode.ROOT_ID]?.let {
          effectiveProps(it, large = false).option("Screen", "timeText")
        } == false
      )
        lambda()
      else lambda(stmt(Call(file.use(WearSym.TimeText))))
    val scaffold =
      Call(
        file.use(WearSym.AppScaffold),
        listOf(arg("timeText", clock)),
        lambda(stmt(Call(functionName))),
      )
    return FunDecl(
      annotations =
        listOf(
          "@$preview(device = $devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)",
          "@Composable",
        ),
      modifiers = "private ",
      name = "${functionName}Preview",
      params = emptyList(),
      body = listOf(stmt(Call(WEAR_THEME, lambda = lambda(stmt(scaffold))))),
    )
  }

  // endregion

  // region components

  /**
   * The statements that draw component [id] in [place]: one block, or an `if (isLarge)` over the
   * two when a `large` size override changes it.
   */
  fun emit(id: String, place: WearPlace): List<Stmt> {
    val node = byId[id] ?: return emptyList()
    val normal = effectiveProps(node, large = false)
    val large = effectiveProps(node, large = true)
    if (normal == large) return emitComponent(node, normal, place)
    usesLarge = true
    return listOf(
      IfStmt(
        listOf("isLarge" to emitComponent(node, large, place)),
        emitComponent(node, normal, place),
      ),
    )
  }

  /** The props [node] has on a normal or a [large] display: its own, then the override on top. */
  fun effectiveProps(node: ComponentNode, large: Boolean): JsonObject {
    val merged = linkedMapOf<String, JsonElement>()
    node.props.forEach { (key, value) ->
      if (key != Catalog.SIZE_CLASS && key != Catalog.STYLE) merged[key] = value
    }
    val width = if (large) WearCatalog.LARGE_MIN_DP else 0
    merged.putAll(node.sizeOverridesAt(WearCatalog.sizeBreakpoints, width))
    return JsonObject(merged)
  }

  /** The named style of [node], or null. */
  fun styleOf(node: ComponentNode): StyleDef? =
    (node.props[Catalog.STYLE] as? JsonPrimitive)?.content?.let { design.styles[it] }

  // endregion

  // region properties

  fun JsonObject.literal(key: String): String? =
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

  /** An enum prop of [component]: the stated value, else its catalog default. */
  fun JsonObject.choice(component: String, key: String): String =
    literal(key)
      ?: checkNotNull(WearCatalog.find(component)?.defaultOf(key)?.content) {
        "$component.$key has no default"
      }

  fun JsonObject.flag(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toBooleanStrictOrNull()

  /** A boolean prop of [component]: the stated literal, else its catalog default. */
  fun JsonObject.option(component: String, key: String): Boolean =
    flag(key)
      ?: WearCatalog.find(component)?.defaultOf(key)?.content?.toBooleanStrictOrNull()
      ?: false

  /** A number prop of [component], moved into the catalog's bounds like the renderer does. */
  fun JsonObject.numberProp(component: String, key: String): Float? {
    val value =
      (this[key] as? JsonPrimitive)
        ?.takeIf { !it.isString }
        ?.content
        ?.toDoubleOrNull()
        ?.takeIf {
          it.isFinite()
        } ?: return null
    val def = WearCatalog.find(component) ?: return value.toFloat()
    return def.coerceNumber(key, value.toFloat().toDouble()).toFloat()
  }

  /** The `[min, max]` pair of [key], or null when it is absent or not two numbers. */
  fun JsonObject.range(key: String): Pair<Float, Float>? {
    val numbers =
      (this[key] as? JsonArray)?.mapNotNull {
        (it as? JsonPrimitive)?.content?.toDoubleOrNull()?.takeIf(Double::isFinite)?.toFloat()
      } ?: return null
    return if (numbers.size == 2) numbers[0] to numbers[1] else null
  }

  fun floatLiteral(value: Float): String = "${number(value)}f"

  fun stringLiteral(value: String): Expr = Raw(kotlinString(value))

  // endregion

  // region tokens

  fun colorRole(name: String): String = "${file.use(WearSym.MaterialTheme)}.colorScheme.$name"

  fun typeRole(name: String): String = "${file.use(WearSym.MaterialTheme)}.typography.$name"

  /** A spacing token as dp: `sm` is `8.dp` when the design system says 8. */
  fun spacing(name: String): String = dp(design.spacing[name] ?: 0f)

  fun dp(value: Float): String = "${number(value)}.${file.use(Sym.dp)}"

  /** A style's shape: a corner token, since Wear Compose has no `MaterialShapes`. */
  fun shapeToken(name: String): String {
    val token = ShapeToken.fromWire(name)
    if (token == null) {
      val hint =
        if (MaterialShape.fromWire(name) != null) "polygons are not in Wear Compose" else "unknown"
      throw PreviewException("shape '$name' cannot be exported to Wear: $hint; use a corner token")
    }
    val corner = design.rounded[token] ?: 0f
    return when {
      token == ShapeToken.None -> file.use(Sym.RectangleShape)
      token == ShapeToken.Full || corner >= 1000f -> file.use(Sym.CircleShape)
      else -> "${file.use(Sym.RoundedCornerShape)}(${dp(corner)})"
    }
  }

  // endregion

  // region modifiers

  /**
   * The `modifier` argument of component [node]: the weight, the stretch, the padding, then (in the
   * order the renderer applies them) the content description, the list item's height
   * transformation, and any [extra] calls such as a size. Null when there is nothing to say.
   */
  fun modifier(
    node: ComponentNode,
    p: JsonObject,
    place: WearPlace,
    describe: Boolean = false,
    item: Boolean = false,
    vararg extra: String?,
  ): Arg? {
    val parts =
      modifierParts(node, p, place, describe, item) + extra.filterNotNull().map(::useModifier)
    return modifierArg(place, parts)
  }

  fun modifierParts(
    node: ComponentNode,
    p: JsonObject,
    place: WearPlace,
    describe: Boolean,
    item: Boolean,
    fillHeight: Boolean = false,
  ): List<String> {
    val parts = mutableListOf<String>()
    if (place.scopeWeight) {
      parts += "weight(1f)"
    } else if (place.weightable) {
      p.numberProp(node.component, "weight")?.let { parts += "weight(${floatLiteral(it)})" }
    }
    val def = WearCatalog.find(node.component)
    if (place.parentStretches && def?.fillsWidth { p[it] } == true) {
      parts += useModifier("fillMaxWidth()")
    }
    if (fillHeight && place.fillHeight) parts += useModifier("fillMaxHeight()")
    (p.literal("padding") ?: styleOf(node)?.padding)?.let {
      parts += "${useModifier("padding")}(${spacing(it)})"
    }
    if (describe) {
      p.literal("contentDescription")?.let {
        file.use(Sym.contentDescription)
        parts += "${useModifier("semantics")} { contentDescription = ${kotlinString(it)} }"
      }
    }
    if (item && place.item) parts += itemHeight()
    return parts
  }

  /** `transformedHeight(this, transformationSpec)`, the height a scroll item takes at the edge. */
  fun itemHeight(): String {
    file.use(WearSym.transformedHeight)
    return "transformedHeight(this, transformationSpec)"
  }

  /** The surface transformation argument of a component that is a scroll item, else null. */
  fun transformation(place: WearPlace): Arg? {
    if (!place.item) return null
    return arg("transformation", "${file.use(WearSym.SurfaceTransformation)}(transformationSpec)")
  }

  fun modifierArg(place: WearPlace, parts: List<String>): Arg? {
    if (parts.isEmpty()) {
      return if (place.modifierBase == "modifier") arg("modifier", "modifier") else null
    }
    return arg("modifier", modifierChain(place, parts))
  }

  fun modifierChain(place: WearPlace, parts: List<String>): Expr {
    val base = if (place.modifierBase == "modifier") "modifier" else file.use(Sym.Modifier)
    return Chain(base, parts)
  }

  /** Imports the extension behind a modifier call written as `name(...)` and returns the call. */
  fun useModifier(call: String): String {
    val name = call.substringBefore('(').substringBefore(' ')
    MODIFIER_IMPORTS[name]?.let { file.use(it) }
    return call
  }

  // endregion

  // region actions

  class ActionCall(val name: String, val args: JsonObject?)

  /** Every `functionCall` in the action props of [node], size overrides included. */
  fun calls(node: ComponentNode): List<ActionCall> {
    val def = WearCatalog.find(node.component) ?: return emptyList()
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

  /**
   * `var name by remember { mutableStateOf(value) }`; [factory] is `mutableStateOf`,
   * `mutableFloatStateOf` or `mutableIntStateOf`.
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
    for (overlay in screen.components.filter { it.component in WearCatalog.overlayNames }) {
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

  /** Ids of the components [node] references through child props. */
  fun childIds(node: ComponentNode): List<String> {
    val def = WearCatalog.find(node.component) ?: return emptyList()
    return def.props.flatMap { prop ->
      when (val value = node.props[prop.name]) {
        is JsonPrimitive ->
          if (prop.kind == PropKind.Child && value.isString) listOf(value.content) else emptyList()
        is JsonArray ->
          if (prop.kind == PropKind.ChildList) value.mapNotNull { (it as? JsonPrimitive)?.content }
          else emptyList()
        else -> emptyList()
      }
    }
  }

  // endregion

  // region children

  /** The statements that set a block of children up, and the ones that draw them. */
  class Children(val prelude: List<Stmt>, val body: List<Stmt>)

  /** The ids of a `ChildList` prop. */
  fun childList(p: JsonObject, key: String): List<String> =
    (p[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }.orEmpty()

  /**
   * The children of [parent] listed under [key], each drawn in [place] (a radio button also learns
   * its group). [wrap] puts one child's statements where the parent needs them, such as in an `item
   * { }`.
   */
  fun children(
    parent: ComponentNode,
    p: JsonObject,
    key: String,
    place: WearPlace,
    wrap: (List<Stmt>) -> List<Stmt> = { it },
  ): Children {
    val ids = childList(p, key)
    val radios = ids.filter { byId[it]?.component == "RadioButton" }
    val state = stateName(parent.id, "Selected")
    val prelude =
      if (radios.isEmpty()) emptyList()
      else {
        val picked = ids.indexOfFirst { id ->
          byId[id]?.let {
            it.component == "RadioButton" && effectiveProps(it, false).flag("selected") == true
          } == true
        }
        listOf(stateVar(state, picked.toString(), WearSym.mutableIntStateOf))
      }
    val body = ids.flatMapIndexed { index, id ->
      val child = if (id in radios) place.copy(radio = WearRadio(state, index)) else place
      wrap(emit(id, child))
    }
    return Children(prelude, body)
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
        "aspectRatio" to Sym.aspectRatio,
        "background" to Sym.background,
        "clip" to Sym.clip,
        "semantics" to Sym.semantics,
      )
  }
}
