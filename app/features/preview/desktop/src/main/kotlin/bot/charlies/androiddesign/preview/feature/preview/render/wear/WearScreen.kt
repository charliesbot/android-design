package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
    { _ ->
      // Full bleed, as in the official samples: a ring or a stepper reaches the display's edge.
      Box(Modifier.fillMaxSize()) {
        ctx.Children("children", 0, ChildHost.Fill)
      }
    }
  if (edgeButton != null) {
    // The body does not scroll, so the edge button always shows in full.
    ScreenScaffold(
      scrollInfoProvider = ScrollInfoProvider(rememberScrollState()),
      edgeButton = { ctx.ChildNode(edgeButton, 1) },
      content = body,
    )
  } else {
    ScreenScaffold(content = body)
  }
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
