package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.feature.preview.render.AssetLoader
import java.nio.file.Files
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AssetLoaderTest {
  private val pixel =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

  @Test
  fun `a data URI decodes`() {
    val image = AssetLoader(null).image("data:image/png;base64,$pixel")
    assertEquals(1, assertNotNull(image).width)
  }

  @Test
  fun `a relative path loads from the proposal folder`() {
    val dir = Files.createTempDirectory("assets")
    Files.createDirectories(dir.resolve("img"))
    Files.write(dir.resolve("img/dot.png"), Base64.getDecoder().decode(pixel))
    val loader = AssetLoader(dir)
    assertNotNull(loader.image("img/dot.png"))
    assertNull(loader.image("img/missing.png"))
  }

  @Test
  fun `an absolute path and a broken source give null`() {
    val dir = Files.createTempDirectory("assets")
    val file = Files.write(dir.resolve("dot.png"), Base64.getDecoder().decode(pixel))
    val loader = AssetLoader(dir)
    assertNull(loader.image(file.toAbsolutePath().toString()))
    assertNull(loader.image("data:image/png;base64,@@@"))
    assertNull(AssetLoader(null).image("dot.png"))
  }
}
