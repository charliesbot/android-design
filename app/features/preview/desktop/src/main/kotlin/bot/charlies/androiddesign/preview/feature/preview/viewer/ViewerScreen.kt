package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.derivedMediaQuery
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
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
      state.effectiveDesign
        ?: DesignSystemResolver.resolve(null, null).let { base ->
          base.copy(seed = state.seed ?: base.seed, variant = state.variant ?: base.variant)
        }
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

// ---- side panel ----

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SidePanel(
  state: ViewerState,
  actions: ViewerActions,
  narrow: Boolean,
  modifier: Modifier = Modifier,
) {
  val sections: @Composable (Modifier) -> Unit = { sectionModifier ->
    BrightnessSection(state, actions, sectionModifier)
    ColorSection(state, actions, sectionModifier)
    StyleSection(state, actions, sectionModifier)
    ScreensSection(state, actions, sectionModifier)
  }
  Surface(modifier, color = MaterialTheme.colorScheme.surfaceContainer) {
    if (narrow) {
      // A narrow window lays the sections out side by side and wraps them.
      FlexBox(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        config =
          FlexBoxConfig {
            wrap(FlexWrap.Wrap)
            gap(24.dp)
          },
      ) {
        sections(Modifier.width(280.dp))
      }
    } else {
      Column(
        Modifier.verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        sections(Modifier.fillMaxWidth())
      }
    }
  }
}

@Composable
private fun PanelSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      title,
      Modifier.padding(horizontal = 8.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.titleSmall,
    )
    content()
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BrightnessSection(state: ViewerState, actions: ViewerActions, modifier: Modifier) {
  PanelSection("Brightness", modifier) {
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
      for ((dark, label, icon) in
        listOf(Triple(false, "Light", "light_mode"), Triple(true, "Dark", "dark_mode"))) {
        FilledTonalToggleButton(
          checked = state.dark == dark,
          onCheckedChange = { actions.setDark(dark) },
          modifier = Modifier.weight(1f),
          shapes =
            if (!dark) ButtonGroupDefaults.connectedLeadingButtonShapes()
            else ButtonGroupDefaults.connectedTrailingButtonShapes(),
        ) {
          SymbolIcon(icon, size = ButtonDefaults.SmallIconSize, filled = state.dark == dark)
          Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
          Text(label)
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSection(state: ViewerState, actions: ViewerActions, modifier: Modifier) {
  val variant = state.variant ?: state.design?.variant ?: PaletteVariant.TonalSpot
  val proposalSeed = state.design?.seed
  PanelSection("Color", modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (proposalSeed != null) {
        SwatchRow(
          label = "Proposal",
          seed = proposalSeed,
          variant = variant,
          dark = state.dark,
          selected = state.seed == null,
          index = 0,
          count = 1,
          onClick = { actions.setSeed(null) },
        )
      }
      val presets = ViewerPresets.seeds
      // The presets sit in a one-column grid, so a segmented list shares one row height.
      Grid(
        config = {
          column(1.fr)
          gap(2.dp)
        },
        modifier = Modifier.fillMaxWidth(),
      ) {
        presets.forEachIndexed { index, preset ->
          SwatchRow(
            label = preset.name,
            seed = preset.seed,
            variant = variant,
            dark = state.dark,
            selected = state.seed.equals(preset.seed, ignoreCase = true),
            index = index,
            count = presets.size,
            onClick = { actions.setSeed(preset.seed) },
          )
        }
      }
    }
  }
}

@Composable
private fun StyleSection(state: ViewerState, actions: ViewerActions, modifier: Modifier) {
  val seed = state.seed ?: state.design?.seed ?: DesignSystemResolver.DEFAULT_SEED
  val current = state.variant ?: state.design?.variant ?: PaletteVariant.TonalSpot
  PanelSection("Style", modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      ViewerPresets.variants.forEachIndexed { index, variant ->
        SwatchRow(
          label = ViewerPresets.label(variant),
          seed = seed,
          variant = variant,
          dark = state.dark,
          selected = current == variant,
          index = index,
          count = ViewerPresets.variants.size,
          onClick = { actions.setVariant(variant) },
        )
      }
    }
  }
}

@Composable
private fun ScreensSection(state: ViewerState, actions: ViewerActions, modifier: Modifier) {
  if (state.screens.isEmpty()) return
  val current = state.host?.current
  PanelSection("Screens", modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      state.screens.forEachIndexed { index, screen ->
        ChoiceRow(
          selected = screen == current,
          index = index,
          count = state.screens.size,
          onClick = { actions.selectScreen(screen) },
        ) {
          Text(
            screen,
            Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
    }
  }
}

/** The corners of a row in a segmented group: round at the ends, tight between rows. */
private fun segmentShape(index: Int, count: Int) =
  RoundedCornerShape(
    topStart = if (index == 0) 20.dp else 4.dp,
    topEnd = if (index == 0) 20.dp else 4.dp,
    bottomStart = if (index == count - 1) 20.dp else 4.dp,
    bottomEnd = if (index == count - 1) 20.dp else 4.dp,
  )

@Composable
private fun ChoiceRow(
  selected: Boolean,
  index: Int,
  count: Int,
  onClick: () -> Unit,
  content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
  val colors = MaterialTheme.colorScheme
  Surface(
    selected = selected,
    onClick = onClick,
    shape = segmentShape(index, count),
    color = colors.surfaceContainerHigh,
    contentColor = colors.onSurface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      content = content,
    )
  }
}

/**
 * A row that previews a color: a swatch, the name and five role dots, all in the theme the row
 * stands for (its seed and variant, in the current brightness).
 */
@Composable
private fun SwatchRow(
  label: String,
  seed: String,
  variant: PaletteVariant,
  dark: Boolean,
  selected: Boolean,
  index: Int,
  count: Int,
  onClick: () -> Unit,
) {
  val scheme =
    remember(seed, variant, dark) {
      ThemeResolver.colorScheme(
        DesignSystemResolver.resolve(null, null).copy(seed = seed, variant = variant),
        dark,
      )
    }
  val ring = MaterialTheme.colorScheme.outlineVariant
  ChoiceRow(selected, index, count, onClick) {
    Box(
      Modifier.size(32.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(
            0f to scheme.primary,
            0.5f to scheme.primary,
            0.5f to scheme.primaryContainer,
            1f to scheme.primaryContainer,
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
          ),
        ),
    )
    Text(
      label,
      Modifier.weight(1f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyLarge,
    )
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
      listOf(
          scheme.primary,
          scheme.primaryContainer,
          scheme.secondaryContainer,
          scheme.tertiaryContainer,
          scheme.surfaceContainerHighest,
        )
        .forEach { color ->
          Box(
            Modifier.size(20.dp)
              .clip(CircleShape)
              .background(color)
              .border(BorderStroke(1.dp, ring), CircleShape),
          )
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
      // The ring around a phone or foldable is drawn around the frame, so it counts in the fit.
      val ring = ringFor(view.device)
      val scale =
        fitScale(
          availableWidth = maxWidth,
          availableHeight = maxHeight,
          frameWidth = view.width.dp + ring * 2,
          frameHeight = view.height.dp + ring * 2,
        )
      val host = state.host
      val design = state.effectiveDesign
      Column(horizontalAlignment = Alignment.Start) {
        Box(
          Modifier.size((view.width.dp + ring * 2) * scale, (view.height.dp + ring * 2) * scale)
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
            host != null && design != null -> frame(host, design, view, scale)
            else -> EmptyPanel()
          }
        }
        Footer(state, view)
      }
    }
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
      content = { ProposalPreview(host, design, view) },
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
