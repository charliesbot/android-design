package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import bot.charlies.androiddesign.preview.domain.SideSheetSpec
import bot.charlies.androiddesign.preview.domain.TooltipSpec
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.TypeRole

/**
 * The overlay open on this screen, if it is one the screen draws itself: menus that hang off a
 * component or a split button draw where they are anchored instead.
 */
@Composable
internal fun RenderOpenOverlay(env: RenderEnv) {
  val open = env.openOverlay ?: return
  if (env.placement.screenLevel.none { it.id == open }) return
  RenderNode(
    env = env,
    ref = ChildRef(open, DataScope.root(env.screen.dataModel)),
    parentId = env.idPrefix + "root",
    order = listOf(9, 0),
    host = ChildHost.Flow,
  )
}

/**
 * The snackbar open on this screen, if any: the Scaffold's snackbar slot calls this, so the
 * snackbar sits above the FAB and the navigation bar.
 */
@Composable
internal fun SnackbarSlot(env: RenderEnv) {
  val open = env.openOverlay ?: return
  if (env.placement.snackbars.none { it.id == open }) return
  RenderNode(
    env = env,
    ref = ChildRef(open, DataScope.root(env.screen.dataModel)),
    parentId = env.idPrefix + "root",
    order = listOf(9, 0),
    host = ChildHost.Wrap,
  )
}

/** Draws the menu [menuId] where its anchor is, when it is the open overlay. */
@Composable
internal fun MenuOverlay(
  env: RenderEnv,
  menuId: String,
  scope: DataScope,
  parentId: String,
  order: List<Int>,
) {
  if (env.openOverlay != menuId) return
  RenderNode(env, ChildRef(menuId, scope), parentId, order + listOf(99, 0), ChildHost.Wrap)
}

private val BottomSheetAmbient = Ambient(ColorRole.SurfaceContainerLow, ColorRole.OnSurface)
private val DialogAmbient = Ambient(ColorRole.SurfaceContainerHigh, ColorRole.OnSurface)
private val MenuAmbient = Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurface)

internal fun NodeContext.bottomSheetLook() = Look(BottomSheetAmbient, shape = "extraLarge")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderBottomSheet(ctx: NodeContext) {
  ModalBottomSheet(
    onDismissRequest = { ctx.env.act(ActionCall.Dismiss) },
    modifier = ctx.modifier(),
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle = { BottomSheetDefaults.DragHandle() },
  ) {
    CompositionLocalProvider(LocalAmbient provides BottomSheetAmbient) {
      ctx.Children("content", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, column = this))
      Spacer(Modifier.height(8.dp))
    }
  }
}

internal fun NodeContext.dialogLook() =
  Look(
    DialogAmbient,
    TypeRole.HeadlineSmall,
    "extraLarge",
    string("title"),
  )

@Composable
internal fun RenderDialog(ctx: NodeContext) {
  val text = ctx.string("text")
  CompositionLocalProvider(LocalAmbient provides DialogAmbient) {
    AlertDialog(
      onDismissRequest = { ctx.env.act(ActionCall.Dismiss) },
      confirmButton = { ctx.Child("confirm", 1) },
      modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 560.dp).then(ctx.modifier()),
      dismissButton = ctx.props.childId("dismiss")?.let { id -> { ctx.ChildNode(id, 2) } },
      title = { ctx.PartText(ctx.string("title").orEmpty(), "title") },
      text = text?.let { value -> { ctx.PartText(value, "text") } },
      properties = DialogProperties(usePlatformDefaultWidth = false),
    )
  }
}

internal fun NodeContext.menuLook() = Look(MenuAmbient, shape = "extraLarge")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderMenu(ctx: NodeContext) {
  CompositionLocalProvider(LocalAmbient provides MenuAmbient) {
    DropdownMenuPopup(expanded = true, onDismissRequest = { ctx.env.act(ActionCall.Dismiss) }) {
      DropdownMenuGroup(
        shapes = MenuDefaults.groupShape(0, 1),
        modifier = ctx.modifier(),
        containerColor = ctx.env.color(MenuAmbient.container),
      ) {
        ctx.Children("items", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, column = this))
      }
    }
  }
}

internal fun NodeContext.menuItemLook() =
  Look(
    MenuAmbient,
    TypeRole.LabelLarge,
    text = string("label"),
    a11yRole = "Button",
  )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderMenuItem(ctx: NodeContext) {
  val icon = ctx.string("icon")
  DropdownMenuItem(
    onClick = {
      ctx.env.act(ctx.props.action("action"))
      // Choosing an item closes its menu unless the action already moved on.
      if (ctx.props.action("action") !is ActionCall.Open) ctx.env.act(ActionCall.Dismiss)
    },
    text = { ctx.PartText(ctx.string("label").orEmpty()) },
    shape = MenuDefaults.itemShape(ctx.host.groupIndex, ctx.host.groupCount).shape,
    modifier = ctx.modifier(),
    leadingIcon = icon?.let { name -> { SymbolIcon(name) } },
  )
}

