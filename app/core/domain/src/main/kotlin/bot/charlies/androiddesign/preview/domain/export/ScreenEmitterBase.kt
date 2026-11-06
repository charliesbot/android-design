package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Screen
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * What a phone and a Wear screen emitter share, read from the [catalog] of the proposal: the
 * screen's components and overlays, the navigation and actions its components call, the state it
 * declares, the typed reads of a component's props and the modifier imports.
 */
internal abstract class ScreenEmitterBase(
  val file: FileContext,
  val design: DesignSystem,
  val screenId: String,
  val screen: Screen,
  private val catalog: Catalog,
) {
  val byId: Map<String, ComponentNode> = screen.components.associateBy { it.id }

  /** The overlay components (dialogs, sheets, menus), in document order. */
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

  /** An enum prop of [component]: the stated value, else its catalog default. */
  fun JsonObject.choice(component: String, key: String): String =
    literal(key)
      ?: checkNotNull(catalogDefault(component, key)) { "$component.$key has no default" }

  fun JsonObject.flag(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toBooleanStrictOrNull()

  protected fun catalogDefault(component: String, key: String): String? =
    catalog.find(component)?.defaultOf(key)?.content

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
  fun childIds(node: ComponentNode): List<String> {
    val def = catalog.find(node.component) ?: return emptyList()
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
