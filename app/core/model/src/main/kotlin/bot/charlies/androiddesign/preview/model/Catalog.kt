package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/** What a prop's value may be. Any [String], [Boolean] or [Number] prop may also be a binding. */
sealed interface PropKind {
  /** The id of one component. */
  data object Child : PropKind

  /**
   * The id of the component this one is drawn beside or below: a menu's button, a tooltip's target,
   * a docked date picker's text field. A reference, not a child: the anchor is drawn where its own
   * parent puts it. On the wire it is written like a [Child].
   */
  data object Anchor : PropKind

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

  /**
   * A badge on an icon: `true` draws a small badge, a number or a string of at most
   * [BADGE_MAX_CHARACTERS] characters draws a large badge with that text, `false` draws none.
   */
  data object Badge : PropKind

  companion object {
    /** The most characters a large badge's label holds, a `+` included. */
    const val BADGE_MAX_CHARACTERS = 4
  }
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
 * One prop of a component. For [PropKind.Child], [PropKind.Anchor] and [PropKind.ChildList],
 * [allowedChildren] lists the component names it may reference, or is null when any component is
 * allowed.
 *
 * @property default The value the renderer and the exporter use when a proposal leaves the prop
 *   out, or null when the prop has no fixed default. This is the only place a default is written
 *   down, and the catalog shows it to agents so they can omit it.
 */
data class PropDef(
  val name: String,
  val kind: PropKind,
  val required: Boolean = false,
  val description: String = "",
  val allowedChildren: Set<String>? = null,
  val default: JsonPrimitive? = null,
)

/** When a component stretches to its parent's width. */
sealed interface WidthFill {
  /** It keeps the width its content gives it. */
  data object Never : WidthFill

  data object Always : WidthFill

  /** Only when the proposal gives [prop] a value, such as a Text with an `align`. */
  data class WhenSet(val prop: String) : WidthFill

  /** Unless [prop] has the value [value], such as a vertical Divider. */
  data class UnlessIs(val prop: String, val value: String) : WidthFill
}

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
 * @property width When the component stretches to its parent's width, if the parent allows it.
 * @property stretchesChildren True when the parent lets its children stretch to its width (a Column
 *   does; a Row does not, since that would push its siblings out: they use `weight`). The renderer
 *   and the exporter both read these two, so a stretched child looks the same in both.
 * @property m3Pages The slugs of the Material 3 component pages this entry implements
 *   (`sources/m3.material.io/pages/components/<slug>.md`). Entries may share a page (`Tabs` and
 *   `Tab`), and an entry claims the page of each variant it draws (`FloatingActionButton` claims
 *   `extended-fab`). Empty for layout primitives and content parts that no page describes. A page
 *   that no entry claims must be in [Catalog.m3Omissions]; a script test enforces it.
 */
data class ComponentDef(
  val name: String,
  val group: ComponentGroup,
  val description: String,
  val compose: String,
  val props: List<PropDef>,
  val interactive: Boolean = false,
  val overlay: Boolean = false,
  val width: WidthFill = WidthFill.Never,
  val stretchesChildren: Boolean = false,
  val m3Pages: List<String> = emptyList(),
) {
  fun prop(name: String): PropDef? = props.firstOrNull { it.name == name }

  /**
   * Whether a node of this component stretches to the width of a parent that lets it. [valueOf] is
   * the node's value of a prop, or null when it has none; a catalog default counts as a value.
   */
  fun fillsWidth(valueOf: (String) -> JsonElement?): Boolean {
    fun value(prop: String) = valueOf(prop) ?: defaultOf(prop)
    return when (val rule = width) {
      WidthFill.Never -> false
      WidthFill.Always -> true
      is WidthFill.WhenSet -> value(rule.prop).let { it != null && it !is JsonNull }
      is WidthFill.UnlessIs -> (value(rule.prop) as? JsonPrimitive)?.content != rule.value
    }
  }

  /**
   * A bound number is not validated, so the renderer and the exporter both move it into the bounds
   * the catalog gives number prop [name] (a common prop included): a value outside them, or one
   * that is not a finite number, would reach a Compose API that throws or hangs on it.
   */
  fun coerceNumber(name: String, value: Double): Double =
    ((props + COMMON_PROPS).firstOrNull { it.name == name }?.kind as? PropKind.Number)
      ?.bounds
      ?.coerce(value) ?: value

  /** The catalog default of prop [name], or null when it is not a prop or has no default. */
  fun defaultOf(name: String): JsonPrimitive? = prop(name)?.default
}

/** Why the catalog does not draw a Material 3 component page. */
enum class OmissionReason(val wire: String) {
  /** Material no longer recommends the component, and names its replacement. */
  Retired("retired"),

  /** Material specifies it, but Compose has no API for it yet on the build that renders it. */
  NoComposeApi("no-compose-api"),
}

/**
 * A Material 3 component page the catalog deliberately does not draw.
 *
 * @property page The page slug, as in [ComponentDef.m3Pages].
 * @property why One sentence: what replaces it, or what Compose lacks.
 * @property waitsFor For [OmissionReason.NoComposeApi], the desktop material3 version that is
 *   expected to bring the API, when known.
 */
data class M3Omission(
  val page: String,
  val reason: OmissionReason,
  val why: String,
  val waitsFor: String? = null,
)

/** Renderer-only functions an `action` may call. */
enum class ActionFunction(val wire: String, val argument: String?, val description: String) {
  Navigate("navigate", "screen", "Opens the screen with this id."),
  Back("back", null, "Goes back one screen."),
  Open(
    "open",
    "overlay",
    "Opens a BottomSheet, Dialog, Menu, SideSheet, SearchView, Snackbar, Tooltip, DatePicker " +
      "or TimePicker of the same " +
      "screen by id.",
  ),
  Dismiss("dismiss", null, "Closes the open overlay.");

