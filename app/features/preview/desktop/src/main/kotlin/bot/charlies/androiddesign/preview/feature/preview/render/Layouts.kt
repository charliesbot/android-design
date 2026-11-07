package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog

private fun verticalArrangement(name: String?, spacing: Dp?): Arrangement.Vertical {
  val gap = spacing ?: 0.dp
  return when (name) {
    "center" -> Arrangement.spacedBy(gap, Alignment.CenterVertically)
    "end" -> Arrangement.spacedBy(gap, Alignment.Bottom)
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> if (spacing != null) Arrangement.spacedBy(gap) else Arrangement.Top
  }
}

private fun horizontalArrangement(name: String?, spacing: Dp?): Arrangement.Horizontal {
  val gap = spacing ?: 0.dp
  return when (name) {
    "center" -> Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
    "end" -> Arrangement.spacedBy(gap, Alignment.End)
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> if (spacing != null) Arrangement.spacedBy(gap) else Arrangement.Start
  }
}

private fun alignmentOf(name: String?): Alignment =
  when (name) {
    "topCenter" -> Alignment.TopCenter
    "topEnd" -> Alignment.TopEnd
    "centerStart" -> Alignment.CenterStart
    "center" -> Alignment.Center
    "centerEnd" -> Alignment.CenterEnd
    "bottomStart" -> Alignment.BottomStart
    "bottomCenter" -> Alignment.BottomCenter
    "bottomEnd" -> Alignment.BottomEnd
    else -> Alignment.TopStart
  }

/**
 * The look of every layout component: a style's container and color over the ambient, as
 * `StyledAmbient` and `styleBackground` draw them.
 */
internal fun NodeContext.layoutLook(ambient: Ambient): Look = Look(styledAmbient(ambient))

/** The role a Shape paints behind its content, if any: its `color` prop, else its style's color. */
private fun NodeContext.shapeFill(): ColorRole? = colorRole()

/** A Shape draws only its fill; it does not apply its style's container or content color. */
internal fun NodeContext.shapeLook(ambient: Ambient): Look =
  Look(shapeFill() ?: ambient.container, ambient.content)

@Composable
internal fun RenderColumn(ctx: NodeContext) {
  val alignment =
    when (ctx.string("horizontalAlignment")) {
      "center" -> Alignment.CenterHorizontally
      "end" -> Alignment.End
      else -> Alignment.Start
    }
  var modifier = ctx.modifier(fillHeight = true, container = true)
  if (ctx.bool("scroll")) modifier = modifier.verticalScroll(rememberScrollState())
  ctx.StyledAmbient {
    Column(
      modifier = modifier,
      verticalArrangement = verticalArrangement(ctx.string("arrangement"), ctx.spacing("spacing")),
      horizontalAlignment = alignment,
    ) {
      ctx.Children("children", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, column = this))
    }
  }
}

@Composable
internal fun RenderRow(ctx: NodeContext) {
  val verticalAlignment = ctx.string("verticalAlignment")
  val alignment =
    when (verticalAlignment) {
      "center" -> Alignment.CenterVertically
      "bottom" -> Alignment.Bottom
      else -> Alignment.Top
    }
  val hasVerticalDivider =
    ctx.props.children("children").any {
      val child = ctx.env.nodes[it.id]
      child?.component == "Divider" && child.props["vertical"].toString() == "true"
    }
  // A row with a vertical divider is as tall as its tallest child so the divider can span it. When
  // the row is offered a height, that intrinsic height is coerced to the filled one.
  var modifier = ctx.modifier(fillHeight = true, container = true)
  if (hasVerticalDivider) modifier = modifier.height(IntrinsicSize.Min)
  ctx.StyledAmbient {
    Row(
      modifier = modifier,
      horizontalArrangement =
        horizontalArrangement(ctx.string("arrangement"), ctx.spacing("spacing")),
      verticalAlignment = alignment,
    ) {
      // Stretch offers the row's height to its Column and Box children, the full-height panes.
      ctx.Children(
        "children",
        0,
        ChildHost(
          fillWidth = ctx.def.stretchesChildren,
          fillHeight = verticalAlignment == "stretch",
          row = this,
        ),
      )
    }
  }
}

@Composable
internal fun RenderBox(ctx: NodeContext) {
  ctx.StyledAmbient {
    Box(
      modifier = ctx.modifier(fillHeight = true, container = true),
      contentAlignment = alignmentOf(ctx.string("contentAlignment")),
    ) {
      ctx.Children("children", 0, ChildHost(fillWidth = ctx.def.stretchesChildren))
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RenderGrid(ctx: NodeContext) {
  val gap = ctx.spacing("spacing") ?: 0.dp
  val modifier = ctx.modifier(container = true)
  val fixed = ctx.whole("columns")
  val minWidth = ctx.number("minColumnWidth")
  ctx.StyledAmbient {
    if (fixed == null && minWidth != null) {
      BoxWithConstraints(modifier) {
        val count =
          ((maxWidth + gap) / (minWidth.dp + gap))
            .toInt()
            .coerceIn(1, M3ExpressiveCatalog.MAX_GRID_COLUMNS)
        GridOf(ctx, count, gap, Modifier.fillMaxWidth())
      }
    } else {
      GridOf(ctx, fixed ?: 1, gap, modifier)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridOf(ctx: NodeContext, columns: Int, gap: Dp, modifier: Modifier) {
  Grid(
    config = {
      repeat(columns) { column(1.fr) }
      gap(gap)
    },
    modifier = modifier,
  ) {
    ctx.Children("children", 0, ChildHost(fillWidth = ctx.def.stretchesChildren))
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RenderFlexBox(ctx: NodeContext) {
  val gap = ctx.spacing("gap") ?: 0.dp
  val wrap = ctx.bool("wrap")
  ctx.StyledAmbient {
    FlexBox(
      modifier = ctx.modifier(container = true),
      config =
        FlexBoxConfig {
          wrap(if (wrap) FlexWrap.Wrap else FlexWrap.NoWrap)
          gap(gap)
        },
    ) {
      ctx.Children("children", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, flexItem = true))
    }
  }
}

@Composable
internal fun RenderSpacer(ctx: NodeContext) {
  var modifier = ctx.modifier()
  ctx.spacing("width")?.let { modifier = modifier.width(it) }
  ctx.spacing("height")?.let { modifier = modifier.height(it) }
  Spacer(modifier)
}

@Composable
internal fun RenderDivider(ctx: NodeContext) {
  if (ctx.bool("vertical")) {
    VerticalDivider(ctx.modifier())
  } else {
    HorizontalDivider(ctx.modifier())
  }
}

@Composable
internal fun RenderShape(ctx: NodeContext) {
  val shape = ctx.shapeName()?.let { shapeFor(it) }
  val fill = ctx.shapeFill()
  var modifier = ctx.modifier()
  if (shape != null) modifier = modifier.clip(shape)
  if (fill != null) modifier = modifier.background(ctx.env.color(fill))
  Box(modifier) {
    ctx.Child("content", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, fillHeight = false))
  }
}
