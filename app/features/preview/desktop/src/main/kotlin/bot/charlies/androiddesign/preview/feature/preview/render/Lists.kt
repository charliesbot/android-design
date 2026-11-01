package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import bot.charlies.androiddesign.preview.model.ColorRole

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderList(ctx: NodeContext) {
  val segmented = ctx.enum("variant") == "segmented"
  ctx.StyledAmbient {
    Column(
      modifier = ctx.modifier(fillWidth = true).then(ctx.styleBackground()),
      verticalArrangement =
        if (segmented) Arrangement.spacedBy(ListItemDefaults.SegmentedGap) else Arrangement.Top,
    ) {
      ctx.Children("children", 0, ChildHost(fillWidth = true, column = this, segmented = segmented))
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderListItem(ctx: NodeContext) {
  val action = ctx.props.action("action")
  val selected = ctx.bool("selected")
  val headline = ctx.string("headline").orEmpty()
  val overline = ctx.string("overline")
  val supporting = ctx.string("supporting")
  val modifier = ctx.modifier(fillWidth = true)
  val overlineSlot: (@Composable () -> Unit)? = overline?.let { text ->
    { ctx.PartText(text, "overline", maxLines = 1) }
  }
  val supportingSlot: (@Composable () -> Unit)? = supporting?.let { text ->
    { ctx.PartText(text, "supporting", maxLines = 2) }
  }
  val leading: (@Composable () -> Unit)? =
    ctx.props.childId("leading")?.let { id -> { ctx.ChildNode(id, 0, ChildHost.Wrap) } }
  val trailing: (@Composable () -> Unit)? =
    ctx.props.childId("trailing")?.let { id -> { ctx.ChildNode(id, 1, ChildHost.Wrap) } }
  val headlineSlot: @Composable () -> Unit = {
    ctx.PartText(headline, "headline", maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
  if (ctx.host.segmented) {
    val shapes = ListItemDefaults.segmentedShapes(ctx.host.groupIndex, ctx.host.groupCount)
    val colors: ListItemColors =
      if (selected) {
        ListItemDefaults.segmentedColors(
          containerColor = ctx.env.color(ColorRole.SecondaryContainer),
          contentColor = ctx.env.color(ColorRole.OnSecondaryContainer),
        )
      } else {
        // The library default is the surface color, which vanishes on a surface background.
        ListItemDefaults.segmentedColors(containerColor = ctx.env.color(ColorRole.SurfaceContainer))
      }
    SegmentedListItem(
      onClick = { ctx.env.act(action) },
      shapes = shapes,
      modifier = modifier,
      leadingContent = leading,
      overlineContent = overlineSlot,
      supportingContent = supportingSlot,
      trailingContent = trailing,
      colors = colors,
      content = headlineSlot,
    )
  } else {
    val clickable = if (action != null) Modifier.clickable { ctx.env.act(action) } else Modifier
    ListItem(
      headlineContent = headlineSlot,
      modifier = modifier.then(clickable),
      overlineContent = overlineSlot,
      supportingContent = supportingSlot,
      leadingContent = leading,
      trailingContent = trailing,
      colors =
        if (selected) {
          ListItemDefaults.colors(containerColor = ctx.env.color(ColorRole.SecondaryContainer))
        } else {
          ListItemDefaults.colors()
        },
    )
  }
}
