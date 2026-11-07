package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.runtime.Composable
import bot.charlies.androiddesign.preview.feature.preview.render.wear.RenderWearComponent
import bot.charlies.androiddesign.preview.model.M3Component
import java.util.EnumMap

/**
 * How one phone component draws, and the look the layout tree records for it. Both live in one
 * entry so a component's drawing and its description cannot drift apart.
 */
internal class RenderKit(
  val render: @Composable (NodeContext) -> Unit,
  val look: NodeContext.(Ambient) -> Look,
)

/** Draws [ctx] with its catalog component's composable. */
@Composable
internal fun RenderComponent(ctx: NodeContext) {
  if (ctx.env.isWear) {
    RenderWearComponent(ctx)
    return
  }
  renderKitOf(ctx.component).render(ctx)
}

/** The kit of the phone component [component] names. */
internal fun renderKitOf(component: String): RenderKit = kits.getValue(M3Component.of(component))

private val kits: Map<M3Component, RenderKit> =
  M3Component.entries.associateWithTo(EnumMap(M3Component::class.java), ::kit)

private fun kit(component: M3Component): RenderKit =
  when (component) {
    M3Component.Column -> RenderKit({ RenderColumn(it) }) { layoutLook(it) }
    M3Component.Row -> RenderKit({ RenderRow(it) }) { layoutLook(it) }
    M3Component.Box -> RenderKit({ RenderBox(it) }) { layoutLook(it) }
    M3Component.Grid -> RenderKit({ RenderGrid(it) }) { layoutLook(it) }
    M3Component.FlexBox -> RenderKit({ RenderFlexBox(it) }) { layoutLook(it) }
    M3Component.Spacer -> RenderKit({ RenderSpacer(it) }) { layoutLook(it) }
    M3Component.Divider -> RenderKit({ RenderDivider(it) }) { layoutLook(it) }
    M3Component.Shape -> RenderKit({ RenderShape(it) }) { shapeLook(it) }
    M3Component.Text -> RenderKit({ RenderText(it) }) { textLook(it) }
    M3Component.Icon -> RenderKit({ RenderIcon(it) }) { iconLook(it) }
    M3Component.Image -> RenderKit({ RenderImage(it) }) { imageLook(it) }
    M3Component.Carousel -> RenderKit({ RenderCarousel(it) }) { carouselLook(it) }
    M3Component.Scaffold -> RenderKit({ RenderScaffold(it) }) { _ -> scaffoldLook() }
    M3Component.TopAppBar -> RenderKit({ RenderTopAppBar(it) }) { _ -> topAppBarLook() }
    M3Component.NavigationBar -> RenderKit({ RenderNavigationBar(it) }) { _ -> navigationBarLook() }
    M3Component.NavigationBarItem ->
      RenderKit({ RenderNavigationBarItem(it) }) { _ -> navigationBarItemLook() }
    M3Component.Tabs -> RenderKit({ RenderTabs(it) }) { _ -> tabsLook() }
    M3Component.Tab -> RenderKit({ RenderTab(it) }) { _ -> tabLook() }
    M3Component.NavigationRail ->
      RenderKit({ RenderNavigationRail(it) }) { _ -> navigationRailLook() }
    M3Component.NavigationRailItem ->
      RenderKit({ RenderNavigationRailItem(it) }) { _ -> navigationRailItemLook() }
    M3Component.FloatingToolbar ->
      RenderKit({ RenderFloatingToolbar(it) }) { _ -> floatingToolbarLook() }
    M3Component.DockedToolbar -> RenderKit({ RenderDockedToolbar(it) }) { _ -> dockedToolbarLook() }
    M3Component.Card -> RenderKit({ RenderCard(it) }) { _ -> cardLook() }
    M3Component.List -> RenderKit({ RenderList(it) }) { layoutLook(it) }
    M3Component.ListItem -> RenderKit({ RenderListItem(it) }) { _ -> listItemLook() }
    M3Component.Button -> RenderKit({ RenderButton(it) }) { buttonLook(it) }
    M3Component.ToggleButton -> RenderKit({ RenderToggleButton(it) }) { toggleButtonLook(it) }
    M3Component.ButtonGroup -> RenderKit({ RenderButtonGroup(it) }) { buttonGroupLook(it) }
    M3Component.IconButton -> RenderKit({ RenderIconButton(it) }) { iconButtonLook(it) }
    M3Component.SplitButton -> RenderKit({ RenderSplitButton(it) }) { splitButtonLook(it) }
    M3Component.FloatingActionButton -> RenderKit({ RenderFab(it) }) { _ -> fabLook() }
    M3Component.FabMenu -> RenderKit({ RenderFabMenu(it) }) { _ -> fabMenuLook() }
    M3Component.FabMenuItem -> RenderKit({ RenderFabMenuItem(it) }) { _ -> fabMenuItemLook() }
    M3Component.AssistChip -> RenderKit({ RenderAssistChip(it) }) { actionChipLook(it) }
    M3Component.FilterChip -> RenderKit({ RenderFilterChip(it) }) { selectableChipLook(it) }
    M3Component.InputChip -> RenderKit({ RenderInputChip(it) }) { selectableChipLook(it) }
    M3Component.SuggestionChip -> RenderKit({ RenderSuggestionChip(it) }) { actionChipLook(it) }
    M3Component.ChipGroup -> RenderKit({ RenderChipGroup(it) }) { chipGroupLook(it) }
    M3Component.Switch -> RenderKit({ RenderSwitch(it) }) { _ -> switchLook() }
    M3Component.Checkbox -> RenderKit({ RenderCheckbox(it) }) { checkboxLook(it) }
    M3Component.RadioGroup -> RenderKit({ RenderRadioGroup(it) }) { radioGroupLook(it) }
    M3Component.RadioButton -> RenderKit({ RenderRadioButton(it) }) { radioButtonLook(it) }
    M3Component.Slider -> RenderKit({ RenderSlider(it) }) { sliderLook(it) }
    M3Component.RangeSlider -> RenderKit({ RenderRangeSlider(it) }) { sliderLook(it) }
    M3Component.TextField -> RenderKit({ RenderTextField(it) }) { textFieldLook(it) }
    M3Component.SearchBar -> RenderKit({ RenderSearchBar(it) }) { _ -> searchBarLook() }
    M3Component.LoadingIndicator ->
      RenderKit({ RenderLoadingIndicator(it) }) { loadingIndicatorLook(it) }
    M3Component.ProgressIndicator ->
      RenderKit({ RenderProgressIndicator(it) }) { progressIndicatorLook(it) }
    M3Component.BottomSheet -> RenderKit({ RenderBottomSheet(it) }) { _ -> bottomSheetLook() }
    M3Component.Dialog -> RenderKit({ RenderDialog(it) }) { _ -> dialogLook() }
    M3Component.DatePicker -> RenderKit({ RenderDatePicker(it) }) { _ -> datePickerLook() }
    M3Component.TimePicker -> RenderKit({ RenderTimePicker(it) }) { _ -> timePickerLook() }
    M3Component.Menu -> RenderKit({ RenderMenu(it) }) { _ -> menuLook() }
    M3Component.MenuGroup -> RenderKit({ RenderMenuGroup(it) }) { _ -> menuGroupLook() }
    M3Component.MenuItem -> RenderKit({ RenderMenuItem(it) }) { _ -> menuItemLook() }
    M3Component.SideSheet -> RenderKit({ RenderSideSheet(it) }) { _ -> sideSheetLook() }
    M3Component.SearchView -> RenderKit({ RenderSearchView(it) }) { _ -> searchViewLook() }
    M3Component.Tooltip -> RenderKit({ RenderTooltip(it) }) { _ -> tooltipLook() }
    M3Component.Snackbar -> RenderKit({ RenderSnackbar(it) }) { _ -> snackbarLook() }
  }
