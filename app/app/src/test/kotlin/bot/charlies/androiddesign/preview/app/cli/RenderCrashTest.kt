package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.InMemoryRenderWorker
import bot.charlies.androiddesign.preview.app.server.RenderWorker
import bot.charlies.androiddesign.preview.app.server.WorkerBatchResult
import bot.charlies.androiddesign.preview.app.server.WorkerFrame
import bot.charlies.androiddesign.preview.app.server.WorkerTargetOutput
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOverlayMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RenderCrashTest {
  private fun samplePngBytes(): ByteArray {
    val rgba =
      ByteArray(64 * 64 * 4) { idx ->
        if (idx % 4 == 3) 0xFF.toByte() else ((idx * 37) xor (idx shr 3)).toByte()
      }
    return FastPngEncoder.encodeFast(64, 64, rgba)
  }

  private fun flakyWorker(): RenderWorker {
    val png = samplePngBytes()
    val real =
      InMemoryRenderWorker(
        directions =
          listOf(
            DeviceDirectionMeta(
              name = "Crash",
              surface = "phone",
              startScreen = "home",
              screens = listOf("home", "detail"),
              overlays = listOf(DeviceOverlayMeta("info", "home")),
            ),
          ),
        views = PhoneSurface.views,
        targetBuilder = { target ->
          val view = Views.byName(target.viewName) ?: Views.Light
          WorkerTargetOutput(
            direction = target.direction,
            screen = target.screen,
            viewName = target.viewName,
            overlayId = target.overlayId,
            renderMs = 5L,
            tree =
              LayoutTree(
                screen = target.screen,
                view = view,
                nodes = emptyList(),
                findings = emptyList(),
              ),
            frame = WorkerFrame(bytes = png, mime = "image/png", isPng = true),
          )
        },
      )
    return object : RenderWorker by real {
      override fun renderTargets(
        entry: CompileEntry,
        targets: List<DeviceRenderTarget>,
        stageRoot: Path,
      ): WorkerBatchResult {
        if (targets.any { it.screen == "detail" && it.viewName == "dark" }) {
          error("boom\nsecond line")
        }
        return real.renderTargets(entry, targets, stageRoot)
      }

      override fun close() = real.close()
    }
  }

  @Test
  fun `a render that throws is recorded, the others are kept and the exit code is 1`() {
    val dir = createTempDirectory("render-crash-")
    try {
      val source =
        dir.resolve("crash.explore.kt").also {
          Files.writeString(
            it,
            """
            import androidx.compose.foundation.layout.*
            import androidx.compose.material3.*
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.platform.testTag
            import androidx.compose.ui.unit.dp
            import bot.charlies.androiddesign.preview.kit.*

            val preview = designBoard("Crash") {
              listDetail(
                id = "home",
                detailId = "detail",
                list = { onOpen ->
                  Box(Modifier.fillMaxSize()) {
                    Button(onClick = onOpen, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp)) {
                      Text("Open")
                    }
                  }
                },
                detail = {
                  Box(Modifier.fillMaxSize()) {
                    Button(onClick = {}, modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp)) {
                      Text("Save")
                    }
                  }
                },
                placeholder = { Box(Modifier.fillMaxSize()) },
              )
              overlay("info", screen = "home") {
                AlertDialog(
                  onDismissRequest = {},
                  confirmButton = { TextButton(onClick = {}) { Text("OK") } },
                  text = { Text("Info") },
                )
              }
            }
            """
              .trimIndent(),
          )
        }
      val out = dir.resolve("out")
      val err = ByteArrayOutputStream()
      val code =
        RenderCommand(::flakyWorker)
          .run(
            listOf(
              source.toString(),
              "--out",
              out.toString(),
              "--views",
              "light,dark",
              "--screens",
              "home,detail",
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
          "home/light",
          "home/dark",
          "home+info/light",
          "home+info/dark",
          "detail/light",
          "detail/dark",
        ),
        report.renders.map { "${it.screen}/${it.view}" },
      )
      val broken =
        report.renders.single { !it.pass && it.failures.any { f -> "render-error" in f } }
      assertEquals("detail/dark", "${broken.screen}/${broken.view}")
      assertEquals(listOf("render-error: boom"), broken.failures)
      assertNull(broken.png)
      assertNull(broken.tree)
      assertFalse(report.pass)
      for (entry in report.renders - broken) {
        assertTrue(Files.size(out.resolve(assertNotNull(entry.png))) > 1000, entry.toString())
      }
      assertTrue("detail/dark: render-error: boom" in err.toString(), err.toString())
    } finally {
      dir.toFile().deleteRecursively()
    }
  }
}
