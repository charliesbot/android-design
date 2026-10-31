package bot.charlies.androiddesign.preview.domain.export

import java.util.SortedSet

/** What one exported file collects while it is written: imports, opt-ins, drawables, fonts. */
internal class FileContext {
  private val imports: SortedSet<String> = sortedSetOf()
  private val optIns: SortedSet<String> = sortedSetOf()

  /** Material Symbol drawable names (`R.drawable.<name>`) the code refers to. */
  val drawables: SortedSet<String> = sortedSetOf()

  /** Font resource names (`R.font.<name>`) the code refers to. */
  val fonts: SortedSet<String> = sortedSetOf()

  /** Simple names of everything imported so far. */
  fun importedNames(): Set<String> = imports.mapTo(mutableSetOf()) { it.substringAfterLast('.') }

  /** Records the import for [fqn] and returns the name to write. */
  fun use(fqn: String): String {
    imports += fqn
    return fqn.substringAfterLast('.')
  }

  /** Records [fqn] as an opt-in marker, imports it, and returns its simple name. */
  fun optIn(fqn: String): String {
    val name = use(fqn)
    optIns += name
    return name
  }

  fun drawable(name: String): String {
    drawables += name
    return "R.drawable.$name"
  }

  fun font(name: String): String {
    fonts += name
    return "R.font.$name"
  }

  /**
   * The import lines for names that [code] uses. A name that appears nowhere in the code is left
   * out, as ktfmt would remove it. Property delegates use `getValue` and `setValue` without naming
   * them, so those stay whenever the code has a `by`.
   */
  fun importLines(code: String): List<String> =
    imports
      .filter { fqn ->
        val name = fqn.substringAfterLast('.')
        val delegate = name == "getValue" || name == "setValue"
        (delegate && code.contains(" by ")) ||
          Regex("(?<![A-Za-z0-9_])${Regex.escape(name)}(?![A-Za-z0-9_])").containsMatchIn(code)
      }
      .map { "import $it" }

  /** `@file:OptIn(...)` laid out like ktfmt, or null when nothing needs one. */
  fun optInAnnotation(): String? =
    if (optIns.isEmpty()) null
    else
      CodePrinter.printStatements(
          listOf(stmt(Call("@file:OptIn", optIns.map { pos("$it::class") }))),
        )
        .joinToString("\n")
}
