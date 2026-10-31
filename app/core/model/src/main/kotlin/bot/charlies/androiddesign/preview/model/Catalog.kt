package bot.charlies.androiddesign.preview.model

/** What a prop's value may be. Any [String], [Boolean] or [Number] prop may also be a binding. */
sealed interface PropKind {
  /** The id of one component. */
  data object Child : PropKind

  /** An array of component ids, or a template `{componentId, path}`. */
  data object ChildList : PropKind

  data object Text : PropKind

  data object Bool : PropKind

  /** A number, optionally limited to [bounds]. */
  data class Number(val bounds: NumberBounds? = null) : PropKind

  /** A two-number array such as `[0, 100]`. */
  data object NumberRange : PropKind

  data class Enum(val values: List<String>) : PropKind

  /** A [ColorRole] wire name. */
  data object ColorRole : PropKind

  /** A [TypeRole] wire name. */
  data object TypeRole : PropKind

  /** A [ShapeToken] or [MaterialShape] wire name. */
  data object Shape : PropKind

  /** A name from the merged design system's spacing scale. */
  data object Spacing : PropKind

  /** A Material Symbols name. */
  data object Icon : PropKind

  /** A path relative to the proposal file, or a `data:` URI. */
  data object Image : PropKind

  /** `{"functionCall": {"@call": ..., "args": {...}}}`. */
  data object Action : PropKind

  /** `{"@path": "..."}` only. */
  data object Binding : PropKind
}

/**
 * The values a number prop can take: from [min] to [max], whole numbers only when [whole]. The
 * validator rejects a literal outside them, and the renderer clamps a bound value into them, so a
 * value from the data model never reaches a Compose API that would throw on it.
 */
data class NumberBounds(val min: Double, val max: Double, val whole: Boolean = false) {
  operator fun contains(value: Double): Boolean =
    value.isFinite() && value in min..max && (!whole || value == Math.rint(value))

  /** The nearest allowed value; a value that is not a finite number becomes [min]. */
  fun coerce(value: Double): Double {
    if (!value.isFinite()) return min
    val inRange = value.coerceIn(min, max)
    return if (whole) Math.rint(inRange) else inRange
  }

  /** `a whole number from 1 to 24`, for error messages. */
  val description: String
    get() = "${if (whole) "a whole number" else "a number"} from ${format(min)} to ${format(max)}"

  private fun format(value: Double) =
    if (value == Math.rint(value)) value.toLong().toString() else value.toString()
}

/**
 * One prop of a component. For [PropKind.Child] and [PropKind.ChildList], [allowedChildren] lists
 * the component names it may reference, or is null when any component is allowed.
 */
data class PropDef(
  val name: String,
  val kind: PropKind,
  val required: Boolean = false,
  val description: String = "",
  val allowedChildren: Set<String>? = null,
)

enum class ComponentGroup {
  Layout,
  Content,
  Structure,
  Lists,
  Actions,
  Chips,
  Inputs,
  Feedback,
  Overlays,
}

/**
 * A component of the catalog.
 *
 * @property compose The Compose counterpart, from `compose-mapping.md`. Used by the exporter and by
 *   docs.
 * @property interactive True when the user taps it, so the touch target rule applies.
 * @property overlay True for components that stay closed until an `open(id)` action.
 */
data class ComponentDef(
  val name: String,
  val group: ComponentGroup,
  val description: String,
  val compose: String,
  val props: List<PropDef>,
  val interactive: Boolean = false,
  val overlay: Boolean = false,
) {
  fun prop(name: String): PropDef? = props.firstOrNull { it.name == name }
}

/** Renderer-only functions an `action` may call. */
enum class ActionFunction(val wire: String, val argument: String?, val description: String) {
  Navigate("navigate", "screen", "Opens the screen with this id."),
  Back("back", null, "Goes back one screen."),
  Open("open", "overlay", "Opens a BottomSheet, Dialog or Menu of the same screen by id."),
  Dismiss("dismiss", null, "Closes the open overlay.");

