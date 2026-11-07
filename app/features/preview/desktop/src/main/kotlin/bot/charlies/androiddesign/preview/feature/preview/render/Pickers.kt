package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import bot.charlies.androiddesign.preview.domain.DatePickerSpec
import bot.charlies.androiddesign.preview.domain.FullScreenSpec
import bot.charlies.androiddesign.preview.domain.TimePickerSpec
import bot.charlies.androiddesign.preview.model.ColorRole
import java.time.LocalTime
import java.util.Locale

private val PickerAmbient = Ambient(ColorRole.SurfaceContainerHigh, ColorRole.OnSurface)

internal fun NodeContext.datePickerLook() = Look(PickerAmbient)

internal fun NodeContext.timePickerLook() = Look(PickerAmbient)

/**
 * The picker's colors with the clock taken out. A date picker marks today with a ring and marks the
 * current year, and which day that is changes every day, so a preview that drew it would change
 * overnight. The previewer draws both as an ordinary day and year; the exported picker marks them
 * as Material does.
 */
@Composable
private fun previewColors(): DatePickerColors {
  val base = DatePickerDefaults.colors()
  return base.copy(
    todayContentColor = base.dayContentColor,
    todayDateBorderColor = Color.Transparent,
    currentYearContentColor = base.yearContentColor,
  )
}

private fun millis(text: String?): Long? = DatePickerSpec.date(text)?.let(DatePickerSpec::millis)

/** The month a picker opens on when its date is missing: a fixed one, never the clock's. */
private val fallbackMonth = DatePickerSpec.millis(DatePickerSpec.FALLBACK)

