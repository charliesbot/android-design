package bot.charlies.androiddesign.preview.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/**
 * What the date and time pickers decide for the renderer, the exporter and the validator alike: the
 * variants, the dates and times a proposal writes, and the labels Material fixes. A picker reads
 * the clock for its "today" marker and for the month it opens on when nothing is selected, and a
 * preview must not change from one day to the next, so a proposal always names the date it opens on
 * and the previewer never draws the today marker. The exported picker marks today as Material does.
 */
object DatePickerSpec {
  enum class Variant(val wire: String) {
    Modal("modal"),
    ModalInput("modal-input"),
    Docked("docked"),
    FullScreen("full-screen"),
  }

  /**
   * What of its own chrome a picker draws, which is Compose's `title`, `headline` and
   * `showModeToggle`: a modal picker has all of it, a full-screen one has its header in the
   * window's app bar and keeps the headline, and a docked one is the calendar alone.
   */
  class Chrome(val title: Boolean, val headline: Boolean, val modeToggle: Boolean) {
    /** Whether the picker keeps all of Compose's own chrome, so no argument says otherwise. */
    val isDefault: Boolean
      get() = title && headline && modeToggle
  }

  fun chrome(variant: Variant): Chrome =
    when (variant) {
      Variant.Modal,
      Variant.ModalInput -> Chrome(title = true, headline = true, modeToggle = true)
      Variant.FullScreen -> Chrome(title = false, headline = true, modeToggle = false)
      Variant.Docked -> Chrome(title = false, headline = false, modeToggle = false)
    }

  /** The variant named [wire], else [Variant.Modal], the catalog default. */
  fun variant(wire: String?): Variant =
    Variant.entries.firstOrNull { it.wire == wire } ?: Variant.Modal

  /**
   * The years `DatePickerDefaults.YearRange` offers: a picker throws on a date outside them, so a
   * date outside them is not a date here.
   */
  val YEARS: IntRange = 1900..2100

  /** The month a picker with no usable date opens on, in place of the clock's. */
  val FALLBACK: LocalDate = LocalDate.of(2026, 1, 1)

  /** A docked picker's width: this port's choice, the width a modal picker has. */
  const val DOCKED_WIDTH_DP = 360f

  /** The text button that confirms a modal picker. */
  const val CONFIRM = "OK"

  /** The text button that dismisses a modal picker. */
  const val DISMISS = "Cancel"

  /** The text button that saves a full-screen picker, labeled by its outcome. */
  const val SAVE = "Save"

  /** The date [text] writes (`YYYY-MM-DD`, a real day within [YEARS]), or null. */
  fun date(text: String?): LocalDate? {
    if (text == null || !ISO.matches(text)) return null
    val date =
      try {
        LocalDate.parse(text)
      } catch (_: DateTimeParseException) {
        return null
      }
    return date.takeIf { it.year in YEARS }
  }

  /** The milliseconds `DatePickerState` takes for [date]: its midnight in UTC. */
  fun millis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

  /** The ISO text of the day a `DatePickerState` holds as [millis]. */
  fun iso(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

  private val ISO = Regex("""\d{4}-\d{2}-\d{2}""")
}

object TimePickerSpec {
  enum class Variant(val wire: String) {
    Dial("dial"),
    Input("input"),
  }

  /** The variant named [wire], else [Variant.Dial], the catalog default. */
  fun variant(wire: String?): Variant =
    Variant.entries.firstOrNull { it.wire == wire } ?: Variant.Dial

  /** The text button that confirms the picker. */
  const val CONFIRM = "OK"

  /** The text button that dismisses the picker. */
  const val DISMISS = "Cancel"

  /** The time [text] writes (`HH:mm`, 00:00 to 23:59), or null. */
  fun time(text: String?): LocalTime? {
    if (text == null || !HOUR_MINUTE.matches(text)) return null
    return try {
      LocalTime.parse(text)
    } catch (_: DateTimeParseException) {
      null
    }
  }

  /** The `HH:mm` text of [hour] and [minute]. */
  fun text(hour: Int, minute: Int): String = "%02d:%02d".format(java.util.Locale.ROOT, hour, minute)

  private val HOUR_MINUTE = Regex("""\d{2}:\d{2}""")
}

object DialogSpec {
  enum class Variant(val wire: String) {
    Basic("basic"),
    FullScreen("full-screen"),
  }

  /** The variant named [wire], else [Variant.Basic], the catalog default. */
  fun variant(wire: String?): Variant =
    Variant.entries.firstOrNull { it.wire == wire } ?: Variant.Basic
}

/** What the full-screen dialog and the full-screen date picker share: Material's header. */
object FullScreenSpec {
  /** The header's height, "Header height 56dp". */
  const val HEADER_HEIGHT_DP = 56f

  /** The padding at the sides of a full-screen dialog's body, "Top/left/right padding 24dp". */
  const val CONTENT_PADDING_DP = 24f
}
