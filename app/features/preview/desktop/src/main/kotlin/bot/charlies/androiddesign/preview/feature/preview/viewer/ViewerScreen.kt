package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.derivedMediaQuery
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.nav.ProposalPreview
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.theme.FontFace
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTheme
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import kotlin.math.min
import kotlinx.serialization.json.Json

/**
 * Draws the preview frame of [view] scaled by [scale] into the stage. The real window uses
 * [ConfinedFrame]; previews and tests draw it inline.
 */
typealias PreviewFrameSlot =
  @Composable (host: ProposalHostState, design: DesignSystem, view: ViewSpec, scale: Float) -> Unit

/** The viewer window: a side panel of choices next to the stage with the framed preview. */
@Composable
fun ViewerScreen(
  viewModel: ViewerViewModel,
  modifier: Modifier = Modifier,
  frame: PreviewFrameSlot = { host, design, view, scale ->
    ConfinedFrame(host, design, view, scale, onFailure = viewModel::frameFailed)
  },
) {
  val state by viewModel.state.collectAsState()
  ViewerContent(state, viewModel, frame, modifier)
}

/** Below this window width the side panel moves above the stage and reflows. */
private val NarrowWindow = 840.dp

private val PanelWidth = 320.dp

/**
 * The window width in dp: from `MediaQuery`, or from the window's size when a host does not report
 * it (the UI test engine leaves it unspecified).
 */
@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun windowWidth(): Dp {
  val reported by derivedMediaQuery { windowWidth }
  val container = LocalWindowInfo.current.containerSize.width
  val fallback = with(LocalDensity.current) { container.toDp() }
  return if (reported.isSpecified) reported else fallback
}

@Composable
fun ViewerContent(
  state: ViewerState,
  actions: ViewerActions,
  frame: PreviewFrameSlot,
  modifier: Modifier = Modifier,
) {
  val chrome =
    remember(state.effectiveDesign, state.seed, state.variant) {
      state.effectiveDesign ?: defaultDesign(state.seed, state.variant)
    }
  ProposalTheme(chrome, state.dark) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
      val narrow = windowWidth() < NarrowWindow
      if (narrow) {
        Column(Modifier.fillMaxSize()) {
          SidePanel(state, actions, narrow = true, Modifier.fillMaxWidth().heightIn(max = 280.dp))
          Stage(state, actions, frame, Modifier.weight(1f).fillMaxWidth())
        }
      } else {
        Row(Modifier.fillMaxSize()) {
          SidePanel(state, actions, narrow = false, Modifier.width(PanelWidth).fillMaxHeight())
          Stage(state, actions, frame, Modifier.weight(1f).fillMaxHeight())
        }
      }
    }
  }
}

// ---- stage ----

private val DeviceRowHeight = 56.dp
private val FooterHeight = 36.dp
private val StagePadding = 24.dp

private data class DeviceChoice(val device: Device, val icon: String, val label: String)

private val DeviceChoices =
  listOf(
    DeviceChoice(Device.Phone, "smartphone", "Phone"),
    DeviceChoice(Device.Foldable, "devices_fold", "Foldable"),
    DeviceChoice(Device.Tablet, "tablet", "Tablet"),
    DeviceChoice(Device.Desktop, "desktop_windows", "Desktop"),
  )