private val SideSheetAmbient = Ambient(ColorRole.SurfaceContainerLow, ColorRole.OnSurface)

/** The container is the sheet's; the content is the title's, set in on surface variant. */
internal fun NodeContext.sideSheetLook() =
  Look(
    SideSheetAmbient.container,
    ColorRole.OnSurfaceVariant,
    TypeRole.TitleLarge,
    text = string("title"),
  )

/**
 * Compose has no side sheet, so it is composed: a full-window `Dialog` (its own scrim dims the
 * screen) holding a click-away area and a `Surface` on the trailing edge. The corners round on the
 * edge that faces the content only, and Compose mirrors them for right-to-left. The exporter writes
 * the same composition, with the same [SideSheetSpec] numbers.
 */
@Composable
internal fun RenderSideSheet(ctx: NodeContext) {
  val env = ctx.env
  val back = ctx.props.action("back")
  val dismiss = { env.act(ActionCall.Dismiss) }
  val onSurfaceVariant = env.color(ColorRole.OnSurfaceVariant)
  val iconColors = IconButtonDefaults.iconButtonColors(contentColor = onSurfaceVariant)
  val padding = SideSheetSpec.PADDING_DP.dp
  Dialog(
    onDismissRequest = dismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    CompositionLocalProvider(LocalAmbient provides SideSheetAmbient) {
      Box(Modifier.fillMaxSize()) {
        // The click-away sits behind the sheet, so a tap on the sheet never reaches it.
        Box(
          Modifier.matchParentSize()
            .clickable(interactionSource = null, indication = null, onClick = dismiss),
        )
        Surface(
          modifier =
            Modifier.align(Alignment.CenterEnd)
              .widthIn(max = SideSheetSpec.MAX_WIDTH_DP.dp)
              .fillMaxWidth()
              .fillMaxHeight()
              .then(ctx.modifier()),
          shape =
            RoundedCornerShape(
              topStart = SideSheetSpec.CORNER_DP.dp,
              topEnd = 0.dp,
              bottomEnd = 0.dp,
              bottomStart = SideSheetSpec.CORNER_DP.dp,
            ),
          color = env.color(SideSheetAmbient.container),
          contentColor = env.color(SideSheetAmbient.content),
        ) {
          Column(Modifier.fillMaxSize().padding(vertical = padding)) {
            Row(
              Modifier.fillMaxWidth()
                .padding(
                  start =
                    if (back != null) SideSheetSpec.START_PADDING_WITH_ICON_DP.dp else padding,
                  end = padding,
                ),
              horizontalArrangement = Arrangement.spacedBy(SideSheetSpec.HEADER_GAP_DP.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (back != null) {
                IconButton(onClick = { env.act(back) }, colors = iconColors) {
                  SymbolIcon("arrow_back", contentDescription = "Back")
                }
              }
              ctx.PartText(
                text = ctx.string("title").orEmpty(),
                part = "title",
                style = env.typeStyle(TypeRole.TitleLarge),
                color = onSurfaceVariant,
                modifier = Modifier.weight(1f),
              )
              IconButton(onClick = dismiss, colors = iconColors) {
                SymbolIcon("close", contentDescription = "Close")
              }
            }
            Spacer(Modifier.height(SideSheetSpec.HEADER_GAP_DP.dp))
            Column(
              Modifier.weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = padding),
            ) {
              ctx.Children(
                "content",
                1,
                ChildHost(fillWidth = ctx.def.stretchesChildren, column = this),
              )
            }
          }
        }
      }
    }
  }
}

private val SnackbarAmbient = Ambient(ColorRole.InverseSurface, ColorRole.InverseOnSurface)

internal fun NodeContext.snackbarLook() =
  Look(SnackbarAmbient, TypeRole.BodyMedium, text = string("message"))

/**
 * The real `Snackbar`, drawn from a stand-in for the `SnackbarData` a `SnackbarHost` would hand it,
 * so it looks exactly like the one `showSnackbar` shows. A review render has no clock, so it stays
 * open until a tap on the action or the close icon.
 */
