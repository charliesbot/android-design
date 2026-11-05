package bot.charlies.androiddesign.preview.app

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

  @Test
  fun `packaged Windows app starts detached through cmd start`() {
    val exe = "C:\\Users\\me\\AppData\\Local\\AndroidDesignPreview\\AndroidDesignPreview.exe"
    val command = InstanceLauncher.command(exe, "ignored", "ignored", windows = true)
    assertEquals(listOf("cmd.exe", "/c", "start", "", exe, "--instance"), command)
  }

  @Test
  fun `a Windows java binary is relaunched as javaw with an argument file`() {
    val command =
      InstanceLauncher.command(
        "C:\\jdk\\bin\\java.exe",
        "a.jar;b.jar",
        "pkg.MainKt",
        windows = true,
      ) {
        Path.of("C:\\tmp\\x.args")
      }
    assertEquals(
      listOf("cmd.exe", "/c", "start", "", "C:\\jdk\\bin\\javaw.exe", "@C:\\tmp\\x.args"),
      command,
    )
  }

  @Test
  fun `the argument file escapes backslashes inside the quoted classpath`() {
    val content = InstanceLauncher.argFileContent("C:\\a b\\x.jar;C:\\y.jar", "pkg.MainKt")
    assertEquals("-cp\n\"C:\\\\a b\\\\x.jar;C:\\\\y.jar\"\npkg.MainKt\n--instance", content)
  }

  @Test
  fun `the owner launch is launchd on macOS and Explorer on Windows`() {
    assertTrue(InstanceLauncher.launchedByUser(windows = false, parentPid = 1, null))
    assertFalse(InstanceLauncher.launchedByUser(windows = false, parentPid = 42, "/bin/zsh"))
    assertTrue(
      InstanceLauncher.launchedByUser(windows = true, parentPid = 42, "C:\\Windows\\explorer.exe"),
    )
    assertFalse(
      InstanceLauncher.launchedByUser(windows = true, parentPid = 42, "C:\\nodejs\\node.exe"),
    )
    assertFalse(InstanceLauncher.launchedByUser(windows = true, parentPid = 42, null))
    assertTrue(InstanceLauncher.launchedByUser(windows = true, parentPid = null, null))
  }

  @Test
  fun `the socket lives in Application Support on macOS and outside AppData on Windows`() {
    assertEquals(
      Path.of("/Users/me", "Library", "Application Support", "AndroidDesignPreview", "mcp.sock"),
      InstanceSocket.defaultPath("Mac OS X", "/Users/me"),
    )
    assertEquals(
      Path.of("C:\\Users\\me", ".android-design-preview", "mcp.sock"),
      InstanceSocket.defaultPath("Windows 11", "C:\\Users\\me"),
    )
  }
}
