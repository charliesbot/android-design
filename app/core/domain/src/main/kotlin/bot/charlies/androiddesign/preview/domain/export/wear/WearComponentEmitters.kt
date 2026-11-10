package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Arg
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.Chain
import bot.charlies.androiddesign.preview.domain.export.Expr
import bot.charlies.androiddesign.preview.domain.export.IconName
import bot.charlies.androiddesign.preview.domain.export.Naming
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.Stmt
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.arrangement
import bot.charlies.androiddesign.preview.domain.export.contentScale
import bot.charlies.androiddesign.preview.domain.export.horizontalAlignment
import bot.charlies.androiddesign.preview.domain.export.lambdaOf
import bot.charlies.androiddesign.preview.domain.export.one
import bot.charlies.androiddesign.preview.domain.export.opt
import bot.charlies.androiddesign.preview.domain.export.pos
import bot.charlies.androiddesign.preview.domain.export.spacer
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.domain.export.textAlign
import bot.charlies.androiddesign.preview.domain.export.verticalAlignment
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.WearComponent
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal fun WearScreenEmitter.wear(name: String): String = file.use(WearSym.m3(name))

/**
 * Draws [node] with the props it has on this display; the dialogs are written at the screen's end.
 */
internal fun WearScreenEmitter.emitComponent(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  when (WearComponent.of(node.component)) {
    WearComponent.Screen -> screenFrame(node, p, place)
    WearComponent.Column -> column(node, p, place)
    WearComponent.Row -> row(node, p, place)
    WearComponent.Box -> box(node, p, place)
    WearComponent.Spacer -> spacer(p, place)
    WearComponent.ListHeader -> listHeader(node, p, place)
    WearComponent.ListSubHeader -> listSubHeader(node, p, place)
    WearComponent.Button -> button(node, p, place)
    WearComponent.CompactButton -> compactButton(node, p, place)
    WearComponent.IconButton -> iconButton(node, p, place)
    WearComponent.TextButton -> textButton(node, p, place)
    WearComponent.EdgeButton -> edgeButton(node, p, place)
    WearComponent.ButtonGroup -> buttonGroup(node, p, place)
    WearComponent.IconToggleButton -> iconToggleButton(node, p, place)
    WearComponent.TextToggleButton -> textToggleButton(node, p, place)
    WearComponent.SwitchButton -> rowToggle(node, p, place, "SwitchButton")
    WearComponent.CheckboxButton -> rowToggle(node, p, place, "CheckboxButton")
    WearComponent.RadioButton -> radioButton(node, p, place)
    WearComponent.Text -> text(node, p, place)
    WearComponent.Icon -> iconComponent(node, p, place)
    WearComponent.Image -> image(node, p, place)
    WearComponent.Card -> card(node, p, place)
    WearComponent.TitleCard -> titleCard(node, p, place)
    WearComponent.AppCard -> appCard(node, p, place)
    WearComponent.Slider -> slider(node, p, place)
    WearComponent.Stepper -> stepper(node, p, place)
    WearComponent.CircularProgressIndicator -> circularProgress(node, p, place)
    WearComponent.LinearProgressIndicator -> linearProgress(node, p, place)
    WearComponent.SegmentedCircularProgressIndicator -> segmentedProgress(node, p, place)
    // Dialogs are written at the end of the screen.
    WearComponent.AlertDialog,
    WearComponent.ConfirmationDialog -> emptyList()
  }

// region shared pieces

/** `Icon(painter, contentDescription = ..., modifier = ..., tint = ...)`. */
internal fun WearScreenEmitter.iconCall(
  painter: String,
  size: String?,
  description: Expr? = null,
  tint: String? = null,
  leading: List<String> = emptyList(),
): Expr {
  val parts = leading + listOfNotNull(size?.let { "${useModifier("size")}($it)" })
  return Call(
    file.use(WearSym.Icon),
    listOfNotNull(
      pos(painter),
      arg("contentDescription", description ?: Raw("null")),
      if (parts.isEmpty()) null else arg("modifier", Chain(file.use(Sym.Modifier), parts)),
      opt("tint", tint),
    ),
  )
}

/** An icon slot `icon = { Icon(...) }` sized [size], or null without an icon name. */
internal fun WearScreenEmitter.iconSlot(name: IconName?, size: String): Arg? = name?.let {
  arg("icon", lambdaOf(iconCall(painter(it), size)))
}

internal fun WearScreenEmitter.textCall(value: Expr, vararg named: Arg?): Expr =
  Call(file.use(WearSym.Text), listOf(pos(value)) + named.filterNotNull())

/** `argName = { Text(...) }` for the text at [key], or null when there is none. */
internal fun WearScreenEmitter.textSlot(
  p: JsonObject,
  key: String,
  place: WearPlace,
  argName: String = key,
): Arg? = p.text(key, place)?.let { arg(argName, lambdaOf(textCall(it))) }

/** `enabled = ...` for a component that can be disabled: absent while it is always enabled. */
internal fun WearScreenEmitter.enabledArg(p: JsonObject, place: WearPlace): Arg? =
  p.boolExpr("enabled", place)?.takeIf { it != "true" }?.let { arg("enabled", it) }

// endregion

// region layout

/**
 * A style's container as modifier calls, clipped to the style's shape first like the previewer
 * draws it. Empty without a container.
 */
private fun WearScreenEmitter.containerBackground(node: ComponentNode): List<String> {
  val style = styleOf(node) ?: return emptyList()
  val container = style.container ?: return emptyList()
  val shape = style.shape?.let(::shapeToken)
  val background = "${useModifier("background")}(${colorRole(container)})"
  return if (shape == null) listOf(background)
  else listOf("${useModifier("clip")}($shape)", background)
}

/**
 * [layout] wrapped in `CompositionLocalProvider(LocalContentColor provides ...)` when the node's
 * style sets a content color, as the previewer's styled ambient does for the children.
 */
private fun WearScreenEmitter.styledAmbient(node: ComponentNode, layout: List<Stmt>): List<Stmt> {
  val color = styleOf(node)?.color ?: return layout
  val provider = file.use(WearSym.CompositionLocalProvider)
  val local = file.use(WearSym.LocalContentColor)
  return one(provider, listOf(pos(Raw("$local provides ${colorRole(color)}"))), layout)
}

private fun WearScreenEmitter.layoutModifier(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
  vararg extra: String?,
): Arg? {
  val parts =
    modifierParts(node, p, place, describe = false, item = false, fillHeight = true) +
      containerBackground(node) +
      extra.filterNotNull()
  return modifierArg(place, parts)
}

private fun WearScreenEmitter.column(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val alignment = horizontalAlignment(p)
  val scroll =
    if (p.option("Column", "scroll")) {
      file.use(Sym.verticalScroll)
      "verticalScroll(${file.use(Sym.rememberScrollState)}())"
    } else null
  val children = children(node, p, "children", place.forChild("Column"))
  return styledAmbient(
    node,
    one(
      file.use(Sym.Column),
      listOf(
        layoutModifier(node, p, place, scroll),
        opt("verticalArrangement", arrangement(p, vertical = true)),
        opt("horizontalAlignment", alignment),
      ),
      children.prelude + children.body,
    ),
  )
}

private fun WearScreenEmitter.row(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val alignment = verticalAlignment(p)
  // Stretch offers the row's height to its Column and Box children, the full-height panes.
  val childPlace =
    place.forChild("Row").copy(fillHeight = p.literal("verticalAlignment") == "stretch")
  val children = children(node, p, "children", childPlace)
  return styledAmbient(
    node,
    one(
      file.use(Sym.Row),
      listOf(
        layoutModifier(node, p, place),
        opt("horizontalArrangement", arrangement(p, vertical = false)),
        opt("verticalAlignment", alignment),
      ),
      children.prelude + children.body,
    ),
  )
}

private fun WearScreenEmitter.box(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val alignment =
    p.literal("contentAlignment")
      ?.takeIf { it != "topStart" }
      ?.let { "${file.use(Sym.Alignment)}.${Naming.pascal(it)}" }
  val children = children(node, p, "children", place.forChild("Box"))
  return styledAmbient(
    node,
    one(
      file.use(Sym.Box),
      listOf(layoutModifier(node, p, place), opt("contentAlignment", alignment)),
      children.prelude + children.body,
    ),
  )
}

// endregion

// region headers, text, icon, image

private fun WearScreenEmitter.listHeader(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("ListHeader"),
    listOf(modifier(node, p, place, item = true), transformation(place)),
    listOf(stmt(textCall(p.textOrEmpty("text", place)))),
  )

private fun WearScreenEmitter.listSubHeader(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("ListSubHeader"),
    listOf(
      modifier(node, p, place, item = true),
      transformation(place),
      iconSlot(p.iconName("icon", place), "${wear("ButtonDefaults")}.IconSize"),
    ),
    listOf(stmt(textCall(p.textOrEmpty("text", place)))),
  )

private fun WearScreenEmitter.text(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val style = styleOf(node)
  val overflow =
    if (p.literal("overflow") == "ellipsis") "${file.use(Sym.TextOverflow)}.Ellipsis" else null
  val align = textAlign(p)
  // The theme sets bodyLarge; inside a card or a stepper the upstream component sets another, and
  // the previewer draws the stated role there, so the export states it too.
  val type = p.literal("type") ?: style?.type ?: "bodyLarge"
  val stateType = type != "bodyLarge" || !place.bodyLarge
  // The style's container is drawn before the list item's height transformation, as in the preview.
  val parts =
    modifierParts(node, p, place, describe = false, item = false) +
      listOfNotNull(style?.container?.let { "${useModifier("background")}(${colorRole(it)})" }) +
      listOfNotNull(if (place.item) itemHeight() else null)
  return listOf(
    stmt(
      textCall(
        p.textOrEmpty("text", place),
        modifierArg(place, parts),
        opt("color", (p.literal("color") ?: style?.color)?.let(::colorRole)),
        opt("style", if (stateType) typeRole(type) else null),
        opt("textAlign", align),
        opt("overflow", overflow),
        opt("maxLines", p.numberProp("Text", "maxLines")?.toInt()?.toString()),
      ),
    ),
  )
}

private fun WearScreenEmitter.iconComponent(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val size = dp(p.numberProp("Icon", "size") ?: 24f)
  val name = p.iconName("name", place) ?: IconName.Literal("")
  val leading = modifierParts(node, p, place, describe = false, item = true)
  val color = (p.literal("color") ?: styleOf(node)?.color)?.let(::colorRole)
  return listOf(
    stmt(
      iconCall(
        painter(name, filled = p.sampleFlag("filled", place) == true),
        size,
        description = p.text("contentDescription", place),
        tint = color,
        leading = leading,
      ),
    ),
  )
}

private fun WearScreenEmitter.image(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val scale = contentScale(p)
  val aspect =
    p.numberProp("Image", "aspectRatio")?.let {
      "${useModifier("aspectRatio")}(${floatLiteral(it)})"
    }
  val parts = modifierParts(node, p, place, describe = false, item = false) + listOfNotNull(aspect)
  return one(
    file.use(Sym.Image),
    listOf(
      arg(
        "painter",
        "${file.use(Sym.painterResource)}(${imageDrawable(node, p, place, asImages = true)})",
      ),
      arg("contentDescription", p.text("contentDescription", place) ?: Raw("null")),
      modifierArg(place, parts),
      arg("contentScale", "${file.use(Sym.ContentScale)}.$scale"),
    ),
  )
}

// endregion

// region cards

private fun clickable(action: JsonElement?): Boolean = action is JsonObject

private fun WearScreenEmitter.onClickOrNull(node: ComponentNode, p: JsonObject): Arg? =
  if (clickable(p["action"])) arg("onClick", actionLambda(p["action"], node.id)) else null

private fun WearScreenEmitter.card(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val children = children(node, p, "children", place.forChild("Card"))
  return children.prelude +
    one(
      wear("Card"),
      listOf(
        onClickOrNull(node, p),
        modifier(node, p, place, describe = true, item = true),
        if (clickable(p["action"])) enabledArg(p, place) else null,
        transformation(place),
      ),
      children.body,
    )
}

private fun WearScreenEmitter.titleCard(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("TitleCard"),
    listOf(
      onClickOrNull(node, p),
      arg("title", lambdaOf(textCall(p.textOrEmpty("title", place)))),
      modifier(node, p, place, describe = true, item = true),
      textSlot(p, "time", place),
      textSlot(p, "subtitle", place),
      if (clickable(p["action"])) enabledArg(p, place) else null,
      transformation(place),
      textSlot(p, "body", place, "content"),
    ),
  )

private fun WearScreenEmitter.appCard(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val body = p.text("body", place)?.let { listOf(stmt(textCall(it))) } ?: emptyList()
  return one(
    wear("AppCard"),
    listOf(
      onClickOrNull(node, p),
      arg("appName", lambdaOf(textCall(p.textOrEmpty("appName", place)))),
      arg("title", lambdaOf(textCall(p.textOrEmpty("title", place)))),
      modifier(node, p, place, describe = true, item = true),
      if (clickable(p["action"])) enabledArg(p, place) else null,
      transformation(place),
      p.iconName("appIcon", place)?.let {
        arg("appImage", lambdaOf(iconCall(painter(it), "${wear("CardDefaults")}.AppImageSize")))
      },
      textSlot(p, "time", place),
    ),
    body,
  )
}

// endregion
