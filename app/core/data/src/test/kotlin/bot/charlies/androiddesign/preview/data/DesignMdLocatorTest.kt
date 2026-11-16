package bot.charlies.androiddesign.preview.data

import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DesignMdLocatorTest {
  @Test
  fun `a DESIGN dot md next to the exploration wins`() {
    val root = createTempDirectory("design-locator")
    val nested = Files.createDirectories(root.resolve("design"))
    val source = nested.resolve("app.explore.kt")
    val top = Files.writeString(root.resolve("DESIGN.md"), "root")
    val near = Files.writeString(nested.resolve("DESIGN.md"), "near")
    assertEquals(near, DesignMdLocator.find(source))
    assertEquals(listOf(near, top), DesignMdLocator.candidates(source).take(2))
  }

  @Test
  fun `parent folders are searched nearest first`() {
    val root = createTempDirectory("design-locator")
    val designDir = Files.createDirectories(root.resolve("design"))
    val nested = Files.createDirectories(designDir.resolve("sub"))
    val source = nested.resolve("app.explore.kt")
    val top = Files.writeString(root.resolve("DESIGN.md"), "top")
    assertEquals(top, DesignMdLocator.find(source))
    val middle = Files.writeString(designDir.resolve("DESIGN.md"), "middle")
    assertEquals(middle, DesignMdLocator.find(source))
  }

  @Test
  fun `no DESIGN dot md means none`() {
    val root = createTempDirectory("design-locator")
    val source = Files.createDirectories(root.resolve("design")).resolve("app.explore.kt")
    assertNull(DesignMdLocator.find(source))
  }

  @Test
  fun `candidates run from the exploration folder upward`() {
    val root = createTempDirectory("design-locator")
    val nested = Files.createDirectories(root.resolve("design"))
    val source = nested.resolve("app.explore.kt")
    val candidates = DesignMdLocator.candidates(source)
    assertEquals(nested.resolve("DESIGN.md"), candidates.first())
    assertEquals(root.resolve("DESIGN.md"), candidates[1])
  }
}