  companion object {
    fun fromWire(value: String): ActionFunction? = entries.firstOrNull { it.wire == value }
  }
}

/**
 * The component registry: the single source of truth for the A2UI catalog JSON, the proposal
 * schema, the validator, the renderer and the exporter.
 */
object M3ExpressiveCatalog {
  const val ID = "https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1"
  const val PROTOCOL_VERSION = "1.0"
  const val TITLE = "Material 3 Expressive for Android"

  /** Props every component accepts besides `id`, `component`, `showIn`, `sizeClass` and `style`. */
  val commonProps: List<PropDef> =
    listOf(
      PropDef(
        "weight",
        PropKind.Number(NumberBounds(0.01, 100.0)),
        description = "Relative weight inside a Row or Column.",
      ),
      PropDef("padding", PropKind.Spacing, description = "Padding around the component."),
    )

  /** The most columns a `Grid` has. */
  const val MAX_GRID_COLUMNS = 24

  const val SHOW_IN = "showIn"
  const val SIZE_CLASS = "sizeClass"
  const val STYLE = "style"

  val components: List<ComponentDef> by lazy { buildComponents() }

  fun component(name: String): ComponentDef? = components.firstOrNull { it.name == name }

  private val byName: Map<String, ComponentDef> by lazy { components.associateBy { it.name } }

  fun find(name: String): ComponentDef? = byName[name]

  /** Names of the components that open as overlays. */
  val overlayNames: Set<String> by lazy { components.filter { it.overlay }.map { it.name }.toSet() }
}

// region registry definitions

private fun text(name: String, required: Boolean = false, description: String = "") =
  PropDef(name, PropKind.Text, required, description)

private fun bool(name: String, description: String = "") =
  PropDef(name, PropKind.Bool, description = description)

private fun num(name: String, description: String = "", bounds: NumberBounds? = null) =
  PropDef(name, PropKind.Number(bounds), description = description)

private fun range(name: String, description: String = "") =
  PropDef(name, PropKind.NumberRange, description = description)

private fun oneOf(name: String, vararg values: String, description: String = "") =
  PropDef(name, PropKind.Enum(values.toList()), description = description)

private fun child(
  name: String,
  vararg allowed: String,
  required: Boolean = false,
  description: String = "",
) = PropDef(name, PropKind.Child, required, description, allowed.toSet().ifEmpty { null })

private fun children(
  name: String,
  vararg allowed: String,
  required: Boolean = false,
  description: String = "",
) = PropDef(name, PropKind.ChildList, required, description, allowed.toSet().ifEmpty { null })

private fun color(name: String, description: String = "") =
  PropDef(name, PropKind.ColorRole, description = description)

private fun type(name: String = "type") = PropDef(name, PropKind.TypeRole)

private fun shape(name: String = "shape") = PropDef(name, PropKind.Shape)

private fun spacing(name: String, description: String = "") =
  PropDef(name, PropKind.Spacing, description = description)

private fun icon(name: String = "icon", required: Boolean = false) =
  PropDef(name, PropKind.Icon, required, "Material Symbols name, such as \"home\".")

private fun action() = PropDef("action", PropKind.Action, description = "What a tap does.")

private val interactiveProps =
  listOf(
    bool("primary", "Marks the screen's primary action. Exactly one is expected per screen."),
    text("contentDescription", description = "Accessibility label when it differs from the label."),
    bool("enabled"),
  )

private val buttonSizes = arrayOf("extra-small", "small", "medium", "large", "extra-large")
private val alignments =
  arrayOf(
    "topStart",
    "topCenter",
    "topEnd",
    "centerStart",
    "center",
    "centerEnd",
    "bottomStart",
    "bottomCenter",
    "bottomEnd",
  )
private val arrangements =
  arrayOf("start", "center", "end", "spaceBetween", "spaceAround", "spaceEvenly")
private val chipVariants = arrayOf("outlined", "elevated")

