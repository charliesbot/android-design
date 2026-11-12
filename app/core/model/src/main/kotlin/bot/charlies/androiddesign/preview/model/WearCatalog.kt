package bot.charlies.androiddesign.preview.model

/**
 * The Wear OS catalog: components that map one to one onto Wear Compose Material 3
 * (`androidx.wear.compose.material3`). A Wear proposal is dark, round and has no panes, and its
 * `sizeClass` override has one key, `large`, from 225 dp.
 */
object WearCatalog : Catalog {
  const val ID = "https://github.com/charliesbot/android-design/catalogs/wear-m3-expressive/v1"
  const val TITLE = "Material 3 Expressive for Wear OS"
  const val KEY = "wear"

  /** The width from which a Wear display is a large one (Wear's `ScreenSize.LARGE` breakpoint). */
  const val LARGE_MIN_DP = 225

  /** The `sizeClass` override key of displays at least [LARGE_MIN_DP] wide. */
  const val LARGE = "large"

  override val id = ID
  override val key = KEY
  override val title = TITLE
  override val description =
    "Material 3 Expressive for Wear OS components that map one to one onto Wear Compose " +
      "material3."
  override val commonProps: List<PropDef> = COMMON_PROPS

  override val components: List<ComponentDef> by lazy { buildWearComponents() }

  private val byName: Map<String, ComponentDef> by lazy { components.associateBy { it.name } }

  override fun find(name: String): ComponentDef? = byName[name]

  override val views: List<ViewSpec>
    get() = Views.wear

  override val sizeBreakpoints = listOf(SizeBreakpoint(LARGE, LARGE_MIN_DP))

  override val showInSizes: List<SizeClass> = emptyList()

  /** The roles of `androidx.wear.compose.material3.ColorScheme`. */
  override val colorRoles: List<String> =
    listOf(
      "primary",
      "primaryDim",
      "primaryContainer",
      "onPrimary",
      "onPrimaryContainer",
      "secondary",
      "secondaryDim",
      "secondaryContainer",
      "onSecondary",
      "onSecondaryContainer",
      "tertiary",
      "tertiaryDim",
      "tertiaryContainer",
      "onTertiary",
      "onTertiaryContainer",
      "surfaceContainerLow",
      "surfaceContainer",
      "surfaceContainerHigh",
      "onSurface",
      "onSurfaceVariant",
      "outline",
      "outlineVariant",
      "background",
      "onBackground",
      "error",
      "errorDim",
      "errorContainer",
      "onError",
      "onErrorContainer",
    )

  /**
   * The roles of `androidx.wear.compose.material3.Typography` that set a run of text (the `arc*`
   * roles style curved text, which the catalog does not have). Numerals count as display text.
   */
  override val typeRoles: Map<String, TypeGroup> =
    linkedMapOf<String, TypeGroup>().apply {
      for (size in listOf("Large", "Medium", "Small")) put("display$size", TypeGroup.Display)
      for (size in listOf("Large", "Medium", "Small")) put("title$size", TypeGroup.Title)
      for (size in listOf("Large", "Medium", "Small")) put("label$size", TypeGroup.Label)
      for (size in listOf("Large", "Medium", "Small", "ExtraSmall")) {
        put("body$size", TypeGroup.Body)
      }
      for (size in listOf("ExtraLarge", "Large", "Medium", "Small", "ExtraSmall")) {
        put("numeral$size", TypeGroup.Display)
      }
    }

  override val minTouchTargetDp = 48f

  override val supportsPanes = false

  override val overlayNames: Set<String> by lazy {
    components.filter { it.overlay }.map { it.name }.toSet()
  }

  override val modalNames: Set<String> by lazy {
    components.filter { it.modal }.map { it.name }.toSet()
  }

  override val fabNames: Set<String> by lazy {
    components.filter { it.fab }.map { it.name }.toSet()
  }

  override val navigationNames: List<String> = emptyList()

  /**
   * Components designed to meet the round edge: `TimeText` is the screen's clock (a `Screen`
   * option, not a component), the ring hugs the display, the edge button follows its curve, and a
   * dialog fills the display (upstream pads its title and text; the buttons it holds are checked).
   */
  override val roundEdgeNames: Set<String> =
    setOf(
      WearComponent.EdgeButton.wire,
      "TimeText",
      WearComponent.CircularProgressIndicator.wire,
      WearComponent.SegmentedCircularProgressIndicator.wire,
      WearComponent.AlertDialog.wire,
      WearComponent.ConfirmationDialog.wire,
    )

  /** Wear Compose sets its type scale in Roboto Flex. */
  override val platformFontFamilies: Set<String> = setOf("Roboto Flex")
}

// region registry definitions

/** What an interactive Wear component also takes. */
private val wearInteractiveProps =
  listOf(
    text("contentDescription", description = "Accessibility label when it differs from the label."),
    bool("enabled", default = true),
  )

/** What a Wear button also takes: the marker of the screen's primary action. */
private val wearButtonProps =
  listOf(
    bool("primary", "Marks the screen's primary action. Exactly one is expected per screen."),
  ) + wearInteractiveProps

