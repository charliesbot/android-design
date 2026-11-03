package bot.charlies.androiddesign.preview.model

/**
 * The first line of what went wrong, which is enough to point at a failure in a report or on a
 * terminal. A missing or blank message reads as the exception's class name.
 */
fun Throwable.firstLine(): String =
  message?.lineSequence()?.firstOrNull()?.takeIf { it.isNotBlank() }
    ?: this::class.simpleName.orEmpty()

/**
 * The first line plus the top [frames] of the stack, by class and method, so a report names where
 * the failure was thrown: `NullPointerException at
 * LookaheadDelegate.owner(LookaheadDelegate.kt:629) < ...`. A failure with no stack reads as its
 * first line.
 */
fun Throwable.located(frames: Int = 6): String {
  val where = stackTrace.take(frames).joinToString(" < ") { it.short() }
  return if (where.isEmpty()) firstLine() else "${firstLine()} at $where"
}

private fun StackTraceElement.short(): String {
  val method = "${className.substringAfterLast('.')}.$methodName"
  return if (fileName == null || lineNumber < 0) method else "$method($fileName:$lineNumber)"
}