private fun buildComponents(): List<ComponentDef> =
  layout() +
    content() +
    structure() +
    lists() +
    actions() +
    chips() +
    inputs() +
    feedback() +
    overlays()

private fun layout(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Column",
      ComponentGroup.Layout,
      "Arranges children vertically.",
      "Column",
      listOf(
        children("children", required = true),
        spacing("spacing", "Gap between children (Arrangement.spacedBy)."),
        oneOf("horizontalAlignment", "start", "center", "end"),
        oneOf("arrangement", *arrangements),
        bool("scroll", "Makes the column vertically scrollable."),
      ),
    ),
    ComponentDef(
      "Row",
      ComponentGroup.Layout,
      "Arranges children horizontally.",
      "Row",
      listOf(
        children("children", required = true),
        spacing("spacing", "Gap between children (Arrangement.spacedBy)."),
        oneOf("verticalAlignment", "top", "center", "bottom"),
        oneOf("arrangement", *arrangements),
      ),
    ),
    ComponentDef(
      "Box",
      ComponentGroup.Layout,
      "Stacks children on top of each other.",
      "Box",
      listOf(children("children", required = true), oneOf("contentAlignment", *alignments)),
    ),
    ComponentDef(
      "Grid",
      ComponentGroup.Layout,
      "Lays children out in a grid with a column count or a minimum column width.",
      "Grid (experimental Compose Grid)",
      listOf(
        children("children", required = true),
        num(
          "columns",
          "Fixed column count.",
          NumberBounds(1.0, M3ExpressiveCatalog.MAX_GRID_COLUMNS.toDouble(), whole = true),
        ),
        num(
          "minColumnWidth",
          "Minimum column width in dp; columns fill the width.",
          NumberBounds(1.0, 1000.0),
        ),
        spacing("spacing", "Gap between cells."),
      ),
    ),
    ComponentDef(
      "FlexBox",
      ComponentGroup.Layout,
      "Lays children out in a row that wraps.",
      "FlexBox (experimental Compose FlexBox)",
      listOf(children("children", required = true), bool("wrap"), spacing("gap")),
    ),
    ComponentDef(
      "Spacer",
      ComponentGroup.Layout,
      "Empty space.",
      "Spacer",
      listOf(spacing("width"), spacing("height")),
    ),
    ComponentDef(
      "Divider",
      ComponentGroup.Layout,
      "A thin separating line.",
      "HorizontalDivider, VerticalDivider",
      listOf(bool("vertical")),
    ),
    ComponentDef(
      "Shape",
      ComponentGroup.Layout,
      "Clips its child to a MaterialShapes polygon.",
      "Modifier.clip(MaterialShapes.<Shape>.toShape())",
      listOf(
        PropDef("shape", PropKind.Enum(MaterialShape.entries.map { it.wire }), required = true),
        child("content", required = true),
        color("color", "Fill behind the child."),
      ),
    ),
  )

private fun content(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Text",
      ComponentGroup.Content,
      "A run of text in a type role.",
      "Text",
      listOf(
        text("text", required = true),
        type(),
        color("color", "Content color role."),
        num("maxLines", bounds = NumberBounds(1.0, 100.0, whole = true)),
        oneOf("overflow", "ellipsis", "clip"),
        oneOf("align", "start", "center", "end"),
      ),
    ),
    ComponentDef(
      "Icon",
      ComponentGroup.Content,
      "A Material Symbols icon (Rounded, weight 400, grade 0).",
      "Icon",
      listOf(
        icon("name", required = true),
        num("size", "Size in dp; 24 by default.", NumberBounds(1.0, 512.0)),
        color("color", "Content color role."),
        bool("filled", "Fill 1 instead of Fill 0."),
        text("contentDescription"),
      ),
    ),
    ComponentDef(
      "Image",
      ComponentGroup.Content,
      "A bitmap from a file next to the proposal or a data URI.",
      "Image",
      listOf(
        PropDef(
          "src",
          PropKind.Image,
          required = true,
          description = "Path relative to the proposal, or a data: URI.",
        ),
        oneOf("fit", "crop", "fit", "fillBounds", "fillWidth", "fillHeight", "inside"),
        shape(),
        num("aspectRatio", "Width divided by height.", NumberBounds(0.1, 10.0)),
        text("contentDescription"),
      ),
    ),
  )

