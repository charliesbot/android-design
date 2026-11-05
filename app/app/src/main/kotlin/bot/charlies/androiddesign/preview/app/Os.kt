package bot.charlies.androiddesign.preview.app

/** The operating system this process runs on, for the few places the app behaves differently. */
object Os {
  private val name: String = System.getProperty("os.name")

  val isWindows: Boolean = isWindows(name)
  val isMac: Boolean = name.startsWith("Mac")

  fun isWindows(osName: String): Boolean = osName.startsWith("Windows")
}
