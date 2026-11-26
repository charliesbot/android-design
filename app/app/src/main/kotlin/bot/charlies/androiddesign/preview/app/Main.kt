package bot.charlies.androiddesign.preview.app

import bot.charlies.androiddesign.preview.app.cli.Cli
import kotlin.system.exitProcess

object AppInfo {
  const val VERSION = "0.7.0"
}

fun main(args: Array<String>) {
  System.setProperty("java.awt.headless", "true")
  exitProcess(Cli.run(args.toList(), System.out, System.err))
}
