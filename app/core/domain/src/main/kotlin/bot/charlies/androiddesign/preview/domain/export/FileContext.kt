package bot.charlies.androiddesign.preview.domain.export

import java.util.SortedMap
import java.util.SortedSet

/**
 * A `private fun name(param: String): Int` that turns a value bound to data into a drawable. Each
 * entry of [cases] maps one value the sample data gives to its `R.drawable` reference; [fallback]
 * is the `else` branch.
 */
internal class DrawableLookup(
  val name: String,
  private val param: String,
  private val fallback: String,
) {
  val cases: SortedMap<String, String> = sortedMapOf()

  fun decl(): Decl =
    RawDecl(
      buildList {
        add("private fun $name($param: String): Int =")
        add("  when ($param) {")
        cases.forEach { (value, drawable) -> add("    ${kotlinString(value)} -> $drawable") }
        add("    else -> $fallback")
        add("  }")
      },
    )
}

/** What one exported file collects while it is written: imports, opt-ins, drawables, fonts. */
internal class FileContext {
  private val imports: SortedSet<String> = sortedSetOf()
  private val optIns: SortedSet<String> = sortedSetOf()

  /** Material Symbol drawable names (`R.drawable.<name>`) the code refers to. */
  val drawables: SortedSet<String> = sortedSetOf()

  /** Image drawable names (`R.drawable.<name>`) for the bitmaps the code refers to. */
  val images: SortedSet<String> = sortedSetOf()

  /** Font resource names (`R.font.<name>`) the code refers to. */
  val fonts: SortedSet<String> = sortedSetOf()

  /** The drawable lookup functions the code calls, by name: only those it uses are written. */
  private val lookups: SortedMap<String, DrawableLookup> = sortedMapOf()

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

  fun image(name: String): String {
    images += name
    return "R.drawable.$name"
  }

  fun drawable(name: String): String {
    drawables += name
    return "R.drawable.$name"
  }

  /**
   * `symbolDrawable` (or `filledSymbolDrawable`), the function that maps each of [names] to its
   * Material Symbol drawable, and the name to call. Any other name gets the `help` drawable.
   */
  fun symbolLookup(names: Set<String>, filled: Boolean): String {
    val suffix = if (filled) "_filled" else ""
    val name = if (filled) "filledSymbolDrawable" else "symbolDrawable"
    val lookup = lookups.getOrPut(name) { DrawableLookup(name, "name", drawable("help$suffix")) }
    names.forEach { lookup.cases[it] = drawable(Naming.snake(it) + suffix) }
    return name
  }

  /**
   * `imageDrawable`, the function that maps each of [sources] (a source to its file name or node
   * id) to its image drawable, and the name to call. Any other source is an error: no image stands
   * in. With [asImages] the drawables are listed as images, not Material Symbols.
   */
  fun imageLookup(sources: Map<String, String>, asImages: Boolean = false): String {
    val name = "imageDrawable"
    val lookup =
      lookups.getOrPut(name) {
        DrawableLookup(name, "src", "error(\"No drawable for image ${'$'}src\")")
      }
    sources.forEach { (source, symbol) ->
      lookup.cases[source] =
        if (asImages) image(Naming.snake(symbol)) else drawable(Naming.snake(symbol))
    }
    return name
  }

  /** The lookup functions to write, in name order. */
  fun lookupDecls(): List<Decl> = lookups.values.map { it.decl() }

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
