package bot.charlies.androiddesign.preview.feature.preview.sandbox

import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.NavigationNodes
import bot.charlies.androiddesign.preview.model.WearComponent

/** Maps Jetpack Compose / Material 3 / Wear Compose function call names to canonical node types. */
internal object CallNameMapper {
  private val PHONE_MAP: Map<String, String> =
    mapOf(
      "Scaffold" to M3Component.Scaffold.wire,
      "Column" to M3Component.Column.wire,
      "LazyColumn" to M3Component.Column.wire,
      "Row" to M3Component.Row.wire,
      "LazyRow" to M3Component.Row.wire,
      "Box" to M3Component.Box.wire,
      "Canvas" to M3Component.Box.wire,
      "Surface" to M3Component.Box.wire,
      "Spacer" to M3Component.Spacer.wire,
      "HorizontalDivider" to M3Component.Divider.wire,
      "VerticalDivider" to M3Component.Divider.wire,
      "Divider" to M3Component.Divider.wire,
      "Text" to M3Component.Text.wire,
      "BasicText" to M3Component.Text.wire,
      "SymbolIcon" to M3Component.Icon.wire,
      "Icon" to M3Component.Icon.wire,
      "Image" to M3Component.Image.wire,
      "Badge" to M3Component.Box.wire,
      "BadgedBox" to M3Component.Box.wire,
      "HorizontalMultiBrowseCarousel" to M3Component.Carousel.wire,
      "HorizontalCenteredHeroCarousel" to M3Component.Carousel.wire,
      "HorizontalUncontainedCarousel" to M3Component.Carousel.wire,
      "TopAppBar" to M3Component.TopAppBar.wire,
      "CenterAlignedTopAppBar" to M3Component.TopAppBar.wire,
      "MediumTopAppBar" to M3Component.TopAppBar.wire,
      "LargeTopAppBar" to M3Component.TopAppBar.wire,
      "TwoRowsTopAppBar" to M3Component.TopAppBar.wire,
      "MediumFlexibleTopAppBar" to M3Component.TopAppBar.wire,
      "LargeFlexibleTopAppBar" to M3Component.TopAppBar.wire,
      "ShortNavigationBar" to NavigationNodes.BAR,
      "NavigationBar" to NavigationNodes.BAR,
      "ShortNavigationBarItem" to NavigationNodes.BAR_ITEM,
      "NavigationBarItem" to NavigationNodes.BAR_ITEM,
      "AnimatedNavigationItem" to NavigationNodes.BAR_ITEM,
      "WideNavigationRail" to NavigationNodes.RAIL,
      "ModalWideNavigationRail" to NavigationNodes.RAIL,
      "NavigationRail" to NavigationNodes.RAIL,
      "WideNavigationRailItem" to NavigationNodes.RAIL_ITEM,
      "NavigationRailItem" to NavigationNodes.RAIL_ITEM,
      "NavigationDrawerItem" to NavigationNodes.RAIL_ITEM,
      "PrimaryTabRow" to M3Component.Tabs.wire,
      "SecondaryTabRow" to M3Component.Tabs.wire,
      "PrimaryScrollableTabRow" to M3Component.Tabs.wire,
      "SecondaryScrollableTabRow" to M3Component.Tabs.wire,
      "TabRow" to M3Component.Tabs.wire,
      "ScrollableTabRow" to M3Component.Tabs.wire,
      "Tab" to M3Component.Tab.wire,
      "LeadingIconTab" to M3Component.Tab.wire,
      "HorizontalFloatingToolbar" to M3Component.FloatingToolbar.wire,
      "VerticalFloatingToolbar" to M3Component.FloatingToolbar.wire,
      "BottomAppBar" to M3Component.DockedToolbar.wire,
      "FlexibleBottomAppBar" to M3Component.DockedToolbar.wire,
      "Card" to M3Component.Card.wire,
      "ElevatedCard" to M3Component.Card.wire,
      "OutlinedCard" to M3Component.Card.wire,
      "ListItem" to M3Component.ListItem.wire,
      "SegmentedListItem" to M3Component.ListItem.wire,
      "Button" to M3Component.Button.wire,
      "ElevatedButton" to M3Component.Button.wire,
      "FilledTonalButton" to M3Component.Button.wire,
      "OutlinedButton" to M3Component.Button.wire,
      "TextButton" to M3Component.Button.wire,
      "SegmentedButton" to M3Component.Button.wire,
      "ToggleButton" to M3Component.ToggleButton.wire,
      "ElevatedToggleButton" to M3Component.ToggleButton.wire,
      "FilledTonalToggleButton" to M3Component.ToggleButton.wire,
      "OutlinedToggleButton" to M3Component.ToggleButton.wire,
      "ButtonGroup" to M3Component.ButtonGroup.wire,
      "SingleChoiceSegmentedButtonRow" to M3Component.ButtonGroup.wire,
      "MultiChoiceSegmentedButtonRow" to M3Component.ButtonGroup.wire,
      "SplitButtonLayout" to M3Component.SplitButton.wire,
      "SplitButton" to M3Component.SplitButton.wire,
      "IconButton" to M3Component.IconButton.wire,
      "FilledIconButton" to M3Component.IconButton.wire,
      "FilledTonalIconButton" to M3Component.IconButton.wire,
      "OutlinedIconButton" to M3Component.IconButton.wire,
      "IconToggleButton" to M3Component.IconButton.wire,
      "FilledIconToggleButton" to M3Component.IconButton.wire,
      "FilledTonalIconToggleButton" to M3Component.IconButton.wire,
      "OutlinedIconToggleButton" to M3Component.IconButton.wire,
      "FloatingActionButton" to M3Component.FloatingActionButton.wire,
      "SmallFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "MediumFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "LargeFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "ExtendedFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "SmallExtendedFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "MediumExtendedFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "LargeExtendedFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "ToggleFloatingActionButton" to M3Component.FloatingActionButton.wire,
      "FloatingActionButtonMenu" to M3Component.FabMenu.wire,
      "FloatingActionButtonMenuItem" to M3Component.FabMenuItem.wire,
      "AssistChip" to M3Component.AssistChip.wire,
      "ElevatedAssistChip" to M3Component.AssistChip.wire,
      "FilterChip" to M3Component.FilterChip.wire,
      "ElevatedFilterChip" to M3Component.FilterChip.wire,
      "InputChip" to M3Component.InputChip.wire,
      "SuggestionChip" to M3Component.SuggestionChip.wire,
      "ElevatedSuggestionChip" to M3Component.SuggestionChip.wire,
      "Switch" to M3Component.Switch.wire,
      "Checkbox" to M3Component.Checkbox.wire,
      "TriStateCheckbox" to M3Component.Checkbox.wire,
      "Slider" to M3Component.Slider.wire,
      "VerticalSlider" to M3Component.Slider.wire,
      "RangeSlider" to M3Component.RangeSlider.wire,
      "RadioButton" to M3Component.RadioButton.wire,
      "TextField" to M3Component.TextField.wire,
      "OutlinedTextField" to M3Component.TextField.wire,
      "SearchBar" to M3Component.SearchBar.wire,
      "AppBarWithSearch" to M3Component.SearchBar.wire,
      "TopSearchBar" to M3Component.SearchBar.wire,
      "LoadingIndicator" to M3Component.LoadingIndicator.wire,
      "ContainedLoadingIndicator" to M3Component.LoadingIndicator.wire,
      "LinearProgressIndicator" to M3Component.ProgressIndicator.wire,
      "LinearWavyProgressIndicator" to M3Component.ProgressIndicator.wire,
      "CircularProgressIndicator" to M3Component.ProgressIndicator.wire,
      "CircularWavyProgressIndicator" to M3Component.ProgressIndicator.wire,
      "ModalBottomSheet" to M3Component.BottomSheet.wire,
      "BottomSheetScaffold" to M3Component.Scaffold.wire,
      "Dialog" to M3Component.Dialog.wire,
      "AlertDialog" to M3Component.Dialog.wire,
      "BasicAlertDialog" to M3Component.Dialog.wire,
      "DatePicker" to M3Component.DatePicker.wire,
      "DatePickerDialog" to M3Component.DatePicker.wire,
      "DateRangePicker" to M3Component.DatePicker.wire,
      "TimePicker" to M3Component.Column.wire,
      "TimePickerDialog" to M3Component.TimePicker.wire,
      "VibrantTimePickerDialog" to M3Component.TimePicker.wire,
      "HorizontalTimePicker" to M3Component.Row.wire,
      "VerticalTimePicker" to M3Component.Column.wire,
      "TimeInput" to M3Component.Row.wire,
      "DropdownMenu" to M3Component.Menu.wire,
      "DropdownMenuPopup" to M3Component.Menu.wire,
      "ExposedDropdownMenu" to M3Component.Menu.wire,
      "DropdownMenuGroup" to M3Component.MenuGroup.wire,
      "DropdownMenuItem" to M3Component.MenuItem.wire,
      "SelectableDropdownMenuItem" to M3Component.MenuItem.wire,
      "CheckableDropdownMenuItem" to M3Component.MenuItem.wire,
      "ModalDrawerSheet" to M3Component.Column.wire,
      "DismissibleDrawerSheet" to M3Component.Column.wire,
      "PermanentDrawerSheet" to M3Component.Column.wire,
      "ModalNavigationDrawer" to M3Component.Box.wire,
      "DockedSearchBar" to M3Component.SearchBar.wire,
      "ExpandedFullScreenSearchBar" to M3Component.SearchView.wire,
      "ExpandedFullScreenContainedSearchBar" to M3Component.SearchView.wire,
      "ExpandedDockedSearchBar" to M3Component.SearchView.wire,
      "ExpandedDockedSearchBarWithGap" to M3Component.SearchView.wire,
      "TooltipBox" to M3Component.Box.wire,
      "PlainTooltip" to M3Component.Tooltip.wire,
      "RichTooltip" to M3Component.Tooltip.wire,
      "Snackbar" to M3Component.Snackbar.wire,
      "SnackbarHost" to M3Component.Snackbar.wire,
    )