  companion object {
    fun fromWire(value: String): ActionFunction? = entries.firstOrNull { it.wire == value }
  }
}

/** The width at which a `sizeClass` override [key] starts to apply, in dp. */
data class SizeBreakpoint(val key: String, val minWidthDp: Int)

/** Props every component of every catalog accepts besides the ones in [Catalog]'s constants. */
internal val COMMON_PROPS: List<PropDef> =
  listOf(
    PropDef(
      "weight",
      PropKind.Number(NumberBounds(0.01, 100.0)),
      description = "Relative weight inside a Row or Column.",
    ),
    PropDef("padding", PropKind.Spacing, description = "Padding around the component."),
  )

/**
 * A component registry: the single source of truth, per surface, for the A2UI catalog JSON, the
 * proposal schema, the validator, the renderer and the exporter. A proposal draws from one catalog:
 * every screen names the same [id]. [Catalogs] finds one by id or key.
 */
interface Catalog {
  val id: String

  /** The short name an agent asks for (`get_catalog`) and the catalog file is named after. */
  val key: String
  val title: String

  /** One sentence for the catalog JSON. */
  val description: String
  val components: List<ComponentDef>

  /** Material 3 component pages this catalog deliberately does not draw. */
  val m3Omissions: List<M3Omission>
    get() = emptyList()

  /** Props every component accepts besides `id`, `component`, `showIn`, `sizeClass` and `style`. */
  val commonProps: List<PropDef>

  /** The views a proposal of this catalog is checked and rendered in. */
  val views: List<ViewSpec>

  /**
   * The keys of the `sizeClass` override prop, each with the view width it starts at. Empty when
   * the catalog has no overrides.
   */
  val sizeBreakpoints: List<SizeBreakpoint>

  /** The size classes `showIn` accepts. Empty when the catalog has no `showIn`. */
  val showInSizes: List<SizeClass>

  /** [ColorRole]-kind props, theme `colors` and styles take these wire names. */
  val colorRoles: List<String>

  /** [TypeRole]-kind props and theme `typography` take these wire names, each in a type group. */
  val typeRoles: Map<String, TypeGroup>

  /** Interactive components are measured against this touch area, in dp. */
  val minTouchTargetDp: Float

  /** Whether `navigation.panes` may name pane roles (list-detail layouts). */
  val supportsPanes: Boolean

  /** Names of the components that open as overlays. */
  val overlayNames: Set<String>

  /** Overlays that dim and disable the screen behind them while open. */
  val modalNames: Set<String>

  /** Names of the floating action button components; a pane may hold only one. */
  val fabNames: Set<String>

  /** Names of the navigation components, in the order a view lists them. */
  val navigationNames: List<String>

  /**
   * Components made for the round display's edge: a full-screen ring, an edge button, the clock.
   * The round-edge rule leaves them alone. Empty on a catalog with no round views.
   */
  val roundEdgeNames: Set<String>
    get() = emptySet()

  /**
   * Typefaces the platform's own components are set in, which the font rule accepts for every type
   * role besides the bundled ones. Empty when the catalog uses the bundled fonts only.
   */
  val platformFontFamilies: Set<String>
    get() = emptySet()

  fun find(name: String): ComponentDef?

  companion object {
    const val PROTOCOL_VERSION = "1.0"
    const val SHOW_IN = "showIn"
    const val SIZE_CLASS = "sizeClass"
    const val STYLE = "style"
    const val ITEM_SIZE = "itemSize"
  }
}

/** The catalogs a proposal can name, found by id or key. */
object Catalogs {
  val all: List<Catalog> by lazy { listOf(M3ExpressiveCatalog, WearCatalog) }

  fun byId(id: String): Catalog? = all.firstOrNull { it.id == id }

  fun byKey(key: String): Catalog? = all.firstOrNull { it.key == key }

  /** The catalog of [screen]; the phone catalog when it names none we know. */
  fun of(screen: Screen): Catalog = byId(screen.catalogId) ?: M3ExpressiveCatalog

  /**
   * The catalog of [proposal]: its start screen's, else its first screen's. The validator makes
   * every screen name the same one, so any screen of a valid proposal gives the answer.
   */
  fun of(proposal: Proposal): Catalog =
    (proposal.screens[proposal.navigation.start] ?: proposal.screens.values.firstOrNull())?.let(
      ::of,
    ) ?: M3ExpressiveCatalog

  /** The catalog whose views include [device]: a watch is Wear, everything else the phone's. */
  fun forDevice(device: Device): Catalog =
    if (device == Device.Watch) WearCatalog else M3ExpressiveCatalog

