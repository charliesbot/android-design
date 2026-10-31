package bot.charlies.androiddesign.preview.domain.export

/** Identifier helpers for generated Kotlin. */
internal object Naming {
  private val KEYWORDS =
    setOf(
      "as",
      "break",
      "class",
      "continue",
      "do",
      "else",
      "false",
      "for",
      "fun",
      "if",
      "in",
      "interface",
      "is",
      "null",
      "object",
      "package",
      "return",
      "super",
      "this",
      "throw",
      "true",
      "try",
      "typealias",
      "typeof",
      "val",
      "var",
      "when",
      "while",
    )

  private fun words(text: String): List<String> {
    val cleaned = text.replace("'", "").replace("’", "")
    return cleaned
      .split(Regex("[^A-Za-z0-9]+"))
      .filter { it.isNotEmpty() }
      .flatMap { part ->
        Regex("[A-Z]?[a-z0-9]+|[A-Z]+(?![a-z])").findAll(part).map { it.value }.toList()
      }
  }

  /** `plate_detail` and `Tonight's plate` become `PlateDetail` and `TonightsPlate`. */
  fun pascal(text: String): String {
    val result =
      words(text).joinToString("") { it.lowercase().replaceFirstChar(Char::uppercaseChar) }
    return if (result.isEmpty() || result.first().isDigit()) "X$result" else result
  }

  /** `first_name` becomes `firstName`; a Kotlin keyword gets backticks. */
  fun camel(text: String): String {
    val pascal = pascal(text)
    val result = pascal.replaceFirstChar(Char::lowercaseChar)
    return if (result in KEYWORDS) "`$result`" else result
  }

  /** `Google Sans Flex` becomes `google_sans_flex`, an Android resource name. */
  fun snake(text: String): String {
    val result = words(text).joinToString("_") { it.lowercase() }
    return if (result.isEmpty() || result.first().isDigit()) "ic_$result" else result
  }

  /** `plates` becomes `plate`, `categories` becomes `category`; other words are kept. */
  fun singular(text: String): String {
    val word = text
    return when {
      word.length > 3 && word.endsWith("ies") -> word.dropLast(3) + "y"
      word.length > 3 && Regex("(ss|x|ch|sh)es$").containsMatchIn(word) -> word.dropLast(2)
      word.length > 2 && word.endsWith("s") && !word.endsWith("ss") -> word.dropLast(1)
      else -> word
    }
  }
}
