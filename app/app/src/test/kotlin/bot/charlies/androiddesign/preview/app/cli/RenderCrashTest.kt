package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.server.CompileEntry
import bot.charlies.androiddesign.preview.app.server.PreviewBackend
import bot.charlies.androiddesign.preview.app.server.RenderArtifact
import bot.charlies.androiddesign.preview.app.server.RobolectricPreviewBackend
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.ViewSpec
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
  private fun flakyBackend(): PreviewBackend {
    val bdir =
      Path.of(
        System.getProperty("adp.bootstrap.dir")
          ?: error("adp.bootstrap.dir system property missing"),
      )
    val meta = BootstrapMeta.fromFile(bdir.resolve("bootstrap-meta.properties"))
    val real =
      RobolectricPreviewBackend(
        meta = meta,
        robolectricCp = bdir.resolve("robolectric.cp"),
        bootstrapDir = bdir,
      )
    return object : PreviewBackend by real {
      override fun renderSingle(
        entry: CompileEntry,
        direction: String,
        screenId: String,
        prefix: String,
        overlayId: String?,
        view: ViewSpec,
        densityScale: Int,
        designSystem: DesignSystem,
        baseCheck: ViewCheck?,
        writeFiles: Boolean,
        rendersRoot: Path,
        frameFormat: FrameFormat,
        fontScale: Float,
        seedOverride: String?,
        variantOverride: String?,
        darkOverride: Boolean?,
      ): RenderArtifact {
        if (screenId == "detail" && view.name == "dark") error("boom\nsecond line")
        return real.renderSingle(
          entry = entry,
          direction = direction,
          screenId = screenId,
          prefix = prefix,
          overlayId = overlayId,
          view = view,
          densityScale = densityScale,
          designSystem = designSystem,
          baseCheck = baseCheck,
          writeFiles = writeFiles,
          rendersRoot = rendersRoot,
          frameFormat = frameFormat,
          fontScale = fontScale,
          seedOverride = seedOverride,
          variantOverride = variantOverride,
          darkOverride = darkOverride,
        )
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
        RenderCommand(::flakyBackend)
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
