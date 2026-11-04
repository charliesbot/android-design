package bot.charlies.androiddesign.preview.app

import kotlin.test.Test
import kotlin.test.assertEquals

class InstanceLauncherTest {
  @Test
  fun `packaged app starts through open`() {
    val command =
      InstanceLauncher.command(
        "/Applications/AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview",
        "ignored",
        "ignored",
      )
    assertEquals(
      listOf(
        "/usr/bin/open",
        "-a",
        "/Applications/AndroidDesignPreview.app",
        "--args",
        "--instance",
      ),
      command,
    )
  }

  @Test
  fun `a java binary is relaunched with the classpath and main class`() {
    val command = InstanceLauncher.command("/jdk/bin/java", "a.jar:b.jar", "pkg.MainKt")
    assertEquals(listOf("/jdk/bin/java", "-cp", "a.jar:b.jar", "pkg.MainKt", "--instance"), command)
  }
}