@Composable
internal fun RenderSnackbar(ctx: NodeContext) {
  val env = ctx.env
  val action = ctx.props.action("action")
  val message = ctx.string("message").orEmpty()
  val actionLabel = ctx.string("actionLabel")
  val withDismissAction = ctx.bool("dismiss")
  val data =
    remember(message, actionLabel, withDismissAction, action) {
      object : SnackbarData {
        override val visuals =
          object : SnackbarVisuals {
            override val message = message
            override val actionLabel = actionLabel
            override val withDismissAction = withDismissAction
            override val duration = SnackbarDuration.Indefinite
          }

        override fun performAction() {
          env.act(action)
          // Choosing the action closes the snackbar unless the action already moved on.
          if (action !is ActionCall.Open) env.act(ActionCall.Dismiss)
        }

        override fun dismiss() = env.act(ActionCall.Dismiss)
      }
    }
  CompositionLocalProvider(LocalAmbient provides SnackbarAmbient) {
    Snackbar(data, ctx.modifier())
  }
}

/** The scope of the tooltip popup being drawn: `PlainTooltip` and `RichTooltip` extend it. */
@OptIn(ExperimentalMaterial3Api::class)
private val LocalTooltipScope = compositionLocalOf<TooltipScope?> { null }

private val PlainTooltipAmbient = Ambient(ColorRole.InverseSurface, ColorRole.InverseOnSurface)
private val RichTooltipAmbient = Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurfaceVariant)

internal fun NodeContext.tooltipLook(): Look =
  if (enum("variant") == "rich") {
    Look(RichTooltipAmbient, TypeRole.BodyMedium, "medium", string("text"))
  } else {
    Look(PlainTooltipAmbient, TypeRole.BodySmall, "extraSmall", string("text"))
  }

/**
 * Draws [anchor] inside the `TooltipBox` of the tooltip [tooltipId], which hangs off it. The box is
 * there whether or not the tooltip is open, as in the exported code, and a tooltip the viewer
 * hovers or long presses opens the way it does on a device. The overlay state opens it for a
 * review: the box then starts visible, and an `open` or `dismiss` action shows or hides it.
 * Tooltips are persistent here, so a static render does not time out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TooltipAnchor(
  env: RenderEnv,
  tooltipId: String,
  scope: DataScope,
  parentId: String,
  order: List<Int>,
  anchor: @Composable () -> Unit,
) {
  val node = env.nodes[tooltipId]
  val open = env.openOverlay == tooltipId
  val state = rememberTooltipState(initialIsVisible = open, isPersistent = true)
  LaunchedEffect(open) { if (open) state.show() else state.dismiss() }
  // A tooltip the viewer closes (a tap outside it) closes the overlay too.
  var shown by remember { mutableStateOf(false) }
  LaunchedEffect(state.isVisible) {
    if (state.isVisible) {
      shown = true
    } else if (shown) {
      shown = false
      if (env.openOverlay == tooltipId) env.dismiss()
    }
  }
  // The variant at this width: the overrides over the prop, else the catalog default.
  val variant = node?.let {
    Props(effectiveProps(it, env), scope).string("variant")
      ?: env.catalog.find(it.component)?.defaultOf("variant")?.content
  }
  val rich = variant == "rich"
  val side = env.placement.tooltipSide(tooltipId, rich) ?: TooltipSpec.Side.Above
  val hasAction = node?.props?.get("actions") != null
  TooltipBox(
    positionProvider =
      TooltipDefaults.rememberTooltipPositionProvider(
        when (side) {
          TooltipSpec.Side.Above -> TooltipAnchorPosition.Above
          TooltipSpec.Side.Below -> TooltipAnchorPosition.Below
        },
      ),
    tooltip = {
      CompositionLocalProvider(LocalTooltipScope provides this) {
        RenderNode(
          env,
          ChildRef(tooltipId, scope),
          parentId,
          order + listOf(98, 0),
          ChildHost.Wrap,
        )
      }
    },
    state = state,
    hasAction = hasAction,
    content = anchor,
  )
}

/**
 * The tooltip itself, called inside the `TooltipBox`'s popup. Plain is one line of text. Rich adds
 * a subhead and up to two text buttons side by side.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderTooltip(ctx: NodeContext) {
  val scope = LocalTooltipScope.current ?: return
  val text = ctx.string("text").orEmpty()
  if (ctx.enum("variant") != "rich") {
    with(scope) { PlainTooltip(modifier = ctx.modifier()) { ctx.PartText(text, "text") } }
    return
  }
  val title = ctx.string("title")
  val hasActions = ctx.props.children("actions").isNotEmpty()
  with(scope) {
    RichTooltip(
      modifier = ctx.modifier(),
      title = title?.let { value -> { ctx.PartText(value, "title") } },
      action =
        if (hasActions) {
          {
            Row(horizontalArrangement = Arrangement.spacedBy(TooltipSpec.ACTION_GAP_DP.dp)) {
              ctx.Children("actions", 1, ChildHost(fillWidth = false, row = this))
            }
          }
        } else {
          null
        },
    ) {
      ctx.PartText(text, "text")
    }
  }
}
