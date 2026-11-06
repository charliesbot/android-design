package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.LinearProgressIndicator
import androidx.wear.compose.material3.SegmentedCircularProgressIndicator
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SliderDefaults
import androidx.wear.compose.material3.Stepper
import bot.charlies.androiddesign.preview.feature.preview.render.ChildHost
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.describedBy
import bot.charlies.androiddesign.preview.feature.preview.render.local
import bot.charlies.androiddesign.preview.feature.preview.render.modifier
import bot.charlies.androiddesign.preview.model.RingInset

private val StepperAmbient = WearAmbient("background", "onSurface")

internal fun NodeContext.sliderLook() = WearLook("surfaceContainer", "primary", a11yRole = "Slider")

private fun NodeContext.valueRange(): ClosedFloatingPointRange<Float> {
  val (min, max) = props.range("range") ?: return 0f..1f
  return if (min < max) min..max else 0f..1f
}

/** The value the control starts at, in its range, and the position the viewer has moved it to. */
@Composable
private fun NodeContext.valueState(range: ClosedFloatingPointRange<Float>) =
  local("value", (props.float("value") ?: range.start).coerceIn(range.start, range.endInclusive))

@Composable
internal fun RenderWearSlider(ctx: NodeContext) {
  val range = ctx.valueRange()
  val value = ctx.valueState(range)
  Slider(
    // The kept value may predate an edit that narrowed the range.
    value = value.value.coerceIn(range.start, range.endInclusive),
    onValueChange = { value.set(it) },
    steps = ctx.wholeOrDefault("steps"),
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    enabled = ctx.enabled,
    valueRange = range,
  )
}

@Composable
internal fun RenderWearStepper(ctx: NodeContext) {
  val range = ctx.valueRange()
  val value = ctx.valueState(range)
  Stepper(
    value = value.value.coerceIn(range.start, range.endInclusive),
    onValueChange = { value.set(it) },
    steps = ctx.wholeOrDefault("steps"),
    decreaseIcon = { SliderDefaults.DecreaseIcon() },
    increaseIcon = { SliderDefaults.IncreaseIcon() },
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    enabled = ctx.enabled,
    valueRange = range,
  ) {
    // Wear's Stepper draws its content in onSurface, not in the screen's onBackground.
    CompositionLocalProvider(LocalWearAmbient provides StepperAmbient) {
      ctx.Child("content", 0, ChildHost.Wrap)
    }
  }
}

// region progress

internal fun NodeContext.progressLook(ambient: WearAmbient) =
  WearLook(ambient, content = "primary", a11yRole = "ProgressBar")

/**
 * A ring fills the display when its parent gives it the whole screen, and is a small spinner else.
 */
@Composable
private fun NodeContext.ringModifier(): Modifier =
  if (host.fillHeight) {
    modifier()
      .then(Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding))
  } else {
    modifier().then(Modifier.size(RING_FALLBACK_DP.dp))
  }

private const val RING_FALLBACK_DP = 48

/**
 * Tells the tree what a ring that fills the display takes at its edge: the padding it is drawn with
 * and the [stroke] it is passed, both Wear's own, so a finding reads the band the ring really
 * draws. A spinner has no band.
 */
@Composable
private fun NodeContext.RecordRingBand(stroke: Dp) {
  val recorder = env.recorder ?: return
  val tagId = id
  val band =
    if (host.fillHeight)
      RingInset(CircularProgressIndicatorDefaults.FullScreenPadding.value, stroke.value)
    else null
  SideEffect { recorder.reportRing(tagId, band) }
}

@Composable
internal fun RenderWearCircularProgress(ctx: NodeContext) {
  val progress = ctx.number("progress")
  if (progress == null) {
    val stroke = CircularProgressIndicatorDefaults.IndeterminateStrokeWidth
    ctx.RecordRingBand(stroke)
    CircularProgressIndicator(modifier = ctx.ringModifier(), strokeWidth = stroke)
  } else {
    val stroke = CircularProgressIndicatorDefaults.largeStrokeWidth
    ctx.RecordRingBand(stroke)
    CircularProgressIndicator(
      progress = { progress },
      modifier = ctx.ringModifier(),
      strokeWidth = stroke,
    )
  }
}

@Composable
internal fun RenderWearLinearProgress(ctx: NodeContext) {
  val progress = ctx.number("progress") ?: 0f
  LinearProgressIndicator(
    progress = { progress },
    modifier = ctx.modifier().then(ctx.itemModifier()),
  )
}

@Composable
internal fun RenderWearSegmentedProgress(ctx: NodeContext) {
  val stroke = CircularProgressIndicatorDefaults.largeStrokeWidth
  ctx.RecordRingBand(stroke)
  SegmentedCircularProgressIndicator(
    segmentCount = ctx.whole("segmentCount") ?: 1,
    progress = { ctx.number("progress") ?: 0f },
    modifier = ctx.ringModifier(),
    strokeWidth = stroke,
  )
}

// endregion
