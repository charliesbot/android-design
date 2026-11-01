package bot.charlies.androiddesign.preview.model

/**
 * The first line of what went wrong, which is enough to point at a failure in a report or on a
 * terminal. A missing or blank message reads as the exception's class name.
 */
fun Throwable.firstLine(): String =
  message?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() }
    ?: this::class.simpleName.orEmpty()
