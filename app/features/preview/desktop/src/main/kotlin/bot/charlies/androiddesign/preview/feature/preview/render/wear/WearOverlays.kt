package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import bot.charlies.androiddesign.preview.feature.preview.render.ActionCall
import bot.charlies.androiddesign.preview.feature.preview.render.ChildHost
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.modifier

private const val DIALOG_ICON_DP = 32

private val DialogAmbient = WearAmbient("background", "onBackground")

internal fun NodeContext.alertDialogLook() =
  WearLook(DialogAmbient, type = "titleMedium", shape = "full", text = string("title"))

@Composable
internal fun RenderWearAlertDialog(ctx: NodeContext) {
  val dismiss = { ctx.env.act(ActionCall.Dismiss) }
  val icon = ctx.string("icon")
  val text = ctx.string("text")
  val confirm = ctx.props.childId("confirm")
  val dismissButton = ctx.props.childId("dismiss")
  CompositionLocalProvider(LocalWearAmbient provides DialogAmbient) {
    AlertDialog(
      visible = true,
      onDismissRequest = dismiss,
      confirmButton = {
        if (confirm != null) {
          ctx.ChildNode(confirm, 1, rowMember())
        } else {
          AlertDialogDefaults.ConfirmButton(dismiss)
        }
      },
      title = { ctx.WearText(ctx.string("title").orEmpty(), "title") },
      modifier = ctx.modifier(),
      dismissButton = {
        if (dismissButton != null) {
          ctx.ChildNode(dismissButton, 2, rowMember())
        } else {
          AlertDialogDefaults.DismissButton(dismiss)
        }
      },
      icon = icon?.let { name -> { WearIcon(name, DIALOG_ICON_DP.dp) } },
      text = text?.let { value -> { ctx.WearText(value, "text") } },
    )
  }
}

/** A dialog's buttons keep their own size in upstream's centered row, as its defaults do. */
@Composable private fun RowScope.rowMember() = ChildHost(fillWidth = false, row = this)

internal fun NodeContext.confirmationDialogLook() =
  WearLook(DialogAmbient, type = "titleMedium", shape = "full", text = string("text"))

@Composable
internal fun RenderWearConfirmationDialog(ctx: NodeContext) {
  val icon = ctx.string("icon")
  val text = ctx.string("text")
  CompositionLocalProvider(LocalWearAmbient provides DialogAmbient) {
    ConfirmationDialog(
      visible = true,
      onDismissRequest = { ctx.env.act(ActionCall.Dismiss) },
      text = text?.let { value -> { ctx.WearText(value, "text") } },
      modifier = ctx.modifier(),
    ) {
      WearIcon(icon, ConfirmationDialogDefaults.IconSize)
    }
  }
}