private val buttonVariants = arrayOf("filled", "filledTonal", "filledVariant", "outlined", "child")
private val iconButtonVariants = arrayOf("standard", "filled", "filledTonal", "outlined")
private val iconButtonSizes = arrayOf("extra-small", "small", "default", "large")
private val edgeButtonSizes = arrayOf("extra-small", "small", "medium", "large")

private fun buildWearComponents(): List<ComponentDef> =
  wearLayout() +
    wearStructure() +
    wearActions() +
    wearContent() +
    wearInputs() +
    wearFeedback() +
    wearOverlays()

/** Column, Row, Box and Spacer are Compose foundation layouts, the same on every surface. */
private fun wearLayout(): List<ComponentDef> =
  layout().filter { it.name in setOf("Column", "Row", "Box", "Spacer") }

private fun wearStructure(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Screen",
      ComponentGroup.Structure,
      "The screen frame: an optional time text and edge button around a scrolling list or a " +
        "fixed body.",
      "AppScaffold and ScreenScaffold, with TransformingLazyColumn for the list",
      listOf(
        bool(
          "scroll",
          "True lays children out as the rows of a scrolling TransformingLazyColumn, whose rows " +
            "scale and fade at the round display's edge. False stacks them in a fixed Box.",
          default = true,
        ),
        bool("timeText", "Shows the clock at the top of the display.", default = true),
        child("edgeButton", "EdgeButton"),
        children("children", required = true),
      ),
      stretchesChildren = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "ListHeader",
      ComponentGroup.Structure,
      "The heading of a section of a list.",
      "ListHeader",
      listOf(text("text", required = true)),
      width = WidthFill.Always,
    ),
    ComponentDef(
      "ListSubHeader",
      ComponentGroup.Structure,
      "A smaller heading inside a section, with an optional icon.",
      "ListSubHeader",
      listOf(text("text", required = true), icon()),
      width = WidthFill.Always,
    ),
  )

private fun wearActions(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Button",
      ComponentGroup.Actions,
      "A full-width button with a label, an optional secondary label and an icon.",
      "Button, FilledTonalButton, OutlinedButton, ChildButton",
      listOf(
        oneOf(
          "variant",
          *buttonVariants,
          default = "filled",
          description = "filledVariant is Button with ButtonDefaults.filledVariantButtonColors.",
        ),
        text("label", required = true),
        text("secondaryLabel"),
        icon(),
        action(),
      ) + wearButtonProps,
      interactive = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "CompactButton",
      ComponentGroup.Actions,
      "A short button for a tight row; give it a label, an icon or both.",
      "CompactButton",
      listOf(
        oneOf("variant", *buttonVariants, default = "filled"),
        text("label"),
        icon(),
        action(),
      ) + wearButtonProps,
      interactive = true,
    ),
    ComponentDef(
      "IconButton",
      ComponentGroup.Actions,
      "A round button that shows only an icon.",
      "IconButton, FilledIconButton, FilledTonalIconButton, OutlinedIconButton",
      listOf(
        oneOf("variant", *iconButtonVariants, default = "standard"),
        oneOf("size", *iconButtonSizes, default = "default"),
        icon(required = true),
        action(),
      ) + wearButtonProps,
      interactive = true,
    ),
    ComponentDef(
      "TextButton",
      ComponentGroup.Actions,
      "A round button that shows a short text, such as a number or two letters.",
      "TextButton",
      listOf(
        oneOf("variant", *iconButtonVariants, default = "standard"),
        oneOf("size", "small", "default", "large", default = "default"),
        text("label", required = true),
        action(),
      ) + wearButtonProps,
      interactive = true,
    ),
    ComponentDef(
      "EdgeButton",
      ComponentGroup.Actions,
      "A button that hugs the bottom edge of the round display, the screen's main action.",
      "EdgeButton",
      listOf(
        oneOf("size", *edgeButtonSizes, default = "small"),
        text("label", description = "Short text. Set exactly one of label or icon."),
        icon(
          description =
            "Material Symbols name, shown instead of a label. Set exactly one of label or icon.",
        ),
        action(),
      ) + wearButtonProps,
      interactive = true,
    ),
    ComponentDef(
      "ButtonGroup",
      ComponentGroup.Actions,
      "Buttons side by side that share the width.",
      "ButtonGroup",
      listOf(
        children(
          "children",
          "Button",
          "CompactButton",
          "IconButton",
          "TextButton",
          "IconToggleButton",
          "TextToggleButton",
          required = true,
        ),
      ),
      width = WidthFill.Always,
    ),
    ComponentDef(
      "IconToggleButton",
      ComponentGroup.Actions,
      "A round icon button with an on and off state.",
      "IconToggleButton",
      listOf(
        oneOf("size", *iconButtonSizes, default = "default"),
        icon(required = true),
        bool("checked"),
        action(),
      ) + wearInteractiveProps,
      interactive = true,
    ),
    ComponentDef(
      "TextToggleButton",
      ComponentGroup.Actions,
      "A round text button with an on and off state.",
      "TextToggleButton",
      listOf(
        oneOf("size", "small", "default", "large", default = "default"),
        text("label", required = true),
        bool("checked"),
        action(),
      ) + wearInteractiveProps,
      interactive = true,
    ),
    ComponentDef(
      "SwitchButton",
      ComponentGroup.Actions,
      "A full-width row with a label and a switch.",
      "SwitchButton",
      toggleRowProps(selected = false),
      interactive = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "CheckboxButton",
      ComponentGroup.Actions,
      "A full-width row with a label and a checkbox.",
      "CheckboxButton",
      toggleRowProps(selected = false),
      interactive = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "RadioButton",
      ComponentGroup.Actions,
      "A full-width row with a label and a radio control, one of a group.",
      "RadioButton",
      toggleRowProps(selected = true),
      interactive = true,
      width = WidthFill.Always,
    ),
  )

