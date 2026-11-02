package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.TypeRole

/** Whether this selectable chip is selected: on its own, or as one of a ChipGroup. */
@Composable
private fun NodeContext.chipSelected(): Boolean {
  val group = host.groupKey ?: return checkedState("selected").value
  return if (host.connected) {
    // `connected` marks a multi-select group: each chip keeps its own state.
    checkedState("selected").value
  } else {
    isPickedIn(group)
  }
}

private fun NodeContext.toggleChip() {
  val group = host.groupKey
  val interaction = env.host.interaction
  when {
    group == null || host.connected -> {
      val key = env.stateKey("$id:selected")
      val current = interaction.get<Boolean>(key) ?: bool("selected")
      interaction[key] = !current
    }
    else -> selectInGroup(group)
  }
  env.act(props.action("action"))
}

@Composable
private fun NodeContext.chipLabel(): @Composable () -> Unit = {
  PartText(string("label").orEmpty())
}

@Composable
private fun NodeContext.chipIcon(
  selected: Boolean = false,
  checkWhenSelected: Boolean = false,
): (@Composable () -> Unit)? {
  val name = if (checkWhenSelected && selected) "check" else string("icon")
  return name?.let { icon -> { IconSlot(icon, FilterChipDefaults.IconSize, filled = selected) } }
}

/** Assist and suggestion chips share a look: elevated or flat, never selected. */
internal fun NodeContext.actionChipLook(ambient: Ambient) =
  Look(
    if (enum("variant") == "elevated") ColorRole.SurfaceContainerLow else ambient.container,
    ColorRole.OnSurface,
    TypeRole.LabelLarge,
    "small",
    string("label"),
    "Button",
  )

@Composable
internal fun RenderAssistChip(ctx: NodeContext) {
  val onClick = { ctx.env.act(ctx.props.action("action")) }
  val modifier = ctx.modifier().then(ctx.describedBy())
  if (ctx.enum("variant") == "elevated") {
    ElevatedAssistChip(
      onClick,
      ctx.chipLabel(),
      modifier,
      ctx.enabled,
      leadingIcon = ctx.chipIcon(),
    )
  } else {
    AssistChip(onClick, ctx.chipLabel(), modifier, ctx.enabled, leadingIcon = ctx.chipIcon())
  }
}

@Composable
internal fun RenderSuggestionChip(ctx: NodeContext) {
  val onClick = { ctx.env.act(ctx.props.action("action")) }
  val modifier = ctx.modifier().then(ctx.describedBy())
  if (ctx.enum("variant") == "elevated") {
    ElevatedSuggestionChip(onClick, ctx.chipLabel(), modifier, ctx.enabled, icon = ctx.chipIcon())
  } else {
    SuggestionChip(onClick, ctx.chipLabel(), modifier, ctx.enabled, icon = ctx.chipIcon())
  }
}

@Composable
internal fun RenderFilterChip(ctx: NodeContext) {
  val selected = ctx.chipSelected()
  val modifier = ctx.modifier().then(ctx.describedBy())
  val leading = ctx.chipIcon(selected, checkWhenSelected = true)
  if (ctx.enum("variant") == "elevated") {
    ElevatedFilterChip(
      selected,
      { ctx.toggleChip() },
      ctx.chipLabel(),
      modifier,
      ctx.enabled,
      leadingIcon = leading,
    )
  } else {
    FilterChip(
      selected,
      { ctx.toggleChip() },
      ctx.chipLabel(),
      modifier,
      ctx.enabled,
      leadingIcon = leading,
    )
  }
}

/** Filter and input chips share a look; selected ones are secondary container. */
internal fun NodeContext.selectableChipLook(ambient: Ambient): Look {
  val selected = bool("selected")
  val container =
    when {
      selected -> ColorRole.SecondaryContainer
      // InputChip has no variant prop, so this is read without a catalog default.
      string("variant") == "elevated" -> ColorRole.SurfaceContainerLow
      else -> ambient.container
    }
  return Look(
    container,
    if (selected) ColorRole.OnSecondaryContainer else ColorRole.OnSurfaceVariant,
    TypeRole.LabelLarge,
    "small",
    string("label"),
    "Checkbox",
  )
}

@Composable
internal fun RenderInputChip(ctx: NodeContext) {
  val selected = ctx.chipSelected()
  InputChip(
    selected = selected,
    onClick = { ctx.toggleChip() },
    label = ctx.chipLabel(),
    modifier = ctx.modifier().then(ctx.describedBy()),
    enabled = ctx.enabled,
    leadingIcon = ctx.chipIcon(selected),
  )
}

internal fun NodeContext.chipGroupLook(ambient: Ambient) = Look(ambient)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderChipGroup(ctx: NodeContext) {
  FlowRow(
    modifier = ctx.modifier(),
    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
  ) {
    ctx.Children(
      "children",
      0,
      ChildHost(
        fillWidth = ctx.def.stretchesChildren,
        groupKey = ctx.id,
        connected = ctx.bool("multi"),
      ),
    )
  }
}
