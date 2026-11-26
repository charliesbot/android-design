package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.RenderEntry
import bot.charlies.androiddesign.preview.domain.CheckError
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.model.BoardSurface
import bot.charlies.androiddesign.preview.model.Views
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckImagePolicyTest {
  private fun artifact(
    viewName: String,
    pass: Boolean = true,
    overlay: String? = null,
    pngPath: Path? = null,
    pngBytes: ByteArray? = byteArrayOf(1, 2, 3),
  ): RenderArtifact {
    val view = Views.byName(viewName) ?: Views.Light
    val check =
      if (pass) ViewCheck(pass = true)
      else ViewCheck(pass = false, errors = listOf(CheckError("contrast")))
    return RenderArtifact(
      direction = "Main",
      screen = "home",
      overlay = overlay,
      view = view,
      densityScale = 2,
      artifactName = "home",
      pass = pass,
      renderMs = 1L,
      check = check,
      layout = null,
      tree = null,
      pngPath = pngPath,
      endPngPath = null,
      pngBytes = pngBytes,
      endPngBytes = null,
      pngSha256 = pngBytes?.let(ContentHasher::sha256Hex),
      entry = RenderEntry.of("home", viewName, null, check),
    )
  }

  @Test
  fun `primaryViews and initial frame gating match phone and wear surfaces`() {
    assertEquals(setOf("light", "desktop"), CheckImagePolicy.primaryViews(BoardSurface.Phone))
    assertEquals(setOf("watch", "watch-large"), CheckImagePolicy.primaryViews(BoardSurface.Wear))

    assertTrue(
      CheckImagePolicy.shouldRequestInitialFrame(
        scoped = false,
        surface = BoardSurface.Phone,
        overlayId = null,
        viewName = "light",
      ),
    )
    assertFalse(
      CheckImagePolicy.shouldRequestInitialFrame(
        scoped = false,
        surface = BoardSurface.Phone,
        overlayId = "dialog",
        viewName = "light",
      ),
    )
    assertTrue(
      CheckImagePolicy.shouldRequestInitialFrame(
        scoped = true,
        surface = BoardSurface.Wear,
        overlayId = "confirm",
        viewName = "watch",
      ),
    )
  }

  @Test
  fun `hasRequiredPngs checks disk presence per scope and pass status`() {
    val dir = createTempDirectory("img-policy-")
    try {
      val lightFile = Files.write(dir.resolve("light.png"), byteArrayOf(1))
      val desktopFile = Files.write(dir.resolve("desktop.png"), byteArrayOf(2))
      val watchFile = Files.write(dir.resolve("watch.png"), byteArrayOf(3))
      val missingFile = dir.resolve("missing.png")

      assertTrue(
        CheckImagePolicy.hasRequiredPngs(
          listOf(
            artifact("light", pass = true, pngPath = lightFile),
            artifact("dark", pass = true, pngPath = null),
            artifact("desktop", pass = true, pngPath = desktopFile),
          ),
          scoped = false,
          surface = BoardSurface.Phone,
        ),
      )
      assertFalse(
        CheckImagePolicy.hasRequiredPngs(
          listOf(artifact("light", pass = true, pngPath = missingFile)),
          scoped = false,
          surface = BoardSurface.Phone,
        ),
      )
      assertFalse(
        CheckImagePolicy.hasRequiredPngs(
          listOf(
            artifact("watch", pass = true, pngPath = watchFile),
            artifact("watch-large", pass = false, pngPath = null),
          ),
          scoped = false,
          surface = BoardSurface.Wear,
        ),
      )
      assertTrue(
        CheckImagePolicy.hasRequiredPngs(
          listOf(artifact("dark", pass = false, pngPath = lightFile)),
          scoped = true,
          surface = BoardSurface.Phone,
        ),
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }
}