private fun toggleRowProps(selected: Boolean): List<PropDef> =
  listOf(
    text("label", required = true),
    text("secondaryLabel"),
    icon(),
    bool(if (selected) "selected" else "checked"),
    action(),
  ) + wearInteractiveProps

private fun wearContent(): List<ComponentDef> =
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
        num("aspectRatio", "Width divided by height.", NumberBounds(0.1, 10.0)),
        text("contentDescription"),
      ),
      width = WidthFill.WhenSet("aspectRatio"),
    ),
    ComponentDef(
      "Card",
      ComponentGroup.Content,
      "A card around any content, tappable when it has an action.",
      "Card",
      listOf(
        children("children", required = true, scope = ChildScope.Column),
        action(),
      ) + wearInteractiveProps,
      interactive = true,
      stretchesChildren = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "TitleCard",
      ComponentGroup.Content,
      "A card with a title, an optional time and subtitle, and a body.",
      "TitleCard",
      listOf(
        text("title", required = true),
        text("time"),
        text("subtitle"),
        text("body"),
        action(),
      ) + wearInteractiveProps,
      interactive = true,
      width = WidthFill.Always,
    ),
    ComponentDef(
      "AppCard",
      ComponentGroup.Content,
      "A card from an app: the app's name and icon, a title, an optional time and a body.",
      "AppCard",
      listOf(
        text("appName", required = true),
        icon("appIcon"),
        text("title", required = true),
        text("time"),
        text("body"),
        action(),
      ) + wearInteractiveProps,
      interactive = true,
      width = WidthFill.Always,
    ),
  )

private fun wearInputs(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "Slider",
      ComponentGroup.Inputs,
      "Picks one value from a range with decrease and increase buttons.",
      "Slider",
      listOf(
        num("value", "Current value."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true), default = 0),
      ) + wearInteractiveProps,
      interactive = true,
      width = WidthFill.Always,
      rangeValued = true,
    ),
    ComponentDef(
      "Stepper",
      ComponentGroup.Inputs,
      "Picks one value from a range with decrease and increase buttons around a value shown " +
        "in its content.",
      "Stepper",
      listOf(
        num("value", "Current value."),
        range("range", "[min, max]; [0, 1] by default."),
        num("steps", bounds = NumberBounds(0.0, 100.0, whole = true), default = 0),
        child("content", description = "What sits between the buttons, such as the value's Text."),
      ) + wearInteractiveProps,
      interactive = true,
      width = WidthFill.Always,
      rangeValued = true,
    ),
  )

private fun wearFeedback(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "CircularProgressIndicator",
      ComponentGroup.Feedback,
      "A ring around the display's edge; indeterminate when progress is absent.",
      "CircularProgressIndicator",
      listOf(num("progress", "Completed fraction.", NumberBounds(0.0, 1.0))),
    ),
    ComponentDef(
      "LinearProgressIndicator",
      ComponentGroup.Feedback,
      "A bar showing a completed fraction.",
      "LinearProgressIndicator",
      listOf(num("progress", "Completed fraction.", NumberBounds(0.0, 1.0)).copy(required = true)),
      width = WidthFill.Always,
    ),
    ComponentDef(
      "SegmentedCircularProgressIndicator",
      ComponentGroup.Feedback,
      "A ring of separate segments filled in order.",
      "SegmentedCircularProgressIndicator",
      listOf(
        num("segmentCount", bounds = NumberBounds(1.0, 24.0, whole = true)).copy(required = true),
        num("progress", "Completed fraction.", NumberBounds(0.0, 1.0)).copy(required = true),
      ),
    ),
  )

private fun wearOverlays(): List<ComponentDef> =
  listOf(
    ComponentDef(
      "AlertDialog",
      ComponentGroup.Overlays,
      "A full-screen dialog with an icon, a title, a message and two buttons.",
      "AlertDialog",
      listOf(
        icon(),
        text("title", required = true),
        text("text"),
        child("confirm", "Button", "IconButton", scope = ChildScope.Row),
        child("dismiss", "Button", "IconButton", scope = ChildScope.Row),
      ),
      overlay = true,
      modal = true,
    ),
    ComponentDef(
      "ConfirmationDialog",
      ComponentGroup.Overlays,
      "A brief full-screen confirmation with an icon and curved text.",
      "ConfirmationDialog",
      listOf(icon(required = true), text("text")),
      overlay = true,
      modal = true,
    ),
  )

// endregion
