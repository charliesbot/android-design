package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.runtime.Composable
import bot.charlies.androiddesign.preview.feature.preview.render.wear.RenderWearComponent

/** One composable per catalog component; the registry in `M3ExpressiveCatalog` names them. */
@Composable
internal fun RenderComponent(ctx: NodeContext) {
  if (ctx.env.isWear) {
    RenderWearComponent(ctx)
    return
  }
  when (ctx.component) {
    "Column" -> RenderColumn(ctx)
    "Row" -> RenderRow(ctx)
    "Box" -> RenderBox(ctx)
    "Grid" -> RenderGrid(ctx)
    "FlexBox" -> RenderFlexBox(ctx)
    "Spacer" -> RenderSpacer(ctx)
    "Divider" -> RenderDivider(ctx)
    "Shape" -> RenderShape(ctx)
    "Text" -> RenderText(ctx)
    "Icon" -> RenderIcon(ctx)
    "Image" -> RenderImage(ctx)
    "Carousel" -> RenderCarousel(ctx)
    "Scaffold" -> RenderScaffold(ctx)
    "TopAppBar" -> RenderTopAppBar(ctx)
    "NavigationBar" -> RenderNavigationBar(ctx)
    "NavigationBarItem" -> RenderNavigationBarItem(ctx)
    "Tabs" -> RenderTabs(ctx)
    "Tab" -> RenderTab(ctx)
    "NavigationRail" -> RenderNavigationRail(ctx)
    "NavigationRailItem" -> RenderNavigationRailItem(ctx)
    "FloatingToolbar" -> RenderFloatingToolbar(ctx)
    "Card" -> RenderCard(ctx)
    "List" -> RenderList(ctx)
    "ListItem" -> RenderListItem(ctx)
    "Button" -> RenderButton(ctx)
    "ToggleButton" -> RenderToggleButton(ctx)
    "ButtonGroup" -> RenderButtonGroup(ctx)
    "IconButton" -> RenderIconButton(ctx)
    "SplitButton" -> RenderSplitButton(ctx)
    "FloatingActionButton" -> RenderFab(ctx)
    "FabMenu" -> RenderFabMenu(ctx)
    "FabMenuItem" -> RenderFabMenuItem(ctx)
    "AssistChip" -> RenderAssistChip(ctx)
    "FilterChip" -> RenderFilterChip(ctx)
    "InputChip" -> RenderInputChip(ctx)
    "SuggestionChip" -> RenderSuggestionChip(ctx)
    "ChipGroup" -> RenderChipGroup(ctx)
    "Switch" -> RenderSwitch(ctx)
    "Checkbox" -> RenderCheckbox(ctx)
    "Slider" -> RenderSlider(ctx)
    "RangeSlider" -> RenderRangeSlider(ctx)
    "TextField" -> RenderTextField(ctx)
    "LoadingIndicator" -> RenderLoadingIndicator(ctx)
    "ProgressIndicator" -> RenderProgressIndicator(ctx)
    "BottomSheet" -> RenderBottomSheet(ctx)
    "Dialog" -> RenderDialog(ctx)
    "Menu" -> RenderMenu(ctx)
    "MenuItem" -> RenderMenuItem(ctx)
    "SideSheet" -> RenderSideSheet(ctx)
    "Snackbar" -> RenderSnackbar(ctx)
  }
}
