package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.has
import bot.charlies.androiddesign.preview.model.Proposal
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/** The Wear fixture that `:core:data` keeps exports, whole and screen by screen. */
class WearFixtureExportTest {
  private val proposal: Proposal =
    Json.decodeFromString(
      Proposal.serializer(),
      File(requireNotNull(System.getProperty("fixtures.dir")), "wear.proposal.json").readText(),
    )
  private val design = DesignSystemResolver.resolve(null, proposal.theme)

  @Test
  fun `the whole proposal exports with its navigation host`() {
    val source = KotlinExporter.export(proposal, design, null, WearExportFixtures.colors)
    WearExportFixtures.dump("wear-fixture.kt", source)
    proposal.screens.keys.forEach {
      has(source, "fun ${it.replaceFirstChar(Char::uppercase)}Screen(")
    }
    has(source, "fun WearTimerApp()")
    has(source, "rememberSwipeDismissableSceneStrategy<NavKey>()")
  }

  @Test
  fun `each screen exports on its own`() {
    proposal.screens.keys.forEach { id ->
      val source = KotlinExporter.export(proposal, design, id, WearExportFixtures.colors)
      assertTrue("fun ${id.replaceFirstChar(Char::uppercase)}Screen(" in source, id)
      assertTrue("NavDisplay" !in source, id)
    }
  }
}
