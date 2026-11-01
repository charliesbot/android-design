package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.model.ValidationError

/** Collects validation errors; paths are JSON-pointer-like locations. */
internal class ErrorSink {
  private val collected = mutableListOf<ValidationError>()
  val errors: List<ValidationError>
    get() = collected

  fun add(path: String, message: String) {
    collected += ValidationError(path, message)
  }
}

/** Appends a JSON pointer segment, escaping `~` and `/`. */
internal fun String.child(segment: Any): String =
  "$this/" + segment.toString().replace("~", "~0").replace("/", "~1")
