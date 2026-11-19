package bot.charlies.androiddesign.preview.app.server

/** Loads the embedded browser board HTML template and substitutes the per-response CSP nonce. */
object BoardAssets {
  private val template: String by lazy {
    val stream =
      BoardAssets::class.java.getResourceAsStream("/board/index.html")
        ?: error("Missing embedded resource /board/index.html")
    stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
  }

  fun renderHtml(nonce: String): String = template.replace("{{NONCE}}", nonce)
}
