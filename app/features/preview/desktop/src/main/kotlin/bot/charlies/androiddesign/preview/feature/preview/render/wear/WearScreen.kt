package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ScrollInfoProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeSource
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import bot.charlies.androiddesign.preview.feature.preview.render.ChildHost
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.RenderNode
import bot.charlies.androiddesign.preview.feature.preview.render.modifier

/** The clock every Wear render shows, so renders stay comparable. */
internal const val FIXED_TIME = "10:09"

private object FixedTimeSource : TimeSource {
  @Composable override fun currentTime(): String = FIXED_TIME
}

internal fun NodeContext.screenLook() = WearLook("background", "onBackground")

/**
 * The screen frame: an `AppScaffold` with the (fixed) time text around a `ScreenScaffold` whose
 * content is either a `TransformingLazyColumn` of the children, each an item transformed at the
 * round display's edge, or the children stacked in a box.
 */
@Composable
internal fun RenderWearScreen(ctx: NodeContext) {
  val edgeButton = ctx.props.childId("edgeButton")
  val ambient = WearAmbient("background", "onBackground")
  CompositionLocalProvider(LocalWearAmbient provides ambient) {
    AppScaffold(
      modifier = Modifier.fillMaxSize().then(ctx.modifier(fillHeight = true)),
      timeText = {
        if (ctx.bool("timeText")) TimeText(timeSource = FixedTimeSource)
      },
    ) {
      if (ctx.bool("scroll")) ScrollingScreen(ctx, edgeButton) else FixedScreen(ctx, edgeButton)
    }
  }
}

@Composable
private fun ScrollingScreen(ctx: NodeContext, edgeButton: String?) {
  val state = rememberTransformingLazyColumnState()
  val content:
    @Composable
    androidx.compose.foundation.layout.BoxScope.(
      androidx.compose.foundation.layout.PaddingValues,
    ) -> Unit =
    { padding ->
      val spec = rememberTransformationSpec()
      val refs = ctx.props.children("children")
      TransformingLazyColumn(state = state, contentPadding = padding) {
        refs.forEachIndexed { index, ref ->
          item {
            CompositionLocalProvider(LocalWearItem provides WearItem(ctx.id, this, spec)) {
              RenderNode(
                env = ctx.env,
                ref = ref,
                parentId = ctx.id,
                order = ctx.order + listOf(0, index),
                host = ChildHost.Flow.at(index, refs.size),
              )
            }
          }
        }
      }
    }
  if (edgeButton != null) {
    ScreenScaffold(
      scrollState = state,
      edgeButton = { ctx.ChildNode(edgeButton, 1) },
      content = content,
    )
  } else {
    ScreenScaffold(scrollState = state, content = content)
  }
}

@Composable
private fun FixedScreen(ctx: NodeContext, edgeButton: String?) {
  val body:
    @Composable
    androidx.compose.foundation.layout.BoxScope.(
      androidx.compose.foundation.layout.PaddingValues,
    ) -> Unit =
    { padding ->
      // Full bleed, as in the official samples: a ring or a stepper reaches the display's edge.
      // With an edge button only the bottom is padded (the scaffold passes the button's height
      // plus spacing), so the body keeps its width and stops above the button.
      val bottom = if (edgeButton != null) padding.calculateBottomPadding() else 0.dp
      Box(Modifier.fillMaxSize().padding(bottom = bottom)) {
        ctx.Children("children", 0, ChildHost.Fill)
      }
    }
  if (edgeButton != null) {
    ScreenScaffold(
      scrollInfoProvider = FixedScrollInfoProvider,
      edgeButton = { ctx.ChildNode(edgeButton, 1) },
      content = body,
    )
  } else {
    ScreenScaffold(content = body)
  }
}

/**
 * More room than any watch screen has, in pixels: the button is capped at its own height. Keep
 * equal to the `lastItemOffset` that `WearHelpers.fixedScrollInfoProvider` (core/domain) exports.
 */
private const val ROOM_PX = 4096f

/**
 * The scroll facts of a body that does not scroll. Upstream's provider for a plain scroll state
 * reports no room below the last item, and the scaffold sizes the edge button to that room, so the
 * button would collapse. Here the room is unbounded, so the button takes its full height, and the
 * scaffold pads the body above it as it does at the end of a list.
 */
private object FixedScrollInfoProvider : ScrollInfoProvider {
  override val isScrollAwayValid = false
  override val isScrollable = false
  override val isScrollInProgress = false
  override val anchorItemOffset = 0f
  override val lastItemOffset = ROOM_PX
}

// region headers

internal fun NodeContext.listHeaderLook(ambient: WearAmbient) =
  WearLook(ambient, content = "onBackground", type = "titleMedium", text = string("text"))

@Composable
internal fun RenderWearListHeader(ctx: NodeContext) {
  ListHeader(
    modifier = ctx.modifier().then(ctx.itemModifier()),
    transformation = ctx.transformation(),
  ) {
    ctx.WearText(ctx.string("text").orEmpty(), "text")
  }
}

internal fun NodeContext.listSubHeaderLook(ambient: WearAmbient) =
  WearLook(ambient, content = "onSurface", type = "titleSmall", text = string("text"))

@Composable
internal fun RenderWearListSubHeader(ctx: NodeContext) {
  val icon = ctx.string("icon")
  ListSubHeader(
    modifier = ctx.modifier().then(ctx.itemModifier()),
    transformation = ctx.transformation(),
    icon =
      icon?.let { name ->
        { WearIcon(name, androidx.wear.compose.material3.ButtonDefaults.IconSize) }
      },
    label = { ctx.WearText(ctx.string("text").orEmpty(), "text") },
  )
}

// endregion
