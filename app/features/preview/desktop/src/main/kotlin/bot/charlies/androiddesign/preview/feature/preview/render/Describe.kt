package bot.charlies.androiddesign.preview.feature.preview.render

import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.TypeRole

/**
 * The roles and the text a component draws with, before they become hex and font names. Each
 * component family builds its own beside its renderer, from the same role decisions.
 */
internal class Look(
  val container: ColorRole,
  val content: ColorRole,
  val type: TypeRole? = null,
  val shape: String? = null,
  val text: String? = null,
  val a11yRole: String? = null,
) {
  /** A look that takes both roles from [on]. */
  constructor(
    on: Ambient,
    type: TypeRole? = null,
    shape: String? = null,
    text: String? = null,
    a11yRole: String? = null,
  ) : this(on.container, on.content, type, shape, text, a11yRole)
}

/**
 * What the tree says about [ctx]: the Material roles its colors come from (the family's `*Look`
 * beside its renderer), its text, and how a screen reader would announce it. The measured parts
 * (bounds, text layout) are added by the recorder.
 */
internal fun describe(ctx: NodeContext, ambient: Ambient): NodeMeta {
  val env = ctx.env
  val look = look(ctx, ambient)
  val label = ctx.string("label") ?: ctx.string("contentDescription") ?: look.text
  val interactive =
    ctx.def.interactive || (ctx.component in ACTION_HOSTS && ctx.props.action("action") != null)
  return NodeMeta(
    id = ctx.id,
    type = ctx.component,
    parent = ctx.parentId,
    order = ctx.order,
    primary = ctx.primary,
    interactive = interactive,
    roles =
      NodeRoles(
        container = look.container.wire,
        content = look.content.wire,
        type = look.type?.wire,
        shape = look.shape,
      ),
    colors =
      NodeColors(
        container = env.color(look.container).toHex(),
        content = env.color(look.content).toHex(),
      ),
    text = look.text?.takeIf { it.isNotEmpty() },
    font = look.type?.takeIf { look.text != null }?.let { env.phone.fontFamilies[it] },
    a11yRole = look.a11yRole,
    a11yLabel = label,
    actions =
      buildList {
        if (interactive) add("click")
      },
  )
}

private val ACTION_HOSTS = setOf("Card", "ListItem")

/** The look of [ctx] from its family, one branch per catalog component like `RenderComponent`. */
private fun look(ctx: NodeContext, ambient: Ambient): Look =
  when (ctx.component) {
    "Column",
    "Row",
    "Box",
    "Grid",
    "FlexBox",
    "Spacer",
    "Divider" -> ctx.layoutLook(ambient)
    "Shape" -> ctx.shapeLook(ambient)
    "Text" -> ctx.textLook(ambient)
    "Icon" -> ctx.iconLook(ambient)
    "Image" -> ctx.imageLook(ambient)
    "Carousel" -> ctx.carouselLook(ambient)
    "Scaffold" -> ctx.scaffoldLook()
    "TopAppBar" -> ctx.topAppBarLook()
    "NavigationBar" -> ctx.navigationBarLook()
    "NavigationBarItem" -> ctx.navigationBarItemLook()
    "Tabs" -> ctx.tabsLook()
    "Tab" -> ctx.tabLook()
    "NavigationRail" -> ctx.navigationRailLook()
    "NavigationRailItem" -> ctx.navigationRailItemLook()
    "FloatingToolbar" -> ctx.floatingToolbarLook()
    "DockedToolbar" -> ctx.dockedToolbarLook()
    "Card" -> ctx.cardLook()
    "List" -> ctx.layoutLook(ambient)
    "ListItem" -> ctx.listItemLook()
    "Button" -> ctx.buttonLook(ambient)
    "ToggleButton" -> ctx.toggleButtonLook(ambient)
    "ButtonGroup" -> ctx.buttonGroupLook(ambient)
    "IconButton" -> ctx.iconButtonLook(ambient)
    "SplitButton" -> ctx.splitButtonLook(ambient)
    "FloatingActionButton" -> ctx.fabLook()
    "FabMenu" -> ctx.fabMenuLook()
    "FabMenuItem" -> ctx.fabMenuItemLook()
    "AssistChip",
    "SuggestionChip" -> ctx.actionChipLook(ambient)
    "FilterChip",
    "InputChip" -> ctx.selectableChipLook(ambient)
    "ChipGroup" -> ctx.chipGroupLook(ambient)
    "Switch" -> ctx.switchLook()
    "Checkbox" -> ctx.checkboxLook(ambient)
    "RadioGroup" -> ctx.radioGroupLook(ambient)
    "RadioButton" -> ctx.radioButtonLook(ambient)
    "Slider",
    "RangeSlider" -> ctx.sliderLook(ambient)
    "TextField" -> ctx.textFieldLook(ambient)
    "SearchBar" -> ctx.searchBarLook()
    "LoadingIndicator" -> ctx.loadingIndicatorLook(ambient)
    "ProgressIndicator" -> ctx.progressIndicatorLook(ambient)
    "BottomSheet" -> ctx.bottomSheetLook()
    "Dialog" -> ctx.dialogLook()
    "Menu" -> ctx.menuLook()
    "MenuGroup" -> ctx.menuGroupLook()
    "MenuItem" -> ctx.menuItemLook()
    "SideSheet" -> ctx.sideSheetLook()
    "SearchView" -> ctx.searchViewLook()
    "Tooltip" -> ctx.tooltipLook()
    "Snackbar" -> ctx.snackbarLook()
    else -> error("no look for ${ctx.component}")
  }
