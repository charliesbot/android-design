package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.model.PreviewJson
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RenderCrashTest {
  private val plates = Path.of(System.getProperty("fixtures.dir")).resolve("plates.proposal.json")

  /** Draws like the real renderer, except that `plate_detail` in the dark view throws. */
  private val flaky = ScreenRenderer { source, screen, view, overlay ->
    if (screen == "plate_detail" && view.name == "dark") error("boom\nsecond line")
    HeadlessRenderer.image(source, screen, view, overlay = overlay)
  }

  @Test
  fun `a render that throws is recorded, the others are kept and the exit code is 1`() {
    val out = Files.createTempDirectory("crash")
    val err = ByteArrayOutputStream()
    val code =
      RenderCommand(flaky)
        .run(
          listOf(
            plates.toString(),
            "--out",
            out.toString(),
            "--views",
            "light,dark",
            "--screens",
            "plates,plate_detail",
          ),
          PrintStream(ByteArrayOutputStream()),
          PrintStream(err, true),
        )

    val report =
      PreviewJson.pretty.decodeFromString(
        RenderReport.serializer(),
        Files.readString(out.resolve("report.json")),
      )
    assertEquals(ExitCode.FINDINGS_FAILED, code)
    assertEquals(
      listOf(
        "plates/light",
        "plates/dark",
        "plates+new_sheet/light",
        "plates+new_sheet/dark",
        "plate_detail/light",
        "plate_detail/dark",
      ),
      report.renders.map { "${it.screen}/${it.view}" },
    )
    val broken = report.renders.single { !it.pass && it.failures.any { f -> "render-error" in f } }
    assertEquals("plate_detail/dark", "${broken.screen}/${broken.view}")
    assertEquals(listOf("render-error: boom"), broken.failures)
    assertNull(broken.png)
    assertNull(broken.tree)
    assertFalse(report.pass)
    for (entry in report.renders - broken) {
      assertTrue(Files.size(out.resolve(assertNotNull(entry.png))) > 1000, entry.toString())
    }
    assertTrue("plate_detail/dark: render-error: boom" in err.toString(), err.toString())
  }
}
