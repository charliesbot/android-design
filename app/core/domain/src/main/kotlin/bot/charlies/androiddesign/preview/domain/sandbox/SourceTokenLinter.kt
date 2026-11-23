package bot.charlies.androiddesign.preview.domain.sandbox

import bot.charlies.androiddesign.preview.model.ValidationError

/**
 * Rejects design-system token bypasses in `.explore.kt` source before compilation: raw `Color(...)`
 * constructor calls outside the board declaration header, and `.copy(alpha = ...)` /
 * `Color.*.copy(...)` opacity hacks that break surface contrast checks.
 */
object SourceTokenLinter {
  private val BOARD_HEADER = Regex("""\b(?:designBoard|wearBoard)\s*\(""")
  private val RAW_COLOR_CALL = Regex("""\bColor\s*\(""")
  private val COPY_ALPHA_NAMED = Regex("""\.\s*copy\s*\([^)]*\balpha\s*=""")
  private val COPY_COLOR_NAMED_CONST = Regex("""\bColor\s*\.\s*[A-Za-z0-9_]+\s*\.\s*copy\s*\(""")
  private val RAW_FONT_FAMILY_CTOR = Regex("""\bFontFamily\s*\(""")
  private val GENERIC_FONT_FAMILY =
    Regex("""\bFontFamily\s*\.\s*(?:Serif|SansSerif|Monospace|Cursive|Default)\b""")

  fun lint(fileName: String, source: String): List<ValidationError> {
    val sanitized = blankCommentsAndStrings(source)
    val headerSpans = boardHeaderSpans(sanitized)
    val errors = mutableListOf<ValidationError>()

    for (match in RAW_COLOR_CALL.findAll(sanitized)) {
      val index = match.range.first
      if (headerSpans.any { index in it }) continue
      val (line, col) = lineAndColumn(source, index)
      errors +=
        ValidationError(
          path = "$fileName:$line:$col",
          message =
            "Raw Color(...) literal is not allowed in explorations; use MaterialTheme.colorScheme " +
              "roles or set seed in designBoard/wearBoard",
        )
    }

    val seenCopyStarts = HashSet<Int>()
    for (match in COPY_ALPHA_NAMED.findAll(sanitized) + COPY_COLOR_NAMED_CONST.findAll(sanitized)) {
      if (!seenCopyStarts.add(match.range.first)) continue
      val (line, col) = lineAndColumn(source, match.range.first)
      errors +=
        ValidationError(
          path = "$fileName:$line:$col",
          message =
            ".copy(alpha = ...) is not allowed in explorations; use tonal surface or " +
              "onSurfaceVariant color roles so contrast stays reliable",
        )
    }

    val seenFontStarts = HashSet<Int>()
    for (match in
      RAW_FONT_FAMILY_CTOR.findAll(sanitized) + GENERIC_FONT_FAMILY.findAll(sanitized)) {
      if (!seenFontStarts.add(match.range.first)) continue
      val (line, col) = lineAndColumn(source, match.range.first)
      errors +=
        ValidationError(
          path = "$fileName:$line:$col",
          message =
            "Unbundled FontFamily is not allowed in explorations; use MaterialTheme.typography, " +
              "brandFont = BundledFont.* in designBoard, or bundledFontFamily(...)",
        )
    }

    return errors.sortedBy { it.path }
  }

  private fun boardHeaderSpans(sanitized: String): List<IntRange> {
    val spans = mutableListOf<IntRange>()
    for (match in BOARD_HEADER.findAll(sanitized)) {
      val open = sanitized.indexOf('(', match.range.first)
      if (open < 0) continue
      var depth = 0
      var i = open
      while (i < sanitized.length) {
        when (sanitized[i]) {
          '(' -> depth++
          ')' -> {
            depth--
            if (depth == 0) {
              spans += open..i
              break
            }
          }
        }
        i++
      }
    }
    return spans
  }

  /** Replaces comments, strings, and char literals with spaces, preserving `\n` and indices. */
  fun blankCommentsAndStrings(source: String): String {
    val out = CharArray(source.length) { source[it] }
    var i = 0
    val n = source.length
    while (i < n) {
      val c = source[i]
      val next = if (i + 1 < n) source[i + 1] else '\u0000'
      when {
        c == '/' && next == '/' -> {
          while (i < n && source[i] != '\n') {
            out[i++] = ' '
          }
        }
        c == '/' && next == '*' -> {
          var depth = 1
          out[i++] = ' '
          out[i++] = ' '
          while (i < n && depth > 0) {
            if (source[i] == '/' && i + 1 < n && source[i + 1] == '*') {
              depth++
              out[i++] = ' '
              out[i++] = ' '
            } else if (source[i] == '*' && i + 1 < n && source[i + 1] == '/') {
              depth--
              out[i++] = ' '
              out[i++] = ' '
            } else {
              if (source[i] != '\n') out[i] = ' '
              i++
            }
          }
        }
        c == '"' && next == '"' && i + 2 < n && source[i + 2] == '"' -> {
          out[i++] = ' '
          out[i++] = ' '
          out[i++] = ' '
          while (i < n) {
            if (source[i] == '"' && i + 2 < n && source[i + 1] == '"' && source[i + 2] == '"') {
              out[i++] = ' '
              out[i++] = ' '
              out[i++] = ' '
              break
            }
            if (source[i] != '\n') out[i] = ' '
            i++
          }
        }
        c == '"' -> {
          out[i++] = ' '
          while (i < n) {
            val sc = source[i]
            if (sc == '\\' && i + 1 < n) {
              if (source[i] != '\n') out[i] = ' '
              i++
              if (i < n && source[i] != '\n') out[i] = ' '
              i++
            } else {
              if (sc != '\n') out[i] = ' '
              i++
              if (sc == '"' || sc == '\n') break
            }
          }
        }
        c == '\'' -> {
          out[i++] = ' '
          while (i < n) {
            val ch = source[i]
            if (ch == '\\' && i + 1 < n) {
              out[i++] = ' '
              if (i < n && source[i] != '\n') out[i] = ' '
              i++
            } else {
              if (ch != '\n') out[i] = ' '
              i++
              if (ch == '\'' || ch == '\n') break
            }
          }
        }
        else -> i++
      }
    }
    return String(out)
  }

  private fun lineAndColumn(source: String, offset: Int): Pair<Int, Int> {
    var line = 1
    var col = 1
    for (i in 0 until offset.coerceAtMost(source.length)) {
      if (source[i] == '\n') {
        line++
        col = 1
      } else {
        col++
      }
    }
    return line to col
  }
}
