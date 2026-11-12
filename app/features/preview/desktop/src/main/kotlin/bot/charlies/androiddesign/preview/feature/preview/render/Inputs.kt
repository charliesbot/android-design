package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.VerticalSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.RadioSpec
import bot.charlies.androiddesign.preview.domain.SliderSpec
import bot.charlies.androiddesign.preview.domain.SwitchSpec
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.TypeRole

internal fun NodeContext.switchLook() =
  if (bool("checked")) {
    Look(ColorRole.Primary, ColorRole.OnPrimary, shape = "full", a11yRole = "Switch")
  } else {
    Look(ColorRole.SurfaceContainerHighest, ColorRole.Outline, shape = "full", a11yRole = "Switch")
  }

/**
 * The thumb icon at the switch's state: a check when on, a close when off, each only as far as
 * `icons` says. Material: "icons that clearly communicate whether the switch is on or off, such as
 * an X and a checkmark".
 */
private fun NodeContext.switchThumbIcon(checked: Boolean): (@Composable () -> Unit)? {
  val name = SwitchSpec.iconAt(enum("icons"), checked) ?: return null
  return { SymbolIcon(name, size = SwitchDefaults.IconSize) }
}

@Composable
internal fun RenderSwitch(ctx: NodeContext) {
  val checked = ctx.checkedState()
  Switch(
    checked = checked.value,
    onCheckedChange = {
      checked.set(it)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier = ctx.modifier().then(ctx.describedBy()),
    enabled = ctx.enabled,
    thumbContent = ctx.switchThumbIcon(checked.value),
  )
}

internal fun NodeContext.checkboxLook(ambient: Ambient) =
  Look(
    ambient.container,
    colorRole() ?: ColorRole.OnSurface,
    TypeRole.BodyLarge,
    text = string("label"),
    a11yRole = "Checkbox",
  )

/** The box's tri-state: indeterminate (a dash) wins over `checked`; a tap on it checks the box. */
private fun checkboxState(indeterminate: Boolean, checked: Boolean) =
  when {
    indeterminate -> ToggleableState.Indeterminate
    checked -> ToggleableState.On
    else -> ToggleableState.Off
  }

@Composable
internal fun RenderCheckbox(ctx: NodeContext) {
  val checked = ctx.checkedState()
  val indeterminate = ctx.local("indeterminate", ctx.bool("indeterminate"))
  val label = ctx.string("label")
  val onChange: (Boolean) -> Unit = {
    checked.set(it)
    indeterminate.set(false)
    ctx.env.act(ctx.props.action("action"))
  }
  val state = checkboxState(indeterminate.value, checked.value)
  // A tap on an indeterminate box checks it, as it does a clear one.
  val next = state != ToggleableState.On
  if (label == null) {
    TriStateCheckbox(
      state,
      { onChange(next) },
      ctx.modifier().then(ctx.describedBy()),
      ctx.enabled,
    )
  } else {
    Row(
      modifier =
        ctx
          .modifier()
          .triStateToggleable(
            state = state,
            enabled = ctx.enabled,
            role = Role.Checkbox,
            onClick = { onChange(next) },
          )
          .then(ctx.describedBy()),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TriStateCheckbox(state, null, enabled = ctx.enabled)
      Spacer(Modifier.width(4.dp))
      ctx.PartText(label, style = ctx.env.typeStyle(TypeRole.BodyLarge))
    }
  }
}

internal fun NodeContext.radioGroupLook(ambient: Ambient) = Look(ambient)

/** The label is on surface and does not change with the state; the button is primary when on. */
internal fun NodeContext.radioButtonLook(ambient: Ambient) =
  Look(
    ambient.container,
    ColorRole.OnSurface,
    TypeRole.BodyLarge,
    text = string("label"),
    a11yRole = "RadioButton",
  )

@Composable
internal fun RenderRadioGroup(ctx: NodeContext) {
  Column(ctx.modifier().selectableGroup()) {
    ctx.Children(
      "children",
      0,
      ChildHost(fillWidth = ctx.stretchesChildren, column = this, groupKey = ctx.id),
    )
  }
}

@Composable
internal fun RenderRadioButton(ctx: NodeContext) {
  val group = ctx.host.groupKey
  val selected = if (group == null) ctx.bool("selected") else ctx.isPickedIn(group)
  val onClick = {
    group?.let(ctx::selectInGroup)
    ctx.env.act(ctx.props.action("action"))
  }
  Row(
    modifier =
      ctx
        .modifier()
        .heightIn(min = RadioSpec.MIN_HEIGHT_DP.dp)
        .selectable(
          selected = selected,
          enabled = ctx.enabled,
          role = Role.RadioButton,
          onClick = onClick,
        )
        .then(ctx.describedBy()),
    horizontalArrangement = Arrangement.spacedBy(RadioSpec.GAP_DP.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = selected, onClick = null, enabled = ctx.enabled)
    ctx.PartText(ctx.string("label").orEmpty(), style = ctx.env.typeStyle(TypeRole.BodyLarge))
  }
}

/** The `range` prop as a usable `min..max`: a reversed or empty one becomes 0 to 1. */
private fun NodeContext.sliderRange(): ClosedFloatingPointRange<Float> {
  val (min, max) = props.range("range") ?: return 0f..1f
  return if (min < max) min..max else 0f..1f
}

/** Slider and range slider share a look: the primary color on the ambient container. */
internal fun NodeContext.sliderLook(ambient: Ambient) = Look(ambient.container, ColorRole.Primary)

@Composable
internal fun RenderSlider(ctx: NodeContext) {
  val range = ctx.sliderRange()
  val value =
    ctx.local(
      "value",
      (ctx.props.float("value") ?: range.start).coerceIn(range.start, range.endInclusive),
    )
  // The kept value may predate an edit that narrowed the range.
  val current = value.value.coerceIn(range.start, range.endInclusive)
  val vertical = ctx.enum("orientation") == "vertical"
  val centered = ctx.enum("variant") == "centered"
  val modifier = ctx.modifier().then(ctx.describedBy())
  val steps = ctx.wholeOrDefault("steps")
  if (!vertical && !centered) {
    Slider(
      value = current,
      onValueChange = { value.set(it) },
      modifier = modifier,
      enabled = ctx.enabled,
      valueRange = range,
      steps = steps,
    )
    return
  }
  // The centered track and the vertical slider take a SliderState, which holds the value itself.
  val state = remember(range, steps) { SliderState(current, steps, range) }
  if (state.value != current) state.value = current
  val change: (Float) -> Unit = {
    state.value = it
    value.set(it)
  }
  val track: @Composable (SliderState) -> Unit = {
    if (centered) SliderDefaults.CenteredTrack(sliderState = it) else SliderDefaults.Track(it)
  }
  if (vertical) {
    VerticalSlider(
      state = state,
      onValueChange = change,
      modifier = modifier.height(SliderSpec.VERTICAL_HEIGHT_DP.dp),
      // Material: zero is at the bottom.
      topToBottom = false,
      enabled = ctx.enabled,
      track = track,
    )
  } else {
    Slider(
      state = state,
      onValueChange = change,
      modifier = modifier,
      enabled = ctx.enabled,
      track = track,
    )
  }
}

@Composable
internal fun RenderRangeSlider(ctx: NodeContext) {
  val range = ctx.sliderRange()
  val (start, end) = ctx.props.range("value") ?: (range.start to range.endInclusive)
  val value =
    ctx.local(
      "value",
      FloatSpan(
        start.coerceIn(range.start, range.endInclusive),
        end.coerceIn(range.start, range.endInclusive),
      ),
    )
  val kept = value.value
  val low = kept.start.coerceIn(range.start, range.endInclusive)
  val high = kept.end.coerceIn(low, range.endInclusive)
  RangeSlider(
    value = low..high,
    onValueChange = { value.set(FloatSpan(it.start, it.endInclusive)) },
    modifier = ctx.modifier().then(ctx.describedBy()),
    enabled = ctx.enabled,
    valueRange = range,
    steps = ctx.wholeOrDefault("steps"),
  )
}

internal fun NodeContext.textFieldLook(ambient: Ambient) =
  Look(
    if (enum("variant") == "filled") ColorRole.SurfaceContainerHighest else ambient.container,
    ColorRole.OnSurface,
    TypeRole.BodyLarge,
    "extraSmall",
    string("value")?.takeIf { it.isNotEmpty() } ?: string("label"),
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderTextField(ctx: NodeContext) {
  val text = ctx.local("value", ctx.string("value").orEmpty())
  val lines = ctx.wholeOrDefault("lines")
  val label = ctx.string("label")?.let { value -> @Composable { ctx.PartText(value, "label") } }
  val placeholder =
    ctx.string("placeholder")?.let { value -> @Composable { ctx.PartText(value, "placeholder") } }
  val supporting =
    ctx.string("supporting")?.let { value -> @Composable { ctx.PartText(value, "supporting") } }
  val leading = ctx.string("leadingIcon")?.let { name -> @Composable { SymbolIcon(name) } }
  val trailing = ctx.string("trailingIcon")?.let { name -> @Composable { SymbolIcon(name) } }
  val prefix = ctx.string("prefix")?.let { value -> @Composable { Text(value) } }
  val modifier = ctx.modifier().then(ctx.describedBy())
  if (ctx.enum("variant") == "outlined") {
    OutlinedTextField(
      value = text.value,
      onValueChange = { text.set(it) },
      modifier = modifier,
      enabled = ctx.enabled,
      label = label,
      placeholder = placeholder,
      leadingIcon = leading,
      trailingIcon = trailing,
      prefix = prefix,
      supportingText = supporting,
      isError = ctx.bool("error"),
      singleLine = lines <= 1,
      minLines = lines,
    )
  } else {
    TextField(
      value = text.value,
      onValueChange = { text.set(it) },
      modifier = modifier,
      enabled = ctx.enabled,
      label = label,
      placeholder = placeholder,
      leadingIcon = leading,
      trailingIcon = trailing,
      prefix = prefix,
      supportingText = supporting,
      isError = ctx.bool("error"),
      singleLine = lines <= 1,
      minLines = lines,
    )
  }
}