@Composable
private fun Stage(
  state: ViewerState,
  actions: ViewerActions,
  frame: PreviewFrameSlot,
  modifier: Modifier = Modifier,
) {
  val view = state.view
  Column(modifier.padding(StagePadding)) {
    DeviceRow(state, actions)
    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
      // A device switch morphs the frame to the new size, then the screen appears in it. The
      // screen sits in a window of its own (see ConfinedFrame), which cannot fade or resize with
      // this scene, so it is left out while the frame moves; each frame fades its screen in.
      val bezel = updateTransition(Bezel.of(view), label = "deviceFrame")
      val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
      val width by bezel.animateDp({ spec }, label = "frameWidth") { it.width }
      val height by bezel.animateDp({ spec }, label = "frameHeight") { it.height }
      val animatedRing by bezel.animateDp({ spec }, label = "frameRing") { it.ring }
      // The expressive spring overshoots, which would take a ring shrinking to 0 below it.
      val ring = animatedRing.coerceAtLeast(0.dp)
      val settled = bezel.currentState == bezel.targetState
      // The ring around a phone or foldable is drawn around the frame, so it counts in the fit.
      val scale =
        fitScale(
          availableWidth = maxWidth,
          availableHeight = maxHeight,
          frameWidth = width + ring * 2,
          frameHeight = height + ring * 2,
        )
      val host = state.host
      val design = state.effectiveDesign
      Column(horizontalAlignment = Alignment.Start) {
        Box(
          Modifier.testTag(DEVICE_FRAME_TAG)
            .size((width + ring * 2) * scale, (height + ring * 2) * scale)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .background(
              if (ring > 0.dp) MaterialTheme.colorScheme.surfaceContainerHighest
              else Color.Transparent,
            )
            .padding(ring * scale),
          contentAlignment = Alignment.Center,
        ) {
          when {
            state.errors.isNotEmpty() -> ErrorPanel(state.errors, state.hasProposal)
            !settled -> Unit
            host != null && design != null -> frame(host, design, view, scale)
            else -> EmptyPanel()
          }
        }
        Footer(state, view)
      }
    }
  }
}

/** The test tag of the device frame on the stage. */
internal const val DEVICE_FRAME_TAG = "deviceFrame"

/**
 * The frame's size in dp, unscaled, which is what a device switch animates. Brightness and color
 * keep it, so they redraw in place without a morph.
 */
private data class Bezel(val width: Dp, val height: Dp, val ring: Dp) {
  companion object {
    fun of(view: ViewSpec) = Bezel(view.width.dp, view.height.dp, ringFor(view.device))
  }
}

/**
 * Fades [content] in from [cover] when it enters the composition. A cover over the content fades
 * out, rather than the content fading in: an animated layer around the content would hold back a
 * sheet opened while it runs, and that sheet stays off screen. Frames call it inside their own
 * scene, since the window's frame cannot be faded from the stage.
 */
@Composable
internal fun FadeIn(cover: Color, content: @Composable () -> Unit) {
  val coverAlpha = remember { Animatable(1f) }
  val spec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
  LaunchedEffect(Unit) { coverAlpha.animateTo(0f, spec) }
  Box {
    content()
    Box(Modifier.matchParentSize().drawBehind { drawRect(cover, alpha = coverAlpha.value) })
  }
}

private fun ringFor(device: Device): Dp =
  if (device == Device.Phone || device == Device.Foldable) 10.dp else 0.dp

/** The largest scale at most 1 at which a frame fits the space. */
internal fun fitScale(
  availableWidth: Dp,
  availableHeight: Dp,
  frameWidth: Dp,
  frameHeight: Dp,
): Float {
  val vertical = (availableHeight - FooterHeight).value
  return min(1f, min(availableWidth.value / frameWidth.value, vertical / frameHeight.value))
    .coerceAtLeast(0.05f)
}

