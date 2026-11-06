package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.ProposalReader
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.WearCatalog
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExportCommandTest {
  private class Run(val code: Int, val out: String, val err: String)

  private fun run(vararg args: String): Run {
    val out = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    val code = Cli.run(args.toList(), PrintStream(out, true), PrintStream(err, true))
    return Run(code, out.toString(), err.toString())
  }

  private val fixtures = Path.of(System.getProperty("fixtures.dir"))
  private val plates = fixtures.resolve("plates.proposal.json")

  @Test
  fun `one screen exports to stdout as Compose`() {
    val run = run("export", plates.toString(), "--screen", "plates")
    assertEquals(ExitCode.OK, run.code, run.err)
    assertEquals("", run.err)
    assertTrue("@Composable" in run.out)
    assertTrue("fun PlatesScreen" in run.out, run.out.take(600))
    assertFalse("fun PlateDetailScreen" in run.out, "only the asked-for screen")
  }

  @Test
  fun `without a screen every screen and the navigation host are exported`() {
    val run = run("export", plates.toString())
    assertEquals(ExitCode.OK, run.code, run.err)
    assertTrue("fun PlatesScreen" in run.out)
    assertTrue("fun PlateDetailScreen" in run.out)
    assertTrue("NavDisplay" in run.out)
  }

  @Test
  fun `the export uses the design the proposal resolves`() {
    val dir = Files.createTempDirectory("export")
    val design = dir.resolve("OTHER.md")
    Files.writeString(design, "---\nversion: alpha\ncolors:\n  primary: \"#006A6A\"\n---\n")
    val proposal = dir.resolve("small.proposal.json")
    Files.writeString(
      proposal,
      """{"format":1,"navigation":{"start":"home"},"screens":{"home":{"catalogId":"${M3ExpressiveCatalog.ID}",""" +
        """"components":[{"id":"root","component":"Scaffold","content":"t"},{"id":"t","component":"Text","text":"Hi"}]}}}""",
    )
    val withDesign = run("export", proposal.toString(), "--design", design.toString())
    assertEquals(ExitCode.OK, withDesign.code, withDesign.err)
    assertTrue("006a6a" in withDesign.out.lowercase(), "seed of the explicit design")
    val without = run("export", proposal.toString())
    assertFalse("006a6a" in without.out.lowercase())
  }

  @Test
  fun `an invalid proposal exits 2 and prints nothing to stdout`() {
    val dir = Files.createTempDirectory("export")
    val broken = dir.resolve("broken.proposal.json")
    Files.writeString(
      broken,
      Files.readString(plates)
        .replace("\"component\": \"Scaffold\"", "\"component\": \"Scaffoldd\""),
    )
    val run = run("export", broken.toString())
    assertEquals(ExitCode.INVALID, run.code)
    assertEquals("", run.out)
    assertTrue("Scaffoldd" in run.err, run.err)
  }

  @Test
  fun `usage mistakes exit 64`() {
    assertEquals(ExitCode.USAGE, run("export").code)
    assertEquals(ExitCode.USAGE, run("export", "/no/such/file.json").code)
    val unknown = run("export", plates.toString(), "--screen", "nope")
    assertEquals(ExitCode.USAGE, unknown.code)
    assertTrue("plates" in unknown.err, "lists the screens that exist")
    assertEquals(ExitCode.USAGE, run("export", plates.toString(), "--bogus").code)
    assertEquals(ExitCode.USAGE, run("export", plates.toString(), "--screen").code)
  }

  private val wear = fixtures.resolve("wear.proposal.json")

  @Test
  fun `a Wear proposal exports as Wear Compose with the colors the previewer draws`() {
    val run = run("export", wear.toString())
    assertEquals(ExitCode.OK, run.code, run.err)
    assertEquals("", run.err)
    assertTrue("fun WearAppTheme" in run.out)
    assertTrue("rememberSwipeDismissableSceneStrategy" in run.out)
    assertTrue("fun HomeScreen" in run.out)
    val design = ProposalReader().read(wear).designSystem
    val scheme = WearThemeResolver.roleColors(design)
    assertEquals(WearCatalog.colorRoles.toSet(), scheme.keys)
    WearCatalog.colorRoles.forEach { role ->
      val hex = scheme.getValue(role).removePrefix("#")
      assertTrue("$role = Color(0xFF$hex)" in run.out, "$role should be $hex in:\n${run.out}")
    }
  }

  @Test
  fun `a Wear export carries the scheme the design's seed gives`() {
    val dir = Files.createTempDirectory("export")
    val design = dir.resolve("OTHER.md")
    Files.writeString(design, "---\nversion: alpha\ncolors:\n  primary: \"#006A6A\"\n---\n")
    val seeded = run("export", wear.toString(), "--design", design.toString(), "--screen", "home")
    assertEquals(ExitCode.OK, seeded.code, seeded.err)
    val plain = run("export", wear.toString(), "--screen", "home")
    val resolved = ProposalReader().read(wear, design).designSystem
    val primary = WearThemeResolver.roleColors(resolved).getValue("primary")
    assertTrue("primary = Color(0xFF${primary.removePrefix("#")})" in seeded.out)
    assertFalse(seeded.out == plain.out, "the seed changes the scheme")
  }
}
