package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.AppInfo
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

class CliTest {
  private class Run(val code: Int, val out: String, val err: String)

  private fun run(vararg args: String): Run {
    val out = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    val code = Cli.run(args.toList(), PrintStream(out, true), PrintStream(err, true))
    return Run(code, out.toString(), err.toString())
  }

  private fun readReport(dir: Path) =
    PreviewJson.pretty.decodeFromString(
      RenderReport.serializer(),
      Files.readString(dir.resolve("report.json")),
    )

  private fun phoneBoardSource(): String =
    """
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.compose.ui.unit.dp
    import bot.charlies.androiddesign.preview.kit.*

    val preview = designBoard("Plates", seed = 0xFFB5523B, variant = PaletteVariant.TonalSpot) {
      listDetail(
        id = "plates",
        detailId = "plate_detail",
        list = { onOpen ->
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Card(modifier = Modifier.padding(16.dp)) {
              Text("Pasta night", modifier = Modifier.padding(16.dp))
            }
            Button(
              onClick = onOpen,
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
            ) {
              Text("Open")
            }
          }
        },
        detail = {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(
              onClick = {},
              modifier = Modifier.testTag(AD_PRIMARY_TAG).height(56.dp),
            ) {
              Text("Cook")
            }
          }
        },
        placeholder = { Box(modifier = Modifier.fillMaxSize()) },
      )
      overlay("new_sheet", screen = "plates") {
        AlertDialog(
          onDismissRequest = {},
          confirmButton = { TextButton(onClick = {}) { Text("Add") } },
          text = { Text("New dish") },
        )
      }
    }
    """
      .trimIndent()

  private fun wearBoardSource(): String =
    """
    import androidx.compose.foundation.layout.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.testTag
    import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
    import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
    import androidx.wear.compose.material3.*
    import bot.charlies.androiddesign.preview.kit.*

    val preview = wearBoard("Watch") {
      screen("list") {
        val listState = rememberTransformingLazyColumnState()
        AppScaffold(timeText = { TimeText { timeTextCurvedText("10:08") } }) {
          ScreenScaffold(
            scrollState = listState,
            edgeButton = {
              EdgeButton(
                onClick = {},
                buttonSize = EdgeButtonSize.Medium,
                modifier = Modifier.testTag(AD_PRIMARY_TAG),
              ) {
                Text("Go")
              }
            },
          ) { contentPadding ->
            TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
              items(18) { i ->
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                  Text("Item ${'$'}i")
                }
              }
            }
          }
        }
      }
      screen("fixed") {
        AppScaffold(timeText = { TimeText { timeTextCurvedText("10:08") } }) {
          ScreenScaffold { contentPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(contentPadding))
          }
        }
      }
      overlay("sure", screen = "list") {
        AlertDialog(
          visible = true,
          onDismissRequest = {},
          confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = {}) },
          title = { Text("Confirm") },
          text = { Text("Start workout") },
        )
      }
    }
    """
      .trimIndent()