  /** The catalog [view] belongs to. */
  fun forView(view: ViewSpec): Catalog = forDevice(view.device)
}

object M3ExpressiveCatalog : Catalog {
  const val ID = "https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1"
  override val id = ID
  const val PROTOCOL_VERSION = Catalog.PROTOCOL_VERSION
  const val TITLE = "Material 3 Expressive for Android"
  const val KEY = "m3-expressive"

  override val key = KEY
  override val title = TITLE
  override val description =
    "Material 3 Expressive components that map one to one onto Jetpack Compose material3."

  override val commonProps: List<PropDef> =
    COMMON_PROPS +
      oneOf(
        Catalog.ITEM_SIZE,
        *CarouselItemSize.entries.map { it.wire }.toTypedArray(),
        description =
          "Only on a node inside a Carousel item: the smallest item size the node shows at. " +
            "large shows it only while its item is at full size; medium hides it only on the " +
            "smallest item. Leave it out to always show the node.",
      )

  /** The most columns a `Grid` has. */
  const val MAX_GRID_COLUMNS = 24

  const val SHOW_IN = Catalog.SHOW_IN
  const val SIZE_CLASS = Catalog.SIZE_CLASS
  const val STYLE = Catalog.STYLE
  const val ITEM_SIZE = Catalog.ITEM_SIZE

  override val components: List<ComponentDef> by lazy { buildComponents() }

  override val m3Omissions =
    listOf(
      M3Omission(
        "navigation-drawer",
        OmissionReason.Retired,
        "Material 3 Expressive no longer recommends it; use an expanded navigation rail.",
      ),
      M3Omission(
        "segmented-buttons",
        OmissionReason.Retired,
        "Material 3 Expressive no longer recommends it; use a connected button group.",
      ),
    )

  private val byName: Map<String, ComponentDef> by lazy { components.associateBy { it.name } }

  override fun find(name: String): ComponentDef? = byName[name]

  override val views: List<ViewSpec>
    get() = Views.all

  override val sizeBreakpoints =
    listOf(
      SizeBreakpoint(SizeClass.Medium.wire, SizeClass.MEDIUM_MIN_DP),
      SizeBreakpoint(SizeClass.Expanded.wire, SizeClass.EXPANDED_MIN_DP),
    )

  override val showInSizes: List<SizeClass> = SizeClass.entries

  override val colorRoles: List<String> = ColorRole.entries.map { it.wire }

  override val typeRoles: Map<String, TypeGroup> =
    TypeRole.entries.associate { it.wire to it.group }

  override val minTouchTargetDp = 48f

  override val supportsPanes = true

  /** Names of the components that open as overlays. */
  override val overlayNames: Set<String> by lazy {
    components.filter { it.overlay }.map { it.name }.toSet()
  }

  override val modalNames: Set<String> =
    setOf("BottomSheet", "Dialog", "SideSheet", "SearchView", "DatePicker", "TimePicker")

  override val fabNames: Set<String> = setOf("FloatingActionButton", "FabMenu")