private fun structure(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Scaffold",
      ComponentGroup.Structure,
      "The screen frame: bars, floating action button and content.",
      "Scaffold",
      listOf(
        child("topBar", "TopAppBar"),
        child("bottomBar", "NavigationBar", "FloatingToolbar"),
        child("floatingActionButton", "FloatingActionButton", "FabMenu"),
        child("content", required = true),
        child("navigationRail", "NavigationRail"),
      ),
    ),
    ComponentDef(
      "TopAppBar",
      ComponentGroup.Structure,
      "The screen's top bar.",
      "TopAppBar, CenterAlignedTopAppBar, MediumFlexibleTopAppBar, LargeFlexibleTopAppBar",
      listOf(
        oneOf("size", "small", "center", "medium", "large"),
        text("title", required = true),
        text("subtitle"),
        child("navigationIcon", "IconButton"),
        children("actions", "IconButton"),
      ),
    ),
    ComponentDef(
      "NavigationBar",
      ComponentGroup.Structure,
      "Bottom navigation for compact windows.",
      "ShortNavigationBar",
      listOf(children("children", "NavigationBarItem", required = true)),
    ),
    ComponentDef(
      "NavigationBarItem",
      ComponentGroup.Structure,
      "One destination of a NavigationBar.",
      "ShortNavigationBarItem",
      listOf(
        text("label", required = true),
        icon(required = true),
        icon("selectedIcon"),
        bool("selected"),
        action(),
      ),
      interactive = true,
    ),
    ComponentDef(
      "NavigationRail",
      ComponentGroup.Structure,
      "Side navigation for medium and expanded windows.",
      "WideNavigationRail",
      listOf(children("children", "NavigationRailItem", required = true), bool("expanded")),
    ),
    ComponentDef(
      "NavigationRailItem",
      ComponentGroup.Structure,
      "One destination of a NavigationRail.",
      "WideNavigationRailItem",
      listOf(
        text("label", required = true),
        icon(required = true),
        icon("selectedIcon"),
        bool("selected"),
        action(),
      ),
      interactive = true,
    ),
    ComponentDef(
      "FloatingToolbar",
      ComponentGroup.Structure,
      "A floating toolbar of actions.",
      "HorizontalFloatingToolbar, VerticalFloatingToolbar",
      listOf(
        oneOf("orientation", "horizontal", "vertical"),
        oneOf("colors", "standard", "vibrant"),
        children("children", required = true),
      ),
    ),
    ComponentDef(
      "Card",
      ComponentGroup.Structure,
      "A container for related content.",
      "Card, ElevatedCard, OutlinedCard",
      listOf(
        oneOf("variant", "filled", "elevated", "outlined"),
        action(),
        shape(),
        children("content", required = true),
      ),
    ),
  )

private fun lists(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "List",
      ComponentGroup.Lists,
      "A list of ListItems.",
      "Column of ListItem, or of SegmentedListItem for the segmented variant",
      listOf(
        oneOf("variant", "standard", "segmented"),
        children("children", "ListItem", required = true),
      ),
    ),
    ComponentDef(
      "ListItem",
      ComponentGroup.Lists,
      "A row with a headline and optional supporting content.",
      "ListItem, SegmentedListItem",
      listOf(
        text("overline"),
        text("headline", required = true),
        text("supporting"),
        child("leading"),
        child("trailing"),
        bool("selected"),
        action(),
      ),
    ),
  )

