package bot.charlies.androiddesign.preview.model

enum class BoardSurface {
  Phone,
  Wear,
}

/**
 * Surface-level configuration for a target platform ([PhoneSurface] or [WearSurface]): views,
 * color/type role names, navigation chrome, and component sets read by theme resolution and
 * [FindingRules].
 */
interface SurfaceSpec {
  val key: String
  val views: List<ViewSpec>
  val colorRoles: List<String>
  val typeRoles: Map<String, TypeGroup>
  val minTouchTargetDp: Float
  val platformFontFamilies: Set<String>
  val navigationNames: List<String>
  val navigationItemNames: Set<String>
  val interactiveTypes: Set<String>
  val actionTypes: Set<String>
  val labelTypes: Set<String>
  val horizontalScrollers: Set<String>
  val overlayNames: Set<String>
  val modalNames: Set<String>
  val fabNames: Set<String>
  val roundEdgeNames: Set<String>
}

object PhoneSurface : SurfaceSpec {
  override val key = "phone"
  override val views: List<ViewSpec>
    get() = Views.all

  override val colorRoles: List<String> = ColorRole.entries.map { it.wire }
  override val typeRoles: Map<String, TypeGroup> =
    TypeRole.entries.associate { it.wire to it.group }
  override val minTouchTargetDp = 48f
  override val platformFontFamilies: Set<String> = emptySet()
  override val navigationNames: List<String> =
    listOf(
      NavigationNodes.BAR,
      NavigationNodes.RAIL,
      M3Component.FloatingToolbar.wire,
      M3Component.DockedToolbar.wire,
    )
  override val navigationItemNames: Set<String> = NavigationNodes.itemTypes
  override val actionTypes: Set<String> =
    setOf(
      M3Component.Button.wire,
      M3Component.ToggleButton.wire,
      M3Component.IconButton.wire,
      M3Component.SplitButton.wire,
      M3Component.FloatingActionButton.wire,
      M3Component.FabMenu.wire,
      M3Component.AssistChip.wire,
      M3Component.FilterChip.wire,
      M3Component.InputChip.wire,
      M3Component.SuggestionChip.wire,
    )
  override val interactiveTypes: Set<String> =
    actionTypes +
      setOf(
        M3Component.Tab.wire,
        M3Component.FabMenuItem.wire,
        M3Component.Switch.wire,
        M3Component.Checkbox.wire,
        M3Component.Slider.wire,
        M3Component.RangeSlider.wire,
        M3Component.RadioButton.wire,
        M3Component.TextField.wire,
        M3Component.SearchBar.wire,
        M3Component.MenuItem.wire,
      ) +
      NavigationNodes.itemTypes
  override val labelTypes: Set<String> =
    setOf(
      M3Component.Tab.wire,
      M3Component.Button.wire,
      M3Component.ToggleButton.wire,
      M3Component.SplitButton.wire,
      M3Component.FloatingActionButton.wire,
      M3Component.FabMenuItem.wire,
      M3Component.AssistChip.wire,
      M3Component.FilterChip.wire,
      M3Component.InputChip.wire,
      M3Component.SuggestionChip.wire,
      M3Component.MenuItem.wire,
    ) + NavigationNodes.itemTypes
  override val horizontalScrollers: Set<String> =
    setOf(M3Component.Carousel.wire, M3Component.Tabs.wire)
  override val modalNames: Set<String> =
    setOf(
      M3Component.BottomSheet.wire,
      M3Component.Dialog.wire,
      M3Component.SideSheet.wire,
      M3Component.SearchView.wire,
      M3Component.DatePicker.wire,
      M3Component.TimePicker.wire,
    )
  override val overlayNames: Set<String> =
    modalNames + setOf(M3Component.Menu.wire, M3Component.Tooltip.wire, M3Component.Snackbar.wire)
  override val fabNames: Set<String> =
    setOf(M3Component.FloatingActionButton.wire, M3Component.FabMenu.wire)
  override val roundEdgeNames: Set<String> = emptySet()
}

object WearSurface : SurfaceSpec {
  override val key = "wear"
  override val views: List<ViewSpec>
    get() = Views.wear

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
  override val platformFontFamilies: Set<String> = setOf("Roboto Flex")
  override val navigationNames: List<String> = emptyList()
  override val navigationItemNames: Set<String> = emptySet()
  override val actionTypes: Set<String> =
    setOf(
      WearComponent.Button.wire,
      WearComponent.CompactButton.wire,
      WearComponent.IconButton.wire,
      WearComponent.TextButton.wire,
      WearComponent.EdgeButton.wire,
      WearComponent.IconToggleButton.wire,
      WearComponent.TextToggleButton.wire,
      WearComponent.SwitchButton.wire,
      WearComponent.CheckboxButton.wire,
      WearComponent.RadioButton.wire,
    )
  override val interactiveTypes: Set<String> =
    actionTypes +
      setOf(
        WearComponent.Card.wire,
        WearComponent.TitleCard.wire,
        WearComponent.AppCard.wire,
        WearComponent.Slider.wire,
        WearComponent.Stepper.wire,
      )
  override val labelTypes: Set<String> =
    setOf(
      WearComponent.Button.wire,
      WearComponent.CompactButton.wire,
      WearComponent.TextButton.wire,
      WearComponent.EdgeButton.wire,
      WearComponent.TextToggleButton.wire,
      WearComponent.SwitchButton.wire,
      WearComponent.CheckboxButton.wire,
      WearComponent.RadioButton.wire,
    )
  override val horizontalScrollers: Set<String> = emptySet()
  override val modalNames: Set<String> =
    setOf(WearComponent.AlertDialog.wire, WearComponent.ConfirmationDialog.wire)
  override val overlayNames: Set<String> = modalNames
  override val fabNames: Set<String> = emptySet()
  override val roundEdgeNames: Set<String> =
    setOf(
      WearComponent.EdgeButton.wire,
      "TimeText",
      WearComponent.CircularProgressIndicator.wire,
      WearComponent.SegmentedCircularProgressIndicator.wire,
      WearComponent.AlertDialog.wire,
      WearComponent.ConfirmationDialog.wire,
    )
}

object SurfaceSpecs {
  val all: List<SurfaceSpec> = listOf(PhoneSurface, WearSurface)

  fun forDevice(device: Device): SurfaceSpec =
    if (device == Device.Watch) WearSurface else PhoneSurface

  fun forView(view: ViewSpec): SurfaceSpec = forDevice(view.device)
}