  /** Names of the navigation components, in the order a view lists them. */
  override val navigationNames: List<String> =
    listOf("NavigationBar", "NavigationRail", "FloatingToolbar", "DockedToolbar")
}

// region registry definitions

internal fun text(name: String, required: Boolean = false, description: String = "") =
  PropDef(name, PropKind.Text, required, description)

internal fun bool(name: String, description: String = "", default: Boolean? = false) =
  PropDef(
    name,
    PropKind.Bool,
    description = description,
    default = default?.let(::JsonPrimitive),
  )

internal fun num(
  name: String,
  description: String = "",
  bounds: NumberBounds? = null,
  default: Int? = null,
) =
  PropDef(
    name,
    PropKind.Number(bounds),
    description = description,
    default = default?.let(::JsonPrimitive),
  )

internal fun range(name: String, description: String = "") =
  PropDef(name, PropKind.NumberRange, description = description)

internal fun oneOf(
  name: String,
  vararg values: String,
  description: String = "",
  default: String? = null,
) =
  PropDef(
    name,
    PropKind.Enum(values.toList()),
    description = description,
    default = default?.let(::JsonPrimitive),
  )

internal fun child(
  name: String,
  vararg allowed: String,
  required: Boolean = false,
  description: String = "",
) = PropDef(name, PropKind.Child, required, description, allowed.toSet().ifEmpty { null })

internal fun anchor(
  name: String,
  vararg allowed: String,
  required: Boolean = false,
  description: String = "",
) = PropDef(name, PropKind.Anchor, required, description, allowed.toSet().ifEmpty { null })

internal fun children(
  name: String,
  vararg allowed: String,
  required: Boolean = false,
  description: String = "",
) = PropDef(name, PropKind.ChildList, required, description, allowed.toSet().ifEmpty { null })

internal fun color(name: String, description: String = "") =
  PropDef(name, PropKind.ColorRole, description = description)

internal fun type(name: String = "type") = PropDef(name, PropKind.TypeRole)

internal fun shape(name: String = "shape") = PropDef(name, PropKind.Shape)

internal fun spacing(name: String, description: String = "") =
  PropDef(name, PropKind.Spacing, description = description)

internal fun icon(
  name: String = "icon",
  required: Boolean = false,
  description: String = "Material Symbols name, such as \"home\".",
) = PropDef(name, PropKind.Icon, required, description)

internal fun action() = PropDef("action", PropKind.Action, description = "What a tap does.")

internal fun badge() =
  PropDef(
    "badge",
    PropKind.Badge,
    description =
      "A badge on the icon's upper trailing corner: true draws a small one, a number or a " +
        "string of at most ${PropKind.BADGE_MAX_CHARACTERS} characters (such as \"99+\") " +
        "draws a large one with that text.",
  )

internal val interactiveProps =
  listOf(
    bool("primary", "Marks the screen's primary action. Exactly one is expected per screen."),
    text("contentDescription", description = "Accessibility label when it differs from the label."),
    bool("enabled", default = true),
  )

internal val buttonSizes = arrayOf("extra-small", "small", "medium", "large", "extra-large")
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

internal fun layout(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Column",
      ComponentGroup.Layout,
      "Arranges children vertically.",
      "Column",
      listOf(
        children("children", required = true),
        spacing("spacing", "Gap between children (Arrangement.spacedBy)."),
        oneOf("horizontalAlignment", "start", "center", "end", default = "start"),
        oneOf("arrangement", *arrangements, default = "start"),
        bool("scroll", "Makes the column vertically scrollable."),
      ),
      stretchesChildren = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "Row",
      ComponentGroup.Layout,
      "Arranges children horizontally.",
      "Row",
      listOf(
        children("children", required = true),
        spacing("spacing", "Gap between children (Arrangement.spacedBy)."),
        oneOf(
          "verticalAlignment",
          "top",
          "center",
          "bottom",
          "stretch",
          default = "top",
          description =
            "stretch: Column, Row, and Box children fill the row's height, for full-height panes.",
        ),
        oneOf("arrangement", *arrangements, default = "start"),
      ),
      width = WidthFill.Always,
    ),
    ComponentDef(
      "Box",
      ComponentGroup.Layout,
      "Stacks children on top of each other.",
      "Box",
      listOf(
        children("children", required = true),
        oneOf("contentAlignment", *alignments, default = "topStart"),
      ),
      width = WidthFill.Always,
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
      stretchesChildren = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "FlexBox",
      ComponentGroup.Layout,
      "Lays children out in a row that wraps.",
      "FlexBox (experimental Compose FlexBox)",
      listOf(
        children("children", required = true),
        bool("wrap", default = true),
        spacing("gap"),
      ),
      width = WidthFill.Always,
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
      width = WidthFill.UnlessIs("vertical", "true"),
      m3Pages = listOf("divider"),
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
        oneOf("overflow", "ellipsis", "clip", default = "clip"),
        oneOf("align", "start", "center", "end"),
      ),
      width = WidthFill.WhenSet("align"),
    ),
    ComponentDef(
      "Icon",
      ComponentGroup.Content,
      "A Material Symbols icon (Rounded, weight 400, grade 0).",
      "Icon",
      listOf(
        icon("name", required = true),
        num("size", "Size in dp.", NumberBounds(1.0, 512.0), default = 24),
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
        oneOf(
          "fit",
          "crop",
          "fit",
          "fillBounds",
          "fillWidth",
          "fillHeight",
          "inside",
          default = "fit",
        ),
        shape(),
        num("aspectRatio", "Width divided by height.", NumberBounds(0.1, 10.0)),
        text("contentDescription"),
      ),
      width = WidthFill.WhenSet("aspectRatio"),
    ),
    ComponentDef(
      "Carousel",
      ComponentGroup.Content,
      "A horizontally scrolling collection of visual items, clipped to the carousel's item shape.",
      "HorizontalMultiBrowseCarousel, HorizontalCenteredHeroCarousel, " +
        "HorizontalUncontainedCarousel",
      listOf(
        oneOf(
          "variant",
          "multi-browse",
          "hero",
          "centered-hero",
          "uncontained",
          "full-screen",
          default = "multi-browse",
          description =
            "hero and full-screen have no composable of their own: they size the items of a " +
              "multi-browse and an uncontained carousel.",
        ),
        num(
          "itemWidth",
          "Item width in dp: the preferred width of multi-browse, the width of uncontained. " +
            "The other variants size their items themselves.",
          NumberBounds(40.0, 1000.0),
          default = 186,
        ),
        children("children", required = true),
      ),
      stretchesChildren = true,
      width = WidthFill.Always,
      m3Pages = listOf("carousel"),
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
        child("bottomBar", "NavigationBar", "FloatingToolbar", "DockedToolbar"),
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
        oneOf("size", "small", "center", "medium", "large", "search", default = "small"),
        text(
          "title",
          required = true,
          description =
            "The heading. In a search app bar it is the search field's hint, which includes the " +
              "word Search (such as \"Search inbox\").",
        ),
        text("subtitle"),
        child("navigationIcon", "IconButton"),
        children(
          "actions",
          "IconButton",
          description =
            "The trailing icon buttons. A search app bar has at most two at compact widths, an " +
              "avatar coming in addition, and up to four from medium widths up (override " +
              "`actions` in `sizeClass`).",
        ),
        PropDef(
          "avatar",
          PropKind.Image,
          description =
            "Search size only: a profile picture, drawn as a 30dp circle after the actions. " +
              "Path relative to the proposal, or a data: URI.",
        ),
        child(
          "expanded",
          "SearchView",
          description =
            "Search size only: the SearchView that opens when the search field is tapped.",
        ),
      ),
      width = WidthFill.Always,
      m3Pages = listOf("app-bars"),
    ),
    ComponentDef(
      "NavigationBar",
      ComponentGroup.Structure,
      "Bottom navigation for compact windows.",
      "ShortNavigationBar",
      listOf(children("children", "NavigationBarItem", required = true)),
      m3Pages = listOf("navigation-bar"),
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
        badge(),
        action(),
      ),
      interactive = true,
      m3Pages = listOf("navigation-bar", "badges"),
    ),
    ComponentDef(
      "Tabs",
      ComponentGroup.Structure,
      "A row of tabs that switches between sibling views at the same level of hierarchy.",
      "PrimaryTabRow, SecondaryTabRow, PrimaryScrollableTabRow, SecondaryScrollableTabRow",
      listOf(
        oneOf("variant", "primary", "secondary", default = "primary"),
        bool("scrollable", "Lets the tabs scroll when they do not fit."),
        children("children", "Tab", required = true),
      ),
      width = WidthFill.Always,
      m3Pages = listOf("tabs"),
    ),
    ComponentDef(
      "Tab",
      ComponentGroup.Structure,
      "One tab of a Tabs row. Its content is the screen its action navigates to.",
      "Tab",
      listOf(text("label", required = true), icon(), bool("selected"), badge(), action()),
      interactive = true,
      m3Pages = listOf("tabs", "badges"),
    ),
    ComponentDef(
      "NavigationRail",
      ComponentGroup.Structure,
      "Side navigation for medium and expanded windows.",
      "WideNavigationRail",
      listOf(children("children", "NavigationRailItem", required = true), bool("expanded")),
      m3Pages = listOf("navigation-rail"),
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
        badge(),
        action(),
      ),
      interactive = true,
      m3Pages = listOf("navigation-rail", "badges"),
    ),
    ComponentDef(
      "FloatingToolbar",
      ComponentGroup.Structure,
      "A floating toolbar of actions.",
      "HorizontalFloatingToolbar, VerticalFloatingToolbar",
      listOf(
        oneOf("orientation", "horizontal", "vertical", default = "horizontal"),
        oneOf("colors", "standard", "vibrant", default = "standard"),
        children("children", required = true),
      ),
      m3Pages = listOf("toolbars"),
    ),
    ComponentDef(
      "DockedToolbar",
      ComponentGroup.Structure,
      "A toolbar docked to the bottom of the window, full width, in the Scaffold's bottomBar " +
        "slot. It replaces the bottom app bar and is never shown with a navigation bar.",
      "FlexibleBottomAppBar",
      listOf(
        oneOf("colors", "standard", "vibrant", default = "standard"),
        children("children", required = true),
      ),
      width = WidthFill.Always,
      m3Pages = listOf("toolbars"),
    ),
    ComponentDef(
      "Card",
      ComponentGroup.Structure,
      "A container for related content.",
      "Card, ElevatedCard, OutlinedCard",
      listOf(
        oneOf("variant", "filled", "elevated", "outlined", default = "filled"),
        action(),
        shape(),
        children("content", required = true),
      ),
      stretchesChildren = true,
      width = WidthFill.Always,
      m3Pages = listOf("cards"),
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
        oneOf("variant", "standard", "segmented", default = "standard"),
        children("children", "ListItem", required = true),
      ),
      stretchesChildren = true,
      width = WidthFill.Always,
      m3Pages = listOf("lists"),
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
      width = WidthFill.Always,
      m3Pages = listOf("lists"),
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
        oneOf("variant", "filled", "tonal", "elevated", "outlined", "text", default = "filled"),
        oneOf("size", *buttonSizes, default = "small"),
        oneOf("shape", "round", "square", default = "round"),
        text("label", required = true),
        icon(),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("buttons"),
    ),
    ComponentDef(
      "ToggleButton",
      ComponentGroup.Actions,
      "A button with an on and off state.",
      "ToggleButton, FilledTonalToggleButton, ElevatedToggleButton, OutlinedToggleButton",
      listOf(
        oneOf("variant", "filled", "tonal", "elevated", "outlined", default = "filled"),
        oneOf("size", *buttonSizes, default = "small"),
        bool("checked"),
        text("label", required = true),
        icon(),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("buttons"),
    ),
    ComponentDef(
      "ButtonGroup",
      ComponentGroup.Actions,
      "Buttons laid out as one group.",
      "ButtonGroup, or a Row of toggle buttons with ButtonGroupDefaults.connected shapes",
      listOf(
        oneOf("variant", "standard", "connected", default = "standard"),
        children("children", "Button", "ToggleButton", "IconButton", required = true),
      ),
      m3Pages = listOf("button-groups"),
    ),
    ComponentDef(
      "IconButton",
      ComponentGroup.Actions,
      "A button that shows only an icon.",
      "IconButton, FilledIconButton, FilledTonalIconButton, OutlinedIconButton, IconToggleButton",
      listOf(
        oneOf("variant", "standard", "filled", "tonal", "outlined", default = "standard"),
        oneOf("size", *buttonSizes, default = "small"),
        oneOf("shape", "round", "square", default = "round"),
        oneOf("width", "narrow", "uniform", "wide", default = "uniform"),
        icon(required = true),
        badge(),
        bool("toggle", "Makes the button a toggle."),
        bool("checked", "State of a toggle."),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("icon-buttons", "badges"),
    ),
    ComponentDef(
      "SplitButton",
      ComponentGroup.Actions,
      "A button with a leading action and a trailing menu.",
      "SplitButtonLayout with SplitButtonDefaults.LeadingButton and TrailingButton",
      listOf(
        oneOf("variant", "filled", "tonal", "elevated", "outlined", default = "filled"),
        text("label", required = true),
        icon(),
        action(),
        child("menu", "Menu"),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("split-button"),
    ),
    ComponentDef(
      "FloatingActionButton",
      ComponentGroup.Actions,
      "The screen's floating action button.",
      "FloatingActionButton, MediumFloatingActionButton, LargeFloatingActionButton, ExtendedFloatingActionButton",
      listOf(
        oneOf("size", "small", "medium", "large", default = "small"),
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
          default = "primary-container",
        ),
        icon(required = true),
        text("label"),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("floating-action-button", "extended-fab"),
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
      m3Pages = listOf("fab-menu"),
    ),
    ComponentDef(
      "FabMenuItem",
      ComponentGroup.Actions,
      "One action of a FabMenu.",
      "FloatingActionButtonMenuItem",
      listOf(text("label", required = true), icon(required = true), action()),
      interactive = true,
      m3Pages = listOf("fab-menu"),
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
      listOf(oneOf("variant", *chipVariants, default = "outlined")) + chipProps(selectable = false),
      interactive = true,
      m3Pages = listOf("chips"),
    ),
    ComponentDef(
      "FilterChip",
      ComponentGroup.Chips,
      "A chip that toggles a filter.",
      "FilterChip, ElevatedFilterChip",
      listOf(oneOf("variant", *chipVariants, default = "outlined")) + chipProps(selectable = true),
      interactive = true,
      m3Pages = listOf("chips"),
    ),
    ComponentDef(
      "InputChip",
      ComponentGroup.Chips,
      "A chip that represents user input.",
      "InputChip",
      chipProps(selectable = true),
      interactive = true,
      m3Pages = listOf("chips"),
    ),
    ComponentDef(
      "SuggestionChip",
      ComponentGroup.Chips,
      "A chip that offers a suggestion.",
      "SuggestionChip, ElevatedSuggestionChip",
      listOf(oneOf("variant", *chipVariants, default = "outlined")) + chipProps(selectable = false),
      interactive = true,
      m3Pages = listOf("chips"),
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
      width = WidthFill.Always,
      m3Pages = listOf("chips"),
    ),
  )

private fun inputs(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Switch",
      ComponentGroup.Inputs,
      "An on and off switch.",
      "Switch",
      listOf(
        bool("checked"),
        oneOf(
          "icons",
          "none",
          "selected",
          "both",
          description =
            "The icon in the thumb: a check when on (`selected`), plus a close when off (`both`).",
          default = "none",
        ),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("switch"),
    ),
    ComponentDef(
      "Checkbox",
      ComponentGroup.Inputs,
      "A checkbox with an optional label.",
      "Checkbox",
      listOf(
        bool("checked"),
        bool(
          "indeterminate",
          "Partly selected, such as a parent whose children are only partly checked. It shows a " +
            "dash and takes the place of `checked`; a tap checks the box.",
        ),
        text("label"),
        action(),
      ) + interactiveProps,
      interactive = true,
      m3Pages = listOf("checkbox"),
    ),
    ComponentDef(
      "Slider",
      ComponentGroup.Inputs,
      "Picks one value from a range.",
      "Slider",
      listOf(
        num("value", "Current value."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true), default = 0),
        oneOf(
          "variant",
          "standard",
          "centered",
          description =
            "Centered fills from the middle of the range to the handle, for a range that " +
              "crosses zero.",
          default = "standard",
        ),
        oneOf(
          "orientation",
          "horizontal",
          "vertical",
          description =
            "A vertical slider has a fixed height, so it never stretches across the width.",
          default = "horizontal",
        ),
      ) + interactiveProps,
      interactive = true,
      width = WidthFill.UnlessIs("orientation", "vertical"),
      m3Pages = listOf("sliders"),
    ),
    ComponentDef(
      "RangeSlider",
      ComponentGroup.Inputs,
      "Picks a start and an end value from a range.",
      "RangeSlider",
      listOf(
        range("value", "[start, end]."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true), default = 0),
      ) + interactiveProps,
      interactive = true,
      width = WidthFill.Always,
      m3Pages = listOf("sliders"),
    ),
    ComponentDef(
      "RadioGroup",
      ComponentGroup.Inputs,
      "A vertical list of radio buttons. Exactly one is selected, and a tap moves the selection.",
      "Column with Modifier.selectableGroup()",
      listOf(children("children", "RadioButton", required = true)),
      stretchesChildren = true,
      width = WidthFill.Always,
      m3Pages = listOf("radio-button"),
    ),
    ComponentDef(
      "RadioButton",
      ComponentGroup.Inputs,
      "One option of a RadioGroup, with its label. The whole row is the touch target.",
      "RadioButton in a Row with Modifier.selectable(role = Role.RadioButton)",
      listOf(text("label", required = true), bool("selected"), action()) + interactiveProps,
      interactive = true,
      width = WidthFill.Always,
      m3Pages = listOf("radio-button"),
    ),
    ComponentDef(
      "TextField",
      ComponentGroup.Inputs,
      "A text input.",
      "TextField, OutlinedTextField",
      listOf(
        oneOf("variant", "filled", "outlined", default = "filled"),
        text("label"),
        text("placeholder"),
        text("value"),
        text("supporting"),
        bool("error"),
        icon("leadingIcon"),
        icon("trailingIcon"),
        text("prefix"),
        num("lines", bounds = NumberBounds(1.0, 20.0, whole = true), default = 1),
      ) + interactiveProps,
      interactive = true,
      width = WidthFill.Always,
      m3Pages = listOf("text-fields"),
    ),
    ComponentDef(
      "SearchBar",
      ComponentGroup.Inputs,
      "A search field. Tapping it opens the SearchView named by `expanded`.",
      "SearchBar with SearchBarDefaults.InputField and rememberSearchBarState",
      listOf(
        text(
          "placeholder",
          required = true,
          description = "The hint, a short description of what can be searched (\"Search mail\").",
        ),
        PropDef(
          "leadingIcon",
          PropKind.Icon,
          description = "A non-functional icon at the start. Material Symbols name.",
          default = JsonPrimitive("search"),
        ),
        child(
          "navigationIcon",
          "IconButton",
          description = "A navigational icon button (menu, arrow) that replaces the leading icon.",
        ),
        children(
          "trailing",
          "IconButton",
          description = "Trailing icon buttons. At most two, an avatar counting as one.",
        ),
        PropDef(
          "avatar",
          PropKind.Image,
          description =
            "A profile picture, drawn as a 30dp circle after the trailing icon buttons. Path " +
              "relative to the proposal, or a data: URI.",
        ),
        child("expanded", "SearchView"),
      ) + interactiveProps,
      interactive = true,
      width = WidthFill.Always,
      m3Pages = listOf("search"),
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
      m3Pages = listOf("loading-indicator"),
    ),
    ComponentDef(
      "ProgressIndicator",
      ComponentGroup.Feedback,
      "A linear or circular progress indicator; indeterminate when progress is absent.",
      "LinearProgressIndicator, CircularProgressIndicator, and the Wavy variants",
      listOf(
        oneOf("shape", "linear", "circular", default = "linear"),
        bool("wavy"),
        num("progress", "Completed fraction.", NumberBounds(0.0, 1.0)),
      ),
      width = WidthFill.UnlessIs("shape", "circular"),
      m3Pages = listOf("progress-indicators"),
    ),
  )