  @Test
  fun `render writes pngs, trees and a report for every screen, overlay and view`() {
    val dir = createTempDirectory("cli-phone-")
    try {
      val source =
        dir.resolve("plates.explore.kt").also { Files.writeString(it, phoneBoardSource()) }
      Files.writeString(
        dir.resolve("DESIGN.md"),
        "---\ncolors:\n  primary: \"#B5523B\"\nmaterial:\n  scheme: { variant: content }\n---\n",
      )
      val out = dir.resolve("out")
      val result = run("render", source.toString(), "--out", out.toString())
      val report = readReport(out)

      assertTrue(report.valid, report.errors.toString())
      assertEquals(15, report.renders.size, "two screens and one overlay by five views")
      assertEquals(
        setOf("plates", "plates+new_sheet", "plate_detail"),
        report.renders.map { it.screen }.toSet(),
      )
      assertEquals(Views.all.map { it.name }.toSet(), report.renders.map { it.view }.toSet())
      report.renders.forEach { entry ->
        val png = assertNotNull(entry.png)
        val tree = assertNotNull(entry.tree)
        assertTrue(Files.size(out.resolve(png)) > 1000, png)
        assertTrue(Files.readString(out.resolve(tree)).contains("\"nodes\""), tree)
        assertEquals("${entry.screen}-${entry.view}.png", entry.png)
        assertEquals("${entry.screen}-${entry.view}.tree.json", entry.tree)
      }
      assertEquals(source.toAbsolutePath().normalize().toString(), report.proposal)
      assertEquals(dir.resolve("DESIGN.md").toAbsolutePath().normalize().toString(), report.design)
      assertEquals("#B5523B", report.theme.seed)
      assertEquals("tonal-spot", report.theme.variant, "board theme wins over DESIGN.md")
      assertEquals(if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED, result.code)
      assertEquals(report.pass, report.renders.all { it.pass })
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `views and screens can be narrowed`() {
    val dir = createTempDirectory("cli-narrow-")
    try {
      val source =
        dir.resolve("plates.explore.kt").also { Files.writeString(it, phoneBoardSource()) }
      val out = dir.resolve("out")
      run(
        "render",
        source.toString(),
        "--out",
        out.toString(),
        "--views",
        "light,tablet",
        "--screens",
        "plate_detail",
      )
      val report = readReport(out)
      assertEquals(
        listOf("plate_detail/light", "plate_detail/tablet"),
        report.renders.map { "${it.screen}/${it.view}" },
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `an invalid exploration exits 2 and the report says why`() {
    val dir = createTempDirectory("cli-invalid-")
    try {
      val broken =
        dir.resolve("broken.explore.kt").also {
          Files.writeString(
            it,
            "import bot.charlies.androiddesign.preview.kit.*\nval x = Missing()",
          )
        }
      val out = dir.resolve("out")
      val result = run("render", broken.toString(), "--out", out.toString())

      assertEquals(ExitCode.INVALID, result.code)
      val report = readReport(out)
      assertFalse(report.valid)
      assertFalse(report.pass)
      assertTrue(report.renders.isEmpty())
      assertTrue(report.errors.isNotEmpty())
      assertTrue(result.err.isNotBlank())
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `a non-explore-kt file exits 2 with a report`() {
    val dir = createTempDirectory("cli-non-kt-")
    try {
      val other = dir.resolve("other.txt").also { Files.writeString(it, "{ not explore kt }") }
      val out = dir.resolve("out")
      val result = run("render", other.toString(), "--out", out.toString())
      assertEquals(ExitCode.INVALID, result.code)
      assertFalse(readReport(out).valid)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `a file that is not utf-8 exits 2 with a report instead of crashing`() {
    val dir = createTempDirectory("cli-utf8-")
    try {
      val broken = dir.resolve("bad.explore.kt")
      Files.write(broken, byteArrayOf('/'.code.toByte(), '*'.code.toByte(), 0xC3.toByte(), 0x28))
      val out = dir.resolve("out")

      val result = run("render", broken.toString(), "--out", out.toString())

      assertEquals(ExitCode.INVALID, result.code)
      val report = readReport(out)
      assertFalse(report.valid)
      assertTrue(report.errors.any { "UTF-8" in it.message }, report.errors.toString())
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `a DESIGN dot md error names the file in render`() {
    val dir = createTempDirectory("cli-bad-design-")
    try {
      val design = dir.resolve("BAD.md").also { Files.writeString(it, "---\ncolors: [oops\n---\n") }
      val source =
        dir.resolve("small.explore.kt").also { Files.writeString(it, phoneBoardSource()) }
      val out = dir.resolve("out")

      val result =
        run(
          "render",
          source.toString(),
          "--out",
          out.toString(),
          "--design",
          design.toString(),
        )

      assertEquals(ExitCode.INVALID, result.code)
      assertTrue(design.toString() in result.err, result.err)
      assertEquals(
        design.toAbsolutePath().normalize().toString(),
        readReport(out).errors.single().path,
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `render draws Wear overlays and scrolling Wear list ends`() {
    val dir = createTempDirectory("cli-wear-")
    try {
      val source = dir.resolve("watch.explore.kt").also { Files.writeString(it, wearBoardSource()) }
      val out = dir.resolve("out")
      run(
        "render",
        source.toString(),
        "--out",
        out.toString(),
        "--views",
        "watch",
      )
      val report = readReport(out)

      assertEquals(listOf("list", "list+sure", "fixed"), report.renders.map { it.screen })
      val list = report.renders.single { it.screen == "list" }
      assertEquals("list-watch-end.png", list.endPng)
      assertEquals("list-watch-end.tree.json", list.endTree)
      assertTrue(Files.size(out.resolve(assertNotNull(list.endPng))) > 1000)

      val sure = report.renders.single { it.screen == "list+sure" }
      assertEquals("list+sure-watch.png", sure.png)
      assertEquals("list+sure-watch.tree.json", sure.tree)
      assertTrue(Files.size(out.resolve(assertNotNull(sure.png))) > 1000)
      assertNull(sure.endPng, "nothing scrolls behind a dialog")

      val fixed = report.renders.single { it.screen == "fixed" }
      assertNull(fixed.endPng, "a non-scrolling screen has no end render")
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `an explicit design file replaces the lookup`() {
    val dir = createTempDirectory("cli-explicit-design-")
    try {
      val design =
        dir.resolve("OTHER.md").also {
          Files.writeString(it, "---\nversion: alpha\ncolors:\n  primary: \"#006A6A\"\n---\n")
        }
      val source =
        dir.resolve("plain.explore.kt").also {
          Files.writeString(
            it,
            """
            import androidx.compose.material3.Text
            import bot.charlies.androiddesign.preview.kit.*
            val preview = designBoard("Plain") {
              screen("home") { Text("Hi") }
            }
            """
              .trimIndent(),
          )
        }
      val out = dir.resolve("out")
      run(
        "render",
        source.toString(),
        "--out",
        out.toString(),
        "--design",
        design.toString(),
        "--views",
        "light",
      )
      val report = readReport(out)
      assertEquals(design.toAbsolutePath().normalize().toString(), report.design)
      assertEquals("#006A6A", report.theme.seed)

      Files.delete(design)
      val plain = dir.resolve("plain")
      run("render", source.toString(), "--out", plain.toString(), "--views", "light")
      assertEquals("#6750A4", readReport(plain).theme.seed)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `usage mistakes exit 64`() {
    val dir = createTempDirectory("cli-usage-")
    try {
      val source = dir.resolve("ok.explore.kt").also { Files.writeString(it, phoneBoardSource()) }
      assertEquals(ExitCode.USAGE, run().code)
      assertTrue("usage" in run().err)
      assertEquals(ExitCode.USAGE, run("nope").code)
      assertEquals(ExitCode.USAGE, run("render").code)
      assertEquals(ExitCode.USAGE, run("render", source.toString()).code)
      assertEquals(
        ExitCode.USAGE,
        run("render", source.toString(), "--out", dir.resolve("x").toString(), "--views", "huge")
          .code,
      )
      assertEquals(
        ExitCode.USAGE,
        run("render", "/no/such/file.explore.kt", "--out", "x").code,
      )
      assertEquals(
        ExitCode.USAGE,
        run("render", source.toString(), "--out", dir.resolve("y").toString(), "--screens", "nope")
          .code,
      )
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  @Test
  fun `version prints to stdout`() {
    val version = run("--version")
    assertEquals(ExitCode.OK, version.code)
    assertEquals(AppInfo.VERSION, version.out.trim())
  }
}
