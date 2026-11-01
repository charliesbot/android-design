package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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

internal fun NodeContext.bottomSheetLook() =
  Look(BottomSheetAmbient.container, BottomSheetAmbient.content, shape = "extraLarge")

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
    DialogAmbient.container,
    DialogAmbient.content,
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

internal fun NodeContext.menuLook() =
  Look(MenuAmbient.container, MenuAmbient.content, shape = "extraLarge")

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
    MenuAmbient.container,
    MenuAmbient.content,
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
