package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.TypeRole

@Composable
internal fun RenderText(ctx: NodeContext) {
  val align =
    when (ctx.string("align")) {
      "center" -> TextAlign.Center
      "end" -> TextAlign.End
      "start" -> TextAlign.Start
      else -> null
    }
  val color = ctx.colorRole()?.let(ctx.env::color) ?: Color.Unspecified
  Text(
    text = ctx.string("text").orEmpty(),
    modifier = ctx.modifier().then(ctx.styleBackground()),
    color = color,
    style = ctx.textStyle(TypeRole.BodyLarge),
    textAlign = align,
    maxLines = ctx.whole("maxLines") ?: Int.MAX_VALUE,
    overflow =
      if (ctx.string("overflow") == "ellipsis") TextOverflow.Ellipsis else TextOverflow.Clip,
    onTextLayout = ctx.textReporter(),
  )
}

/** Text paints its style's container behind itself, so that is its backdrop. */
internal fun NodeContext.textLook(ambient: Ambient) =
  Look(
    backdropRole(ambient),
    colorRole() ?: ambient.content,
    typeRole(TypeRole.BodyLarge),
    text = string("text"),
  )

@Composable
internal fun RenderIcon(ctx: NodeContext) {
  val color = ctx.colorRole()?.let(ctx.env::color) ?: Color.Unspecified
  SymbolIcon(
    name = ctx.string("name").orEmpty(),
    modifier = ctx.modifier(),
    size = ctx.numberOrDefault("size").dp,
    tint = color,
    filled = ctx.bool("filled"),
    contentDescription = ctx.string("contentDescription"),
  )
}

internal fun NodeContext.iconLook(ambient: Ambient) =
  Look(ambient.container, colorRole() ?: ambient.content, a11yRole = "Image")

@Composable
internal fun RenderImage(ctx: NodeContext) {
  val bitmap = ctx.string("src")?.let(ctx.env.assets::image)
  val shape = ctx.shapeName()?.let { shapeFor(it) }
  val aspectRatio = ctx.number("aspectRatio")
  var modifier = ctx.modifier()
  if (aspectRatio != null) modifier = modifier.aspectRatio(aspectRatio)
  if (shape != null) modifier = modifier.clip(shape)
  val description = ctx.string("contentDescription")
  val semantics = Modifier.semantics {
    role = Role.Image
    if (description != null) contentDescription = description
  }
  if (bitmap == null) {
    // The file is missing or unreadable: a neutral block keeps the layout honest.
    Box(
      modifier
        .then(semantics)
        .background(ctx.env.color(ColorRole.SurfaceVariant))
        .then(
          if (aspectRatio == null) Modifier.aspectRatio(1f).fillMaxWidth(0.5f) else Modifier,
        ),
    )
    return
  }
  Image(
    bitmap = bitmap,
    contentDescription = description,
    modifier = modifier,
    contentScale =
      when (ctx.string("fit")) {
        "crop" -> ContentScale.Crop
        "fillBounds" -> ContentScale.FillBounds
        "fillWidth" -> ContentScale.FillWidth
        "fillHeight" -> ContentScale.FillHeight
        "inside" -> ContentScale.Inside
        else -> ContentScale.Fit
      },
  )
}

internal fun NodeContext.imageLook(ambient: Ambient) =
  Look(ambient.container, ambient.content, a11yRole = "Image")
