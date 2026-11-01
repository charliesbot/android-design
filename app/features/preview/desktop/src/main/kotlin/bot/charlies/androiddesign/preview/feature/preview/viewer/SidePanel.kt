package bot.charlies.androiddesign.preview.feature.preview.viewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.theme.ThemeResolver
import bot.charlies.androiddesign.preview.model.PaletteVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SidePanel(
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
  val variant = state.effectiveVariant
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
  val current = state.effectiveVariant
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
    remember(seed, variant, dark) { ThemeResolver.colorScheme(defaultDesign(seed, variant), dark) }
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