/**
 * The date picker: `DatePickerDialog` holding `DatePicker` or `DateRangePicker` for modal and modal
 * input, a `Popup` below its anchor for docked, and a full-window `Dialog` holding a `Scaffold`
 * with the close icon and Save for full-screen, which Compose has no composable for. The exporter
 * writes the same composition. The picker's dates start at the props and then live in the
 * interaction state, so a reopened picker keeps what was picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderDatePicker(ctx: NodeContext) {
  val variant = DatePickerSpec.variant(ctx.enum("variant"))
  val dismiss = { ctx.env.act(ActionCall.Dismiss) }
  val colors = previewColors()
  val mode =
    if (variant == DatePickerSpec.Variant.ModalInput) DisplayMode.Input else DisplayMode.Picker
  val picker: @Composable (Modifier) -> Unit =
    if (ctx.bool("range")) {
      val start = ctx.local("start", ctx.string("start").orEmpty())
      val end = ctx.local("end", ctx.string("end").orEmpty())
      val state = remember {
        DateRangePickerState(
          locale = Locale.US,
          initialSelectedStartDateMillis = millis(start.value),
          initialSelectedEndDateMillis = millis(end.value),
          initialDisplayedMonthMillis = millis(start.value) ?: millis(end.value) ?: fallbackMonth,
          initialDisplayMode = mode,
        )
      }
      LaunchedEffect(state) {
        snapshotFlow { state.selectedStartDateMillis to state.selectedEndDateMillis }
          .collect { (from, to) ->
            start.set(from?.let(DatePickerSpec::iso).orEmpty())
            end.set(to?.let(DatePickerSpec::iso).orEmpty())
          }
      }
      ({ modifier -> RangeBody(state, variant, modifier, colors) })
    } else {
      val selected = ctx.local("selected", ctx.string("selected").orEmpty())
      val state = remember {
        DatePickerState(
          locale = Locale.US,
          initialSelectedDateMillis = millis(selected.value),
          initialDisplayedMonthMillis = millis(selected.value) ?: fallbackMonth,
          initialDisplayMode = mode,
        )
      }
      LaunchedEffect(state) {
        snapshotFlow { state.selectedDateMillis }
          .collect { selected.set(it?.let(DatePickerSpec::iso).orEmpty()) }
      }
      ({ modifier -> SingleBody(state, variant, modifier, colors) })
    }
  when (variant) {
    DatePickerSpec.Variant.Modal,
    DatePickerSpec.Variant.ModalInput ->
      DatePickerDialog(
        onDismissRequest = dismiss,
        confirmButton = {
          TextButton(onClick = dismiss) { Text(DatePickerSpec.CONFIRM) }
        },
        modifier = ctx.modifier(),
        dismissButton = {
          TextButton(onClick = dismiss) { Text(DatePickerSpec.DISMISS) }
        },
        colors = colors,
      ) {
        picker(Modifier)
      }
    DatePickerSpec.Variant.Docked ->
      // The popup opens at the top start of its parent, an empty box at the field's bottom start.
      Popup(alignment = Alignment.TopStart, onDismissRequest = dismiss) {
        Surface(
          modifier = Modifier.width(DatePickerSpec.DOCKED_WIDTH_DP.dp).then(ctx.modifier()),
          shape = DatePickerDefaults.shape,
          color = colors.containerColor,
        ) {
          picker(Modifier)
        }
      }
    DatePickerSpec.Variant.FullScreen ->
      FullScreenWindow(
        ctx = ctx,
        title = {
          if (ctx.bool("range")) {
            DateRangePickerDefaults.DateRangePickerTitle(displayMode = DisplayMode.Picker)
          } else {
            DatePickerDefaults.DatePickerTitle(displayMode = DisplayMode.Picker)
          }
        },
        confirm = { TextButton(onClick = dismiss) { Text(DatePickerSpec.SAVE) } },
        content = { padding -> picker(Modifier.padding(padding)) },
      )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleBody(
  state: DatePickerState,
  variant: DatePickerSpec.Variant,
  modifier: Modifier,
  colors: DatePickerColors,
) {
  when (variant) {
    DatePickerSpec.Variant.Modal,
    DatePickerSpec.Variant.ModalInput ->
      DatePicker(state = state, modifier = modifier, colors = colors)
    DatePickerSpec.Variant.Docked ->
      DatePicker(
        state = state,
        modifier = modifier,
        colors = colors,
        title = null,
        headline = null,
        showModeToggle = false,
      )
    DatePickerSpec.Variant.FullScreen ->
      DatePicker(
        state = state,
        modifier = modifier,
        colors = colors,
        title = null,
        showModeToggle = false,
      )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeBody(
  state: DateRangePickerState,
  variant: DatePickerSpec.Variant,
  modifier: Modifier,
  colors: DatePickerColors,
) {
  when (variant) {
    DatePickerSpec.Variant.Modal,
    DatePickerSpec.Variant.ModalInput ->
      DateRangePicker(state = state, modifier = modifier, colors = colors)
    DatePickerSpec.Variant.Docked ->
      DateRangePicker(
        state = state,
        modifier = modifier,
        colors = colors,
        title = null,
        headline = null,
        showModeToggle = false,
      )
    DatePickerSpec.Variant.FullScreen ->
      DateRangePicker(
        state = state,
        modifier = modifier,
        colors = colors,
        title = null,
        showModeToggle = false,
      )
  }
}

/**
 * The window of a full-screen dialog, which Compose has no composable for: a `Dialog` the size of
 * the window holding a `Scaffold` whose `TopAppBar` has the close icon at the start, the [title],
 * and the [confirm] text button at the end. The container is surface container high and the header
 * is [FullScreenSpec.HEADER_HEIGHT_DP] tall. The date picker and the Dialog share it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullScreenWindow(
  ctx: NodeContext,
  title: @Composable () -> Unit,
  confirm: @Composable () -> Unit,
  content: @Composable (PaddingValues) -> Unit,
) {
  val container = ctx.env.color(PickerAmbient.container)
  val dismiss = { ctx.env.act(ActionCall.Dismiss) }
  Dialog(
    onDismissRequest = dismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    CompositionLocalProvider(LocalAmbient provides PickerAmbient) {
      Scaffold(
        modifier = Modifier.fillMaxSize().then(ctx.modifier()),
        topBar = {
          TopAppBar(
            title = title,
            navigationIcon = {
              IconButton(onClick = dismiss) { SymbolIcon("close", contentDescription = "Close") }
            },
            actions = { confirm() },
            expandedHeight = FullScreenSpec.HEADER_HEIGHT_DP.dp,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = container),
          )
        },
        containerColor = container,
        content = content,
      )
    }
  }
}

/**
 * The time picker: `TimePickerDialog` holding `TimePicker` (the dial) or `TimeInput`. The keyboard
 * icon switches between them, as Material asks of every mobile time picker, and the dialog's title
 * follows. The time starts at the `time` prop and then lives in the interaction state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderTimePicker(ctx: NodeContext) {
  val dismiss = { ctx.env.act(ActionCall.Dismiss) }
  val is24Hour = ctx.bool("is24Hour")
  val time = ctx.local("time", ctx.string("time").orEmpty())
  val input = ctx.local("mode", ctx.enum("variant"))
  val initial = TimePickerSpec.time(time.value) ?: LocalTime.MIDNIGHT
  val state =
    remember(is24Hour) {
      TimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = is24Hour,
      )
    }
  LaunchedEffect(state) {
    snapshotFlow { TimePickerSpec.text(state.hour, state.minute) }.collect { time.set(it) }
  }
  val displayMode =
    if (TimePickerSpec.variant(input.value) == TimePickerSpec.Variant.Input) {
      TimePickerDisplayMode.Input
    } else {
      TimePickerDisplayMode.Picker
    }
  TimePickerDialog(
    onDismissRequest = dismiss,
    confirmButton = { TextButton(onClick = dismiss) { Text(TimePickerSpec.CONFIRM) } },
    modifier = ctx.modifier(),
    dismissButton = { TextButton(onClick = dismiss) { Text(TimePickerSpec.DISMISS) } },
    title = { TimePickerDialogDefaults.Title(displayMode = displayMode) },
    modeToggleButton = {
      TimePickerDialogDefaults.DisplayModeToggle(
        onDisplayModeChange = {
          input.set(
            if (displayMode == TimePickerDisplayMode.Picker) TimePickerSpec.Variant.Input.wire
            else TimePickerSpec.Variant.Dial.wire,
          )
        },
        displayMode = displayMode,
      )
    },
  ) {
    if (displayMode == TimePickerDisplayMode.Picker) TimePicker(state) else TimeInput(state)
  }
}
