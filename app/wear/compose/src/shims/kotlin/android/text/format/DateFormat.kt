package android.text.format

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object DateFormat {
  fun is24HourFormat(context: android.content.Context): Boolean = true

  fun getBestDateTimePattern(locale: Locale, skeleton: String): String =
    when (skeleton) {
      "Hm",
      "HHmm" -> "HH:mm"
      "hm" -> "h:mm a"
      else -> skeleton
    }

  fun format(pattern: CharSequence, time: Calendar): CharSequence =
    DateTimeFormatter.ofPattern(pattern.toString(), Locale.US)
      .format(time.toInstant().atZone(ZoneId.of("UTC")))
}