@Composable
private fun DeviceRow(state: ViewerState, actions: ViewerActions) {
  Row(
    Modifier.height(DeviceRowHeight).padding(bottom = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    DeviceChoices.forEach { choice ->
      val description = "${choice.label} ${deviceSize(choice.device)}"
      FilledTonalIconToggleButton(
        checked = state.device == choice.device,
        onCheckedChange = { actions.setDevice(choice.device) },
        modifier = Modifier.semantics { contentDescription = description },
        shapes = IconButtonDefaults.toggleableShapes(),
      ) {
        SymbolIcon(choice.icon, filled = state.device == choice.device)
      }
    }
  }
}

private fun deviceSize(device: Device): String =
  bot.charlies.androiddesign.preview.model.Views.of(device, dark = false).let {
    "${it.width}x${it.height}"
  }

@Composable
private fun Footer(state: ViewerState, view: ViewSpec) {
  val code = remember { ProposalFonts.family(FontFace.GoogleSansCode, 400) }
  Row(Modifier.height(FooterHeight).padding(top = 12.dp)) {
    Text(
      "${state.name ?: "Preview"} · ",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.titleSmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      "${view.width}×${view.height}",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style =
        MaterialTheme.typography.titleSmall.copy(fontFamily = code, fontWeight = FontWeight.Normal),
      maxLines = 1,
    )
  }
}

@Composable
private fun EmptyPanel() {
  Column(
    Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("No proposal open", style = MaterialTheme.typography.titleMedium)
    Text(
      "Ask the agent to call open_proposal.",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

/** Validation errors, shown in place of the frame. The last good render is kept behind them. */
@Composable
private fun ErrorPanel(errors: List<ValidationError>, hasLastGood: Boolean) {
  val colors = MaterialTheme.colorScheme
  val code = remember { ProposalFonts.family(FontFace.GoogleSansCode, 400) }
  Column(
    Modifier.fillMaxSize()
      .background(colors.errorContainer)
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      "${errors.size} validation ${if (errors.size == 1) "error" else "errors"}",
      color = colors.onErrorContainer,
      style = MaterialTheme.typography.titleMedium,
    )
    if (hasLastGood) {
      Text(
        "The last valid version is kept and returns when the file validates again.",
        color = colors.onErrorContainer,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    errors.forEach { error ->
      Column {
        Text(
          error.path,
          color = colors.onErrorContainer,
          style = MaterialTheme.typography.labelMedium.copy(fontFamily = code),
        )
        Text(
          error.message,
          color = colors.onErrorContainer,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

// ---- previews ----

private val SampleProposal by lazy {
  Json.decodeFromString(
    bot.charlies.androiddesign.preview.model.Proposal.serializer(),
    """{"format":1,"name":"Tonight's plate","navigation":{"start":"plates","panes":{"plates":"list"}},"screens":{
      "plates":{"catalogId":"https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1",
      "components":[
        {"id":"root","component":"Scaffold","topBar":"bar","content":"body"},
        {"id":"bar","component":"TopAppBar","title":"Tonight's plate"},
        {"id":"body","component":"Text","text":"Burrata and tomatoes","type":"headlineSmall","padding":"lg"}]},
      "plate_detail":{"catalogId":"https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1",
      "components":[{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Detail"}]}}}""",
  )
}

private fun sampleState(
  errors: List<ValidationError> = emptyList(),
  dark: Boolean = false,
  device: Device = Device.Phone,
): ViewerState {
  val design = DesignSystemResolver.resolve(null, null).copy(seed = "#B5523B")
  return ViewerState(
    name = "Tonight's plate",
    screens = SampleProposal.screens.keys.toList(),
    design = design,
    host = ProposalHostState(SampleProposal),
    errors = errors,
    dark = dark,
    device = device,
  )
}

/** Draws the frame inline, scaled, for previews and tests. */
internal val InlineFrame: PreviewFrameSlot = { host, design, view, scale ->
  Box(Modifier.size(view.width.dp * scale, view.height.dp * scale)) {
    androidx.compose.ui.layout.Layout(
      content = {
        FadeIn(MaterialTheme.colorScheme.background) { ProposalPreview(host, design, view) }
      },
      modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
      val placeable =
        measurables
          .single()
          .measure(
            androidx.compose.ui.unit.Constraints.fixed(
              view.width.dp.roundToPx(),
              view.height.dp.roundToPx(),
            ),
          )
      layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.placeWithLayer(0, 0) {
          scaleX = scale
          scaleY = scale
          transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
        }
      }
    }
  }
}

internal object NoActions : ViewerActions {
  override fun setDevice(device: Device) = Unit

  override fun setDark(dark: Boolean) = Unit

  override fun setSeed(seed: String?) = Unit

  override fun setVariant(variant: PaletteVariant?) = Unit

  override fun selectScreen(screen: String) = Unit
}

@Preview
@Composable
private fun ViewerPreview() {
  ViewerContent(sampleState(), NoActions, InlineFrame)
}

@Preview
@Composable
private fun ViewerDarkTabletPreview() {
  ViewerContent(sampleState(dark = true, device = Device.Tablet), NoActions, InlineFrame)
}

@Preview
@Composable
private fun ViewerErrorsPreview() {
  ViewerContent(
    sampleState(
      errors =
        listOf(
          ValidationError("/screens/plates/components/2/text", "must be a string or a binding"),
          ValidationError("/screens/plates/components/3", "references unknown component 'ghost'"),
        ),
    ),
    NoActions,
    InlineFrame,
  )
}
