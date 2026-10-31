package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.app.AppInfo
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Views
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CliTest {
  private class Run(val code: Int, val out: String, val err: String)

  private fun run(vararg args: String): Run {
    val out = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    val code = Cli.run(args.toList(), PrintStream(out, true), PrintStream(err, true))
    return Run(code, out.toString(), err.toString())
  }

  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val plates = fixtures.resolve("plates.proposal.json")

  private fun readReport(dir: Path) =
    PreviewJson.pretty.decodeFromString(
      RenderReport.serializer(),
      Files.readString(dir.resolve("report.json")),
    )

  @Test
  fun `render writes pngs, trees and a report for every screen and view`() {
    val out = Files.createTempDirectory("render")
    val run = run("render", plates.toString(), "--out", out.toString())
    val report = readReport(out)

    assertTrue(report.valid, report.errors.toString())
    assertEquals(10, report.renders.size, "two screens by five views")
    assertEquals(
      setOf("plates", "plate_detail"),
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
    assertEquals(plates.toAbsolutePath().normalize().toString(), report.proposal)
    assertEquals(
      fixtures.resolve("DESIGN.md").toAbsolutePath().normalize().toString(),
      report.design,
    )
    assertEquals("#B5523B", report.theme.seed)
    assertEquals("expressive", report.theme.variant, "the proposal theme wins over DESIGN.md")
    assertEquals(0.0, report.theme.contrast)
    assertEquals(if (report.pass) ExitCode.OK else ExitCode.FINDINGS_FAILED, run.code)
    assertEquals(report.pass, report.renders.all { it.pass })
  }

  @Test
  fun `views and screens can be narrowed`() {
    val out = Files.createTempDirectory("render")
    run(
      "render",
      plates.toString(),
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
  }

  @Test
  fun `an invalid proposal exits 2 and the report says why`() {
    val dir = Files.createTempDirectory("invalid")
    val broken = dir.resolve("broken.proposal.json")
    Files.writeString(
      broken,
      Files.readString(plates)
        .replace("\"component\": \"Scaffold\"", "\"component\": \"Scaffoldd\""),
    )
    val out = dir.resolve("out")
    val run = run("render", broken.toString(), "--out", out.toString())

    assertEquals(ExitCode.INVALID, run.code)
    val report = readReport(out)
    assertFalse(report.valid)
    assertFalse(report.pass)
    assertTrue(report.renders.isEmpty())
    assertTrue(report.errors.any { "Scaffoldd" in it.message }, report.errors.toString())
    assertTrue("Scaffoldd" in run.err)
  }

  @Test
  fun `a file that is not json exits 2`() {
    val dir = Files.createTempDirectory("invalid")
    val broken = dir.resolve("broken.proposal.json")
    Files.writeString(broken, "{ not json")
    val run = run("render", broken.toString(), "--out", dir.resolve("out").toString())
    assertEquals(ExitCode.INVALID, run.code)
    assertFalse(readReport(dir.resolve("out")).valid)
  }

  @Test
  fun `a file that is not utf-8 exits 2 with a report instead of crashing`() {
    val dir = Files.createTempDirectory("invalid")
    val broken = dir.resolve("broken.proposal.json")
    Files.write(broken, byteArrayOf('{'.code.toByte(), 0xC3.toByte(), 0x28, '}'.code.toByte()))
    val out = dir.resolve("out")

    val run = run("render", broken.toString(), "--out", out.toString())

    assertEquals(ExitCode.INVALID, run.code)
    val report = readReport(out)
    assertFalse(report.valid)
    assertTrue(report.errors.any { "UTF-8" in it.message }, report.errors.toString())
    assertEquals(ExitCode.INVALID, run("export", broken.toString()).code)
  }

  @Test
  fun `a DESIGN dot md error names the file in render and export`() {
    val dir = Files.createTempDirectory("design")
    val design = dir.resolve("BAD.md")
    Files.writeString(design, "---\ncolors: [oops\n---\n")
    val proposal = dir.resolve("small.proposal.json")
    Files.writeString(
      proposal,
      """{"format":1,"navigation":{"start":"home"},"screens":{"home":{"catalogId":"${M3ExpressiveCatalog.ID}",""" +
        """"components":[{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Hi"}]}}}""",
    )

    val render =
      run(
        "render",
        proposal.toString(),
        "--out",
        dir.resolve("out").toString(),
        "--design",
        design.toString(),
      )
    val export = run("export", proposal.toString(), "--design", design.toString())

    assertEquals(ExitCode.INVALID, render.code)
    assertEquals(ExitCode.INVALID, export.code)
    assertTrue(design.toString() in render.err, render.err)
    assertTrue(design.toString() in readReport(dir.resolve("out")).errors.single().message)
  }

  @Test
  fun `an explicit design file replaces the lookup`() {
    val dir = Files.createTempDirectory("design")
    val design = dir.resolve("OTHER.md")
    Files.writeString(design, "---\nversion: alpha\ncolors:\n  primary: \"#006A6A\"\n---\n")
    val proposal = dir.resolve("small.proposal.json")
    Files.writeString(
      proposal,
      """{"format":1,"navigation":{"start":"home"},"screens":{"home":{"catalogId":"${M3ExpressiveCatalog.ID}",""" +
        """"components":[{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Hi"}]}}}""",
    )
    val out = dir.resolve("out")
    run(
      "render",
      proposal.toString(),
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

    // Without --design the folder has no DESIGN.md above it that applies, so defaults are used.
    Files.delete(design)
    val plain = dir.resolve("plain")
    run("render", proposal.toString(), "--out", plain.toString(), "--views", "light")
    assertEquals("#6750A4", readReport(plain).theme.seed)
  }

  @Test
  fun `usage mistakes exit 64`() {
    assertEquals(ExitCode.USAGE, run().code)
    assertTrue("usage" in run().err)
    assertEquals(ExitCode.USAGE, run("nope").code)
    assertEquals(ExitCode.USAGE, run("render").code)
    assertEquals(ExitCode.USAGE, run("render", plates.toString()).code)
    assertEquals(
      ExitCode.USAGE,
      run("render", plates.toString(), "--out", "x", "--views", "huge").code,
    )
    assertEquals(ExitCode.USAGE, run("render", "/no/such/file.json", "--out", "x").code)
    val out = Files.createTempDirectory("render")
    assertEquals(
      ExitCode.USAGE,
      run("render", plates.toString(), "--out", out.toString(), "--screens", "nope").code,
    )
  }

  @Test
  fun `version and catalog print to stdout`() {
    val version = run("--version")
    assertEquals(ExitCode.OK, version.code)
    assertEquals(AppInfo.VERSION, version.out.trim())

    val catalog = run("catalog")
    assertEquals(ExitCode.OK, catalog.code)
    assertTrue("\"protocolVersion\"" in catalog.out)
    assertTrue("\"Scaffold\"" in catalog.out)
    assertEquals("", catalog.err)
  }
}
