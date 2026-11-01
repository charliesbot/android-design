package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.model.TypeRole

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
  )
}

@Composable
internal fun RenderCheckbox(ctx: NodeContext) {
  val checked = ctx.checkedState()
  val label = ctx.string("label")
  val onChange: (Boolean) -> Unit = {
    checked.set(it)
    ctx.env.act(ctx.props.action("action"))
  }
  if (label == null) {
    Checkbox(checked.value, onChange, ctx.modifier().then(ctx.describedBy()), ctx.enabled)
  } else {
    Row(
      modifier =
        ctx
          .modifier()
          .toggleable(
            value = checked.value,
            enabled = ctx.enabled,
            role = Role.Checkbox,
            onValueChange = onChange,
          )
          .then(ctx.describedBy()),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked.value, null, enabled = ctx.enabled)
      Spacer(Modifier.width(4.dp))
      ctx.PartText(label, style = ctx.env.typeStyle(TypeRole.BodyLarge))
    }
  }
}

/** The `range` prop as a usable `min..max`: a reversed or empty one becomes 0 to 1. */
private fun NodeContext.sliderRange(): ClosedFloatingPointRange<Float> {
  val (min, max) = props.range("range") ?: return 0f..1f
  return if (min < max) min..max else 0f..1f
}

@Composable
internal fun RenderSlider(ctx: NodeContext) {
  val range = ctx.sliderRange()
  val value =
    ctx.local(
      "value",
      (ctx.props.float("value") ?: range.start).coerceIn(range.start, range.endInclusive),
    )
  Slider(
    // The kept value may predate an edit that narrowed the range.
    value = value.value.coerceIn(range.start, range.endInclusive),
    onValueChange = { value.set(it) },
    modifier = ctx.modifier(fillWidth = true).then(ctx.describedBy()),
    enabled = ctx.enabled,
    valueRange = range,
    steps = ctx.wholeOrDefault("steps"),
  )
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
    modifier = ctx.modifier(fillWidth = true).then(ctx.describedBy()),
    enabled = ctx.enabled,
    valueRange = range,
    steps = ctx.wholeOrDefault("steps"),
  )
}

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
  val modifier = ctx.modifier(fillWidth = true).then(ctx.describedBy())
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
