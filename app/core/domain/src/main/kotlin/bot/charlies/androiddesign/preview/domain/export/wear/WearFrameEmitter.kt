package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Arg
import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.Stmt
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.lambda
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.model.ComponentNode
import kotlinx.serialization.json.JsonObject

/**
 * The `Screen` component: a `ScreenScaffold` (the `AppScaffold` sits once at the app's root) around
 * either a `TransformingLazyColumn` of the children, each an item transformed at the round
 * display's edge, or the children stacked in a `Box`. The edge button goes in the scaffold's slot;
 * the clock is the root's, and a screen with `timeText` off passes an empty one.
 */
internal fun WearScreenEmitter.screenFrame(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val scrolls = p.option("Screen", "scroll")
  val edgeId = p.literal("edgeButton")
  val edge: Arg? =
    edgeId
      ?.takeIf { byId.containsKey(it) }
      ?.let {
        arg("edgeButton", lambda(emit(it, WearPlace(parentType = "Screen"))))
      }
  // The caller sizes the screen, so the root takes only its own padding.
  val rootPlace = place.copy(parentStretches = false)
  val modifier =
    modifierArg(rootPlace, modifierParts(node, p, rootPlace, describe = false, item = false))
      ?: arg("modifier", "modifier")
  val timeText = if (p.option("Screen", "timeText")) null else arg("timeText", "{}")
  return if (scrolls) scrollingFrame(node, p, modifier, edge, timeText)
  else fixedFrame(node, p, modifier, edge, timeText)
}

private fun WearScreenEmitter.scrollingFrame(
  node: ComponentNode,
  p: JsonObject,
  modifier: Arg,
  edge: Arg?,
  timeText: Arg?,
): List<Stmt> {
  val state = file.use(WearSym.rememberTransformingLazyColumnState)
  val itemPlace = WearPlace(parentType = "Screen", item = true)
  val children =
    children(node, p, "children", itemPlace) { statements ->
      if (statements.isEmpty()) emptyList()
      else listOf(stmt(Call("item", lambda = lambda(statements))))
    }
  val list =
    Call(
      file.use(WearSym.TransformingLazyColumn),
      listOf(arg("state", "columnState"), arg("contentPadding", "contentPadding")),
      lambda(children.body),
    )
  val content =
    lambda(
      listOf(
        Assign("val transformationSpec", "=", Call(file.use(WearSym.rememberTransformationSpec))),
        stmt(list),
      ),
      params = "contentPadding",
    )
  val scaffold =
    Call(
      file.use(WearSym.ScreenScaffold),
      listOfNotNull(arg("scrollState", "columnState"), modifier, edge, timeText),
      content,
    )
  return children.prelude +
    listOf<Stmt>(Assign("val columnState", "=", Call(state))) +
    listOf(stmt(scaffold))
}

private fun WearScreenEmitter.fixedFrame(
  node: ComponentNode,
  p: JsonObject,
  modifier: Arg,
  edge: Arg?,
  timeText: Arg?,
): List<Stmt> {
  // Full bleed, as in the official samples: a ring or a stepper reaches the display's edge. With
  // an edge button only the bottom is padded, so the body stops above the button.
  val children = children(node, p, "children", WearPlace(parentType = "Screen", fillHeight = true))
  val boxModifier =
    if (edge == null) "${file.use(Sym.Modifier)}.${useModifier("fillMaxSize()")}"
    else
      "${file.use(Sym.Modifier)}.${useModifier("fillMaxSize()")}" +
        ".${useModifier("padding")}(bottom = contentPadding.calculateBottomPadding())"
  val body = stmt(Call(file.use(Sym.Box), listOf(pos(boxModifier)), lambda(children.body)))
  val scroll =
    if (edge != null) {
      usesFixedScroll = true
      arg("scrollInfoProvider", "FixedScrollInfoProvider")
    } else null
  val scaffold =
    Call(
      file.use(WearSym.ScreenScaffold),
      listOfNotNull(scroll, modifier, edge, timeText),
      lambda(listOf(body), params = if (edge != null) "contentPadding" else null),
    )
  return children.prelude + listOf(stmt(scaffold))
}

private fun pos(text: String) = bot.charlies.androiddesign.preview.domain.export.pos(Raw(text))
