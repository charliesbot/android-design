package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.feature.preview.render.AssetLoader
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

class AssetLoaderTest {
  private val pixel =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

  private val wider =
    "iVBORw0KGgoAAAANSUhEUgAAAAMAAAACCAYAAACddGYaAAAAEUlEQVR4nGP4z8DwH4YZkDkAm34L9XKwuTwAAAAASUVORK5CYII="

  @Test
  fun `an image rewritten at the same path loads again`() {
    val dir = Files.createTempDirectory("assets")
    val file = dir.resolve("dot.png")
    Files.write(file, Base64.getDecoder().decode(pixel))
    val loader = AssetLoader(dir)
    assertEquals(1, assertNotNull(loader.image("dot.png")).width)

    Files.write(file, Base64.getDecoder().decode(wider))
    assertEquals(3, assertNotNull(loader.image("dot.png")).width)
  }

  @Test
  fun `an image replaced by one of the same size is told apart by its time`() {
    val dir = Files.createTempDirectory("assets")
    val file = dir.resolve("dot.png")
    Files.write(file, Base64.getDecoder().decode(pixel))
    Files.setLastModifiedTime(file, FileTime.fromMillis(1_000_000))
    val loader = AssetLoader(dir)
    val first = assertNotNull(loader.image("dot.png"))

    Files.setLastModifiedTime(file, FileTime.fromMillis(2_000_000))
    assertNotSame(first, assertNotNull(loader.image("dot.png")))
  }

  @Test
  fun `an unchanged image is decoded once`() {
    val dir = Files.createTempDirectory("assets")
    Files.write(dir.resolve("dot.png"), Base64.getDecoder().decode(pixel))
    val loader = AssetLoader(dir)
    assertSame(loader.image("dot.png"), loader.image("dot.png"))
  }

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