private fun actions(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Button",
      ComponentGroup.Actions,
      "A button.",
      "Button, FilledTonalButton, ElevatedButton, OutlinedButton, TextButton",
      listOf(
        oneOf("variant", "filled", "tonal", "elevated", "outlined", "text"),
        oneOf("size", *buttonSizes),
        oneOf("shape", "round", "square"),
        text("label", required = true),
        icon(),
        action(),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "ToggleButton",
      ComponentGroup.Actions,
      "A button with an on and off state.",
      "ToggleButton, FilledTonalToggleButton, ElevatedToggleButton, OutlinedToggleButton",
      listOf(
        oneOf("variant", "filled", "tonal", "elevated", "outlined"),
        oneOf("size", *buttonSizes),
        bool("checked"),
        text("label", required = true),
        icon(),
        action(),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "ButtonGroup",
      ComponentGroup.Actions,
      "Buttons laid out as one group.",
      "ButtonGroup, or a Row of toggle buttons with ButtonGroupDefaults.connected shapes",
      listOf(
        oneOf("variant", "standard", "connected"),
        children("children", "Button", "ToggleButton", "IconButton", required = true),
      ),
    ),
    ComponentDef(
      "IconButton",
      ComponentGroup.Actions,
      "A button that shows only an icon.",
      "IconButton, FilledIconButton, FilledTonalIconButton, OutlinedIconButton, IconToggleButton",
      listOf(
        oneOf("variant", "standard", "filled", "tonal", "outlined"),
        oneOf("size", *buttonSizes),
        oneOf("width", "narrow", "uniform", "wide"),
        icon(required = true),
        bool("toggle", "Makes the button a toggle."),
        bool("checked", "State of a toggle."),
        action(),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "SplitButton",
      ComponentGroup.Actions,
      "A button with a leading action and a trailing menu.",
      "SplitButtonLayout with SplitButtonDefaults.LeadingButton and TrailingButton",
      listOf(
        oneOf("variant", "filled", "tonal", "elevated", "outlined"),
        text("label", required = true),
        icon(),
        action(),
        child("menu", "Menu"),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "FloatingActionButton",
      ComponentGroup.Actions,
      "The screen's floating action button.",
      "FloatingActionButton, MediumFloatingActionButton, LargeFloatingActionButton, ExtendedFloatingActionButton",
      listOf(
        oneOf("size", "small", "medium", "large"),
        bool("extended"),
        oneOf(
          "color",
          "primary-container",
          "primary",
          "secondary-container",
          "secondary",
          "tertiary-container",
          "tertiary",
          "surface",
        ),
        icon(required = true),
        text("label"),
        action(),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "FabMenu",
      ComponentGroup.Actions,
      "A floating action button that expands into a menu of actions.",
      "FloatingActionButtonMenu with ToggleFloatingActionButton",
      listOf(
        icon(required = true),
        bool("expanded", "Shows the menu open."),
        children("children", "FabMenuItem", required = true),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "FabMenuItem",
      ComponentGroup.Actions,
      "One action of a FabMenu.",
      "FloatingActionButtonMenuItem",
      listOf(text("label", required = true), icon(required = true), action()),
      interactive = true,
    ),
  )

private fun chipProps(selectable: Boolean): List<PropDef> =
  listOf(text("label", required = true), icon(), action()) +
    (if (selectable) listOf(bool("selected")) else emptyList()) +
    interactiveProps

private fun chips(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "AssistChip",
      ComponentGroup.Chips,
      "A chip that starts a smart or automated action.",
      "AssistChip, ElevatedAssistChip",
      listOf(oneOf("variant", *chipVariants)) + chipProps(selectable = false),
      interactive = true,
    ),
    ComponentDef(
      "FilterChip",
      ComponentGroup.Chips,
      "A chip that toggles a filter.",
      "FilterChip, ElevatedFilterChip",
      listOf(oneOf("variant", *chipVariants)) + chipProps(selectable = true),
      interactive = true,
    ),
    ComponentDef(
      "InputChip",
      ComponentGroup.Chips,
      "A chip that represents user input.",
      "InputChip",
      chipProps(selectable = true),
      interactive = true,
    ),
    ComponentDef(
      "SuggestionChip",
      ComponentGroup.Chips,
      "A chip that offers a suggestion.",
      "SuggestionChip, ElevatedSuggestionChip",
      listOf(oneOf("variant", *chipVariants)) + chipProps(selectable = false),
      interactive = true,
    ),
    ComponentDef(
      "ChipGroup",
      ComponentGroup.Chips,
      "Chips laid out as one group.",
      "Row with FilterChipDefaults.horizontalArrangement",
      listOf(
        children(
          "children",
          "AssistChip",
          "FilterChip",
          "InputChip",
          "SuggestionChip",
          required = true,
        ),
        bool("multi", "Allows more than one selected chip."),
      ),
    ),
  )

private fun inputs(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Switch",
      ComponentGroup.Inputs,
      "An on and off switch.",
      "Switch",
      listOf(bool("checked"), action()) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "Checkbox",
      ComponentGroup.Inputs,
      "A checkbox with an optional label.",
      "Checkbox",
      listOf(bool("checked"), text("label"), action()) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "Slider",
      ComponentGroup.Inputs,
      "Picks one value from a range.",
      "Slider",
      listOf(
        num("value", "Current value."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true)),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "RangeSlider",
      ComponentGroup.Inputs,
      "Picks a start and an end value from a range.",
      "RangeSlider",
      listOf(
        range("value", "[start, end]."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true)),
      ) + interactiveProps,
      interactive = true,
    ),
    ComponentDef(
      "TextField",
      ComponentGroup.Inputs,
      "A text input.",
      "TextField, OutlinedTextField",
      listOf(
        oneOf("variant", "filled", "outlined"),
        text("label"),
        text("placeholder"),
        text("value"),
        text("supporting"),
        bool("error"),
        icon("leadingIcon"),
        icon("trailingIcon"),
        text("prefix"),
        num("lines", bounds = NumberBounds(1.0, 20.0, whole = true)),
      ) + interactiveProps,
      interactive = true,
    ),
  )

private fun feedback(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "LoadingIndicator",
      ComponentGroup.Feedback,
      "An expressive indeterminate loading indicator.",
      "LoadingIndicator, ContainedLoadingIndicator",
      listOf(bool("contained")),
    ),
    ComponentDef(
      "ProgressIndicator",
      ComponentGroup.Feedback,
      "A linear or circular progress indicator; indeterminate when progress is absent.",
      "LinearProgressIndicator, CircularProgressIndicator, and the Wavy variants",
      listOf(
        oneOf("shape", "linear", "circular"),
        bool("wavy"),
        num("progress", "Completed fraction.", NumberBounds(0.0, 1.0)),
      ),
    ),
  )

private fun overlays(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "BottomSheet",
      ComponentGroup.Overlays,
      "A modal sheet from the bottom edge, with a drag handle.",
      "ModalBottomSheet with BottomSheetDefaults.DragHandle",
      listOf(children("content", required = true)),
      overlay = true,
    ),
    ComponentDef(
      "Dialog",
      ComponentGroup.Overlays,
      "A modal dialog.",
      "AlertDialog",
      listOf(
        text("title", required = true),
        text("text"),
        child("confirm", "Button"),
        child("dismiss", "Button"),
      ),
      overlay = true,
    ),
    ComponentDef(
      "Menu",
      ComponentGroup.Overlays,
      "A popup menu anchored to a component.",
      "DropdownMenuPopup with DropdownMenuGroup and DropdownMenuItem",
      listOf(child("anchor"), children("items", "MenuItem", required = true)),
      overlay = true,
    ),
    ComponentDef(
      "MenuItem",
      ComponentGroup.Overlays,
      "One entry of a Menu.",
      "DropdownMenuItem",
      listOf(text("label", required = true), icon(), action()),
      interactive = true,
    ),
  )

// endregion