  private val WEAR_MAP: Map<String, String> =
    mapOf(
      "ScreenScaffold" to WearComponent.Screen.wire,
      "AppScaffold" to WearComponent.Screen.wire,
      "TransformingLazyColumn" to WearComponent.Column.wire,
      "ScalingLazyColumn" to WearComponent.Column.wire,
      "Column" to WearComponent.Column.wire,
      "Row" to WearComponent.Row.wire,
      "Box" to WearComponent.Box.wire,
      "Canvas" to WearComponent.Box.wire,
      "Spacer" to WearComponent.Spacer.wire,
      "ListHeader" to WearComponent.ListHeader.wire,
      "ListSubHeader" to WearComponent.ListSubHeader.wire,
      "Button" to WearComponent.Button.wire,
      "FilledTonalButton" to WearComponent.Button.wire,
      "OutlinedButton" to WearComponent.Button.wire,
      "ChildButton" to WearComponent.Button.wire,
      "CompactButton" to WearComponent.CompactButton.wire,
      "IconButton" to WearComponent.IconButton.wire,
      "FilledIconButton" to WearComponent.IconButton.wire,
      "FilledTonalIconButton" to WearComponent.IconButton.wire,
      "OutlinedIconButton" to WearComponent.IconButton.wire,
      "TextButton" to WearComponent.TextButton.wire,
      "FilledTextButton" to WearComponent.TextButton.wire,
      "FilledTonalTextButton" to WearComponent.TextButton.wire,
      "OutlinedTextButton" to WearComponent.TextButton.wire,
      "EdgeButton" to WearComponent.EdgeButton.wire,
      "ButtonGroup" to WearComponent.ButtonGroup.wire,
      "IconToggleButton" to WearComponent.IconToggleButton.wire,
      "TextToggleButton" to WearComponent.TextToggleButton.wire,
      "SwitchButton" to WearComponent.SwitchButton.wire,
      "SplitSwitchButton" to WearComponent.SwitchButton.wire,
      "CheckboxButton" to WearComponent.CheckboxButton.wire,
      "SplitCheckboxButton" to WearComponent.CheckboxButton.wire,
      "RadioButton" to WearComponent.RadioButton.wire,
      "SplitRadioButton" to WearComponent.RadioButton.wire,
      "Text" to WearComponent.Text.wire,
      "SymbolIcon" to WearComponent.Icon.wire,
      "Icon" to WearComponent.Icon.wire,
      "Image" to WearComponent.Image.wire,
      "Card" to WearComponent.Card.wire,
      "OutlinedCard" to WearComponent.Card.wire,
      "TitleCard" to WearComponent.TitleCard.wire,
      "AppCard" to WearComponent.AppCard.wire,
      "Slider" to WearComponent.Slider.wire,
      "InlineSlider" to WearComponent.Slider.wire,
      "Stepper" to WearComponent.Stepper.wire,
      "CircularProgressIndicator" to WearComponent.CircularProgressIndicator.wire,
      "LinearProgressIndicator" to WearComponent.LinearProgressIndicator.wire,
      "SegmentedCircularProgressIndicator" to WearComponent.SegmentedCircularProgressIndicator.wire,
      "Dialog" to WearComponent.AlertDialog.wire,
      "AlertDialog" to WearComponent.AlertDialog.wire,
      "AlertDialogContent" to WearComponent.AlertDialog.wire,
      "ConfirmationDialog" to WearComponent.ConfirmationDialog.wire,
      "ConfirmationDialogContent" to WearComponent.ConfirmationDialog.wire,
      "TimeText" to "TimeText",
    )