private fun overlays(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "BottomSheet",
      ComponentGroup.Overlays,
      "A modal sheet from the bottom edge, with a drag handle. Its content spans the sheet edge to " +
        "edge, as ModalBottomSheet adds no side padding: put it in a Column with padding.",
      "ModalBottomSheet with BottomSheetDefaults.DragHandle",
      listOf(children("content", required = true)),
      overlay = true,
      stretchesChildren = true,
      m3Pages = listOf("bottom-sheets"),
    ),
    ComponentDef(
      "Dialog",
      ComponentGroup.Overlays,
      "A modal dialog. Basic is for alerts, quick selection and confirmation. Full-screen is " +
        "for tasks with several steps, keyboard input or changes that are not saved at once, " +
        "and only for compact windows: from medium up, use a basic dialog.",
      "AlertDialog; full-screen: Dialog (full window) holding a Scaffold with a TopAppBar, " +
        "composed; no full-screen dialog composable",
      listOf(
        oneOf("variant", "basic", "full-screen", default = "basic"),
        text("title", required = true, description = "The headline."),
        text("text", description = "Basic only: the supporting text."),
        child(
          "confirm",
          "Button",
          description =
            "The confirming text button. Full-screen: in the header, labeled by its outcome " +
              "(Save, Send, Create), never disabled.",
        ),
        child("dismiss", "Button", description = "Basic only: the dismissive text button."),
        children("content", description = "Full-screen only: the body below the header."),
      ),
      overlay = true,
      stretchesChildren = true,
      m3Pages = listOf("dialogs"),
    ),
    ComponentDef(
      "DatePicker",
      ComponentGroup.Overlays,
      "A date or date range picker. Modal is a calendar in a dialog, modal-input is keyboard " +
        "entry in a dialog, docked is a dropdown calendar below the text field named by " +
        "`anchor`, and full-screen (compact windows) fills the window. Dates are ISO " +
        "(\"2026-11-06\") and the picker opens on the month of the selected one.",
      "DatePickerDialog holding DatePicker or DateRangePicker; docked: Popup below the text " +
        "field; full-screen: Dialog (full window) holding a Scaffold, composed",
      listOf(
        oneOf("variant", "modal", "modal-input", "docked", "full-screen", default = "modal"),
        bool("range", "Picks a range of dates (DateRangePicker) instead of one."),
        text("selected", description = "The selected date, ISO (YYYY-MM-DD). Not for a range."),
        text(
          "start",
          description = "A range's first date, ISO (YYYY-MM-DD). Required for a range.",
        ),
        text("end", description = "A range's last date, ISO, on or after `start`."),
        anchor(
          "anchor",
          "TextField",
          description = "The text field a docked picker opens below. Required when docked.",
        ),
      ),
      overlay = true,
      m3Pages = listOf("date-pickers"),
    ),
    ComponentDef(
      "TimePicker",
      ComponentGroup.Overlays,
      "A time picker in a dialog: a dial, or keyboard entry (input). The keyboard icon switches " +
        "between them. Whether the clock is 12 or 24 hours is the device's setting; `is24Hour` " +
        "only sets it for the preview.",
      "TimePickerDialog holding TimePicker or TimeInput",
      listOf(
        oneOf("variant", "dial", "input", default = "dial"),
        text("time", required = true, description = "The selected time, 24-hour HH:mm."),
        bool(
          "is24Hour",
          "Preview only: a 24-hour clock, with no AM/PM selector. The export leaves the clock " +
            "format to the device, as Material does.",
        ),
      ),
      overlay = true,
      m3Pages = listOf("time-pickers"),
    ),
    ComponentDef(
      "Menu",
      ComponentGroup.Overlays,
      "A popup menu anchored to a component.",
      "DropdownMenuPopup with DropdownMenuGroup and DropdownMenuItem",
      listOf(
        anchor("anchor"),
        oneOf(
          "colors",
          "standard",
          "vibrant",
          description = "Vibrant is tertiary-based and more prominent: use it sparingly.",
          default = "standard",
        ),
        children(
          "items",
          "MenuItem",
          "MenuGroup",
          required = true,
          description = "MenuItems, or MenuGroups of MenuItems (not both).",
        ),
      ),
      overlay = true,
      stretchesChildren = true,
      m3Pages = listOf("menus"),
    ),
    ComponentDef(
      "MenuGroup",
      ComponentGroup.Overlays,
      "A group of a Menu, set apart from its neighbors by a gap. Single-select items select " +
        "within their group.",
      "DropdownMenuGroup with MenuDefaults.groupShape(index, count)",
      listOf(children("items", "MenuItem", required = true)),
      stretchesChildren = true,
      m3Pages = listOf("menus"),
    ),
    ComponentDef(
      "MenuItem",
      ComponentGroup.Overlays,
      "One entry of a Menu.",
      "DropdownMenuItem, SelectableDropdownMenuItem or CheckableDropdownMenuItem",
      listOf(
        text("label", required = true),
        icon(),
        oneOf(
          "role",
          "action",
          "single",
          "multi",
          description =
            "Action runs and closes the menu. Single is one choice of its group: the selection " +
              "moves and the menu closes. Multi toggles and the menu stays open.",
          default = "action",
        ),
        bool("selected", "Single and multi only: the initial selection."),
        action(),
      ),
      interactive = true,
      m3Pages = listOf("menus"),
    ),
    ComponentDef(
      "SideSheet",
      ComponentGroup.Overlays,
      "A modal side sheet on the trailing edge, with a header (title, close icon button) over " +
        "its content. Optional content beside the main content is a standard side sheet: a " +
        "screen with pane role supporting.",
      "Dialog (full window) holding a scrim and a Surface, composed; no side sheet composable",
      listOf(
        text("title", required = true),
        PropDef(
          "back",
          PropKind.Action,
          description =
            "Shows a back icon button at the header's leading side and runs this on a tap.",
        ),
        children("content", required = true),
      ),
      overlay = true,
      stretchesChildren = true,
      m3Pages = listOf("side-sheets"),
    ),
    ComponentDef(
      "SearchView",
      ComponentGroup.Overlays,
      "The focused state of a SearchBar or search app bar: its input field over a list of " +
        "suggestions or results, in the contained style. Full-screen fills the window; docked " +
        "opens a dropdown below the bar.",
      "ExpandedFullScreenContainedSearchBar or ExpandedDockedSearchBarWithGap, sharing the " +
        "bar's SearchBarState",
      listOf(
        oneOf("layout", "docked", "full-screen", default = "full-screen"),
        children("content"),
      ),
      overlay = true,
      stretchesChildren = true,
      m3Pages = listOf("search"),
    ),
    ComponentDef(
      "Tooltip",
      ComponentGroup.Overlays,
      "A tooltip anchored to a component. Plain describes an icon-only control; rich adds a " +
        "subhead and up to two text buttons.",
      "TooltipBox with PlainTooltip or RichTooltip",
      listOf(
        anchor("anchor", required = true),
        oneOf("variant", "plain", "rich", default = "plain"),
        text("text", required = true),
        text("title", description = "Rich only: a one-line subhead."),
        children(
          "actions",
          "Button",
          description = "Rich only: up to two text buttons, side by side.",
        ),
      ),
      overlay = true,
      m3Pages = listOf("tooltips"),
    ),
    ComponentDef(
      "Snackbar",
      ComponentGroup.Overlays,
      "A brief message at the bottom of the screen, in the Scaffold's snackbar slot above the " +
        "FAB and the navigation bar.",
      "SnackbarHost in Scaffold(snackbarHost), Snackbar via SnackbarHostState.showSnackbar",
      listOf(
        text("message", required = true),
        text("actionLabel", description = "The label of the one action a snackbar may have."),
        PropDef("action", PropKind.Action, description = "What a tap on the action label does."),
        bool("dismiss", "Shows a close icon that dismisses the snackbar."),
      ),
      overlay = true,
      m3Pages = listOf("snackbar"),
    ),
  )

// endregion
