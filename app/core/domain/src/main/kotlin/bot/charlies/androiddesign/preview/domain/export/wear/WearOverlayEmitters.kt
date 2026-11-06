package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.Stmt
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.lambda
import bot.charlies.androiddesign.preview.domain.export.lambdaOf
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.model.ComponentNode
import kotlinx.serialization.json.JsonObject

/** The size of an `AlertDialog` icon, as the previewer draws it. */
private const val DIALOG_ICON_DP = 32f

/**
 * The call that draws an overlay: an `AlertDialog` or `ConfirmationDialog` shown while its
 * `show...` state is true. Both take `visible`, so no `if` wraps them.
 */
internal fun WearScreenEmitter.overlayStatements(node: ComponentNode): List<Stmt> {
  val p = effectiveProps(node, large = false)
  val close = lambda(Assign(overlayVar(node.id), "=", Raw("false")))
  return when (node.component) {
    "AlertDialog" -> alertDialog(node, p, close)
    "ConfirmationDialog" -> confirmationDialog(node, p, close)
    else -> emptyList()
  }
}

private fun WearScreenEmitter.alertDialog(
  node: ComponentNode,
  p: JsonObject,
  close: bot.charlies.androiddesign.preview.domain.export.Lambda,
): List<Stmt> {
  // A dialog's two buttons share its width, like the upstream confirm and dismiss buttons do.
  val buttons = WearPlace(parentType = "AlertDialog", parentStretches = false, scopeWeight = true)
  val confirm =
    p.literal("confirm")?.let { emit(it, buttons) }
      ?: listOf(
        stmt(
          Call(
            "${wear("AlertDialogDefaults")}.ConfirmButton",
            listOf(arg("onClick", close)),
          ),
        ),
      )
  val dismiss = p.literal("dismiss")?.let { emit(it, buttons) }
  return listOf(
    stmt(
      Call(
        wear("AlertDialog"),
        listOfNotNull(
          arg("visible", overlayVar(node.id)),
          arg("onDismissRequest", close),
          arg("confirmButton", lambda(confirm)),
          arg("title", lambdaOf(textCall(p.literal("title").orEmpty()))),
          dismiss?.let { arg("dismissButton", lambda(it)) },
          p.literal("icon")?.let {
            arg("icon", lambdaOf(iconCall(painter(it), dp(DIALOG_ICON_DP))))
          },
          textSlot(p, "text"),
        ),
      ),
    ),
  )
}

private fun WearScreenEmitter.confirmationDialog(
  node: ComponentNode,
  p: JsonObject,
  close: bot.charlies.androiddesign.preview.domain.export.Lambda,
): List<Stmt> {
  val size = "${wear("ConfirmationDialogDefaults")}.IconSize"
  return listOf(
    stmt(
      Call(
        wear("ConfirmationDialog"),
        listOf(
          arg("visible", overlayVar(node.id)),
          arg("onDismissRequest", close),
          // The text parameter has no default: null is no text.
          p.literal("text")?.let { arg("text", lambdaOf(textCall(it))) } ?: arg("text", "null"),
        ),
        lambda(
          stmt(iconCall(painter(p.literal("icon").orEmpty()), size)),
        ),
      ),
    ),
  )
}