  fun map(callName: String?, surface: BoardSurface): String {
    if (callName.isNullOrBlank()) return "Component"
    val table = if (surface == BoardSurface.Wear) WEAR_MAP else PHONE_MAP
    return table[callName] ?: callName
  }

  fun isKnown(callName: String?, surface: BoardSurface): Boolean {
    if (callName.isNullOrBlank()) return false
    val table = if (surface == BoardSurface.Wear) WEAR_MAP else PHONE_MAP
    return callName in table
  }

  /** Extracts the identifier ending at or starting at [offset] in [sourceText]. */
  fun identifierAt(sourceText: String, offset: Int): String? {
    if (offset !in sourceText.indices) return null
    var start = offset
    while (start < sourceText.length && !isIdentStart(sourceText[start])) {
      start++
    }
    if (start >= sourceText.length) return null
    var end = start
    while (end < sourceText.length && isIdentPart(sourceText[end])) {
      end++
    }
    if (end <= start) return null
    val token = sourceText.substring(start, end)
    // Skip qualifier prefixes like `MaterialTheme.` if offset landed before a member call.
    if (end < sourceText.length && sourceText[end] == '.') {
      val next = identifierAt(sourceText, end + 1)
      if (next != null) return next
    }
    return token
  }

  private fun isIdentStart(c: Char): Boolean = c.isLetter() || c == '_'

  private fun isIdentPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_'
}
