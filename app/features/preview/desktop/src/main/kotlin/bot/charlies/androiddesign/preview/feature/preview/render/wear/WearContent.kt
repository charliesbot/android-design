package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import bot.charlies.androiddesign.preview.feature.preview.render.ChildHost
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.render.describedBy
import bot.charlies.androiddesign.preview.feature.preview.render.modifier
import bot.charlies.androiddesign.preview.feature.preview.render.textReporter

// region text and icon

private fun NodeContext.typeWire(default: String): String =
  string("type") ?: style.def?.type ?: default

private fun NodeContext.colorWire(ambient: WearAmbient): String =
  string("color") ?: style.def?.color ?: ambient.content

internal fun NodeContext.textLook(ambient: WearAmbient) =
  WearLook(
    style.def?.container ?: ambient.container,
    colorWire(ambient),
    typeWire("bodyLarge"),
    text = string("text"),
  )

@Composable
internal fun RenderWearText(ctx: NodeContext) {
  val wear = ctx.env.wear
  val ambient = LocalWearAmbient.current
  val container = ctx.style.def?.container?.let(wear::color)
  Text(
    text = ctx.string("text").orEmpty(),
    modifier =
      ctx
        .modifier()
        .then(if (container != null) Modifier.background(container) else Modifier)
        .then(ctx.itemModifier()),
    color = wear.color(ctx.colorWire(ambient)),
    style = wear.style(ctx.typeWire("bodyLarge")),
    textAlign =
      when (ctx.string("align")) {
        "center" -> TextAlign.Center
        "end" -> TextAlign.End
        "start" -> TextAlign.Start
        else -> null
      },
    maxLines = ctx.whole("maxLines") ?: Int.MAX_VALUE,
    overflow =
      if (ctx.string("overflow") == "ellipsis") TextOverflow.Ellipsis else TextOverflow.Clip,
    onTextLayout = ctx.textReporter(),
  )
}

internal fun NodeContext.iconLook(ambient: WearAmbient) =
  WearLook(ambient, content = colorWire(ambient), a11yRole = "Image")

@Composable
internal fun RenderWearIcon(ctx: NodeContext) {
  val wear = ctx.env.wear
  SymbolIcon(
    name = ctx.string("name").orEmpty(),
    modifier = ctx.modifier().then(ctx.itemModifier()),
    size = ctx.numberOrDefault("size").dp,
    tint = wear.color(ctx.colorWire(LocalWearAmbient.current)),
    filled = ctx.bool("filled"),
    contentDescription = ctx.string("contentDescription"),
  )
}

internal fun NodeContext.imageLook(ambient: WearAmbient) = WearLook(ambient, a11yRole = "Image")

// endregion

// region cards

private val CardAmbient = WearAmbient("surfaceContainer", "onSurface")

internal fun NodeContext.cardLook(): WearLook {
  val text = string("title")
  return WearLook(
    CardAmbient,
    type = if (component == "Card") null else "titleMedium",
    shape = "card",
    text = text,
    a11yRole = if (def.interactive && props.action("action") != null) "Button" else null,
  )
}

@Composable
private fun NodeContext.cardModifier() = modifier().then(describedBy()).then(itemModifier())

@Composable
internal fun RenderWearCard(ctx: NodeContext) {
  val action = ctx.props.action("action")
  val content: @Composable ColumnScope.() -> Unit = {
    CompositionLocalProvider(LocalWearAmbient provides CardAmbient) {
      ctx.Children("children", 0, ChildHost(fillWidth = ctx.stretchesChildren, column = this))
    }
  }
  if (action != null) {
    androidx.wear.compose.material3.Card(
      onClick = { ctx.env.act(action) },
      modifier = ctx.cardModifier(),
      enabled = ctx.enabled,
      transformation = ctx.transformation(),
      content = content,
    )
  } else {
    androidx.wear.compose.material3.Card(
      modifier = ctx.cardModifier(),
      transformation = ctx.transformation(),
      content = content,
    )
  }
}

@Composable
internal fun RenderWearTitleCard(ctx: NodeContext) {
  val action = ctx.props.action("action")
  val title: @Composable RowScope.() -> Unit = {
    ctx.WearText(ctx.string("title").orEmpty(), "title")
  }
  val time: (@Composable () -> Unit)? =
    ctx.string("time")?.let { text -> { ctx.WearText(text, "time") } }
  val subtitle: (@Composable ColumnScope.() -> Unit)? =
    ctx.string("subtitle")?.let { text -> { ctx.WearText(text, "subtitle") } }
  val body: (@Composable () -> Unit)? =
    ctx.string("body")?.let { text -> { ctx.WearText(text, "body") } }
  if (action != null) {
    TitleCard(
      onClick = { ctx.env.act(action) },
      title = title,
      modifier = ctx.cardModifier(),
      time = time,
      subtitle = subtitle,
      enabled = ctx.enabled,
      transformation = ctx.transformation(),
      content = body,
    )
  } else {
    TitleCard(
      title = title,
      modifier = ctx.cardModifier(),
      time = time,
      subtitle = subtitle,
      transformation = ctx.transformation(),
      content = body,
    )
  }
}

@Composable
internal fun RenderWearAppCard(ctx: NodeContext) {
  val action = ctx.props.action("action")
  val appName: @Composable RowScope.() -> Unit = {
    ctx.WearText(ctx.string("appName").orEmpty(), "appName")
  }
  val title: @Composable RowScope.() -> Unit = {
    ctx.WearText(ctx.string("title").orEmpty(), "title")
  }
  val image: (@Composable RowScope.() -> Unit)? =
    ctx.string("appIcon")?.let { name -> { WearIcon(name, CardDefaults.AppImageSize) } }
  val time: (@Composable RowScope.() -> Unit)? =
    ctx.string("time")?.let { text -> { ctx.WearText(text, "time") } }
  val body: @Composable ColumnScope.() -> Unit = {
    ctx.string("body")?.let { ctx.WearText(it, "body") }
  }
  if (action != null) {
    AppCard(
      onClick = { ctx.env.act(action) },
      appName = appName,
      title = title,
      modifier = ctx.cardModifier(),
      enabled = ctx.enabled,
      transformation = ctx.transformation(),
      appImage = image,
      time = time,
      content = body,
    )
  } else {
    AppCard(
      appName = appName,
      title = title,
      modifier = ctx.cardModifier(),
      transformation = ctx.transformation(),
      appImage = image,
      time = time,
      content = body,
    )
  }
}

// endregion
