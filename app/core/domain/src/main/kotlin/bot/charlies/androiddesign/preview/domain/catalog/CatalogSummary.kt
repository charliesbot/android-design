package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.Navigation
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.ShapeToken

/**
 * The catalog in a form an agent can read in one go: what `get_catalog` returns without arguments.
 * One line per component (name, purpose, props as `name: kind`), then the token vocabularies and
 * the action functions once. The full JSON Schema of a component is [CatalogJson.components].
 */
object CatalogSummary {
  /** The summary of [catalog] as plain text; [CatalogJson.catalog] has the same facts as JSON. */
  fun render(catalog: Catalog = M3ExpressiveCatalog): String = buildString {
    appendLine("${catalog.title} (${catalog.id})")
    appendLine(KEY)
    appendLine()
    appendLine("COMPONENTS")
    appendLine("Every component also takes: ${commonProps(catalog)}.")
    catalog.components.forEach { appendLine(line(it)) }
    appendLine()
    if (catalog === M3ExpressiveCatalog) {
      appendLine(NAVIGATION)
      appendLine()
    }
    appendLine("TOKENS (props of kind colorRole, typeRole, shape and spacing take these names)")
    appendLine("colorRole: ${catalog.colorRoles.joinToString(" ")}")
    appendLine("typeRole: ${catalog.typeRoles.keys.joinToString(" ")}")
    if (catalog === M3ExpressiveCatalog) {
      appendLine(
        "shape: ${ShapeToken.entries.joinToString(" ") { it.wire }} (corner tokens), " +
          "${MaterialShape.entries.joinToString(" ") { it.wire }} (polygons)",
      )
    }
    appendLine(
      "spacing: the design system's scale, default ${DesignSystemResolver.DEFAULT_SPACING.keys.joinToString(" ")}",
    )
    if (catalog.showInSizes.isNotEmpty()) {
      appendLine("sizeClass: ${catalog.showInSizes.joinToString(" ") { it.wire }}")
    }
    appendLine()
    appendLine("ACTIONS (an action is {\"functionCall\": {\"@call\": name, \"args\": {...}}})")
    ActionFunction.entries.forEach { function ->
      val argument = function.argument?.let { "$it: string" }.orEmpty()
      appendLine("${function.wire}($argument): ${function.description}")
    }
    appendLine()
    appendLine(CatalogJson.instructions(catalog))
    appendLine()
    append("Call get_catalog with component for the full JSON Schema of the components you use.")
  }

  /**
   * The proposal's `navigation` field, which holds what no component can: the bar and the rail are
   * not part of a screen.
   */
  private val NAVIGATION =
    """
    NAVIGATION (the proposal's `navigation` field, beside `screens`, not a component)
    start*: screen id. panes: {screen id: list|detail|supporting|extra}. placeholders: {list screen id: screen id}.
    destinations: [{screen*: screen id, label*: string, icon*: icon, selectedIcon: icon, badge: boolean|number|string}], ${Navigation.MIN_DESTINATIONS} to ${Navigation.MAX_DESTINATIONS} entries, a literal badge only. railExpanded: boolean = false.
    The bottom bar and the rail are declared once here, never in a screen: there is no NavigationBar, NavigationRail or Scaffold navigationRail. The previewer draws a bar at compact widths and a rail at medium and expanded widths, outside the panes, and tags them `@navigation` in get_tree. Selected: the first destination from the top of the back stack. A tap makes that screen the whole back stack. Shown on a destination, and beside a destination when the view has room for two panes and the selected destination has a pane role too; hidden on a screen that covers the whole window. A destination screen has the pane role list or none. railExpanded expands the rail from 840 dp.
    """
      .trimIndent()

  private const val KEY =
    "Component lines read `Name: purpose. prop: kind, ...`. `*` marks a required prop. `= value` " +
      "is a prop's default: leave the prop out to get it. A kind is " +
      "string, boolean, number, numberRange ([min, max]), one of the enum values joined with |, " +
      "child (one component id), childList (component ids or a template {componentId, path}); " +
      "a slot that accepts only some components lists them, as child(DockedToolbar|FloatingToolbar). " +
      "Other kinds are " +
      "colorRole, typeRole, shape, spacing, icon (Material Symbols name), image, action or " +
      "binding. A string, boolean or number prop also takes a binding {\"@path\": \"/data/path\"}. " +
      "[overlay] components stay closed until an open action names them."

  private fun commonProps(catalog: Catalog): String =
    (listOf("id*: string") +
        listOfNotNull("showIn: sizeClass[]".takeIf { catalog.showInSizes.isNotEmpty() }) +
        "sizeClass: {${catalog.sizeBreakpoints.joinToString { it.key }}: prop overrides}" +
        "style: string" +
        catalog.commonProps.map(::prop))
      .joinToString(", ")

  private fun line(def: ComponentDef): String = buildString {
    append(def.name)
    if (def.overlay) append(" [overlay]")
    append(": ")
    append(def.description.trim())
    if (!endsWithTerminator()) append('.')
    if (def.props.isNotEmpty()) append(' ').append(def.props.joinToString(", ", transform = ::prop))
  }

  private fun StringBuilder.endsWithTerminator() = lastOrNull() in setOf('.', '!', '?')

  private fun prop(prop: PropDef): String {
    val allowed = prop.allowedChildren?.sorted()?.joinToString("|", prefix = "(", postfix = ")")
    val default = prop.default?.let { " = ${it.content}" }.orEmpty()
    return "${prop.name}${if (prop.required) "*" else ""}: ${kind(prop.kind)}${allowed.orEmpty()}$default"
  }

  private fun kind(kind: PropKind): String =
    when (kind) {
      PropKind.Child,
      PropKind.Anchor -> "child"
      PropKind.ChildList -> "childList"
      PropKind.Text -> "string"
      PropKind.Bool -> "boolean"
      is PropKind.Number -> "number"
      PropKind.NumberRange -> "numberRange"
      is PropKind.Enum -> kind.values.joinToString("|")
      PropKind.ColorRole -> "colorRole"
      PropKind.TypeRole -> "typeRole"
      PropKind.Shape -> "shape"
      PropKind.Spacing -> "spacing"
      PropKind.Icon -> "icon"
      PropKind.Image -> "image"
      PropKind.Action -> "action"
      PropKind.Binding -> "binding"
      PropKind.Badge -> "boolean|number|string"
    }
}
