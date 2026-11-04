package bot.charlies.androiddesign.preview.app

import bot.charlies.androiddesign.preview.app.cli.Cli
import kotlin.system.exitProcess

object AppInfo {
  const val VERSION = "0.1.0"
}

fun main(args: Array<String>) {
  // No arguments: an agent's relay, or the instance when the owner launched the app.
  if (args.isEmpty()) {
    runPreviewApp()
    return
  }
  // The detached instance an agent's relay spawned.
  if (args.toList() == listOf(InstanceLauncher.INSTANCE_ARG)) {
    runInstance(launchedByUser = false)
    return
  }
  // Commands draw without a window and must not need a display.
  System.setProperty("java.awt.headless", "true")
  exitProcess(Cli.run(args.toList(), System.out, System.err))
}
