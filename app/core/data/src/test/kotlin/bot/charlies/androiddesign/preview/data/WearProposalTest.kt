package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.validation.ProposalValidator
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WearProposalTest {
  private val fixture = Fixtures.text("wear.proposal.json")
  private val phoneId = "https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1"
  private val wearId =
    "https://github.com/charliesbot/android-design/catalogs/wear-m3-expressive/v1"

  private fun errors(text: String): List<ValidationError> =
    ProposalValidator().validate(assertIs<ProposalParse.Parsed>(ProposalCodec.parse(text)).proposal)

  private fun List<ValidationError>.assertHas(path: String, part: String) =
    assertTrue(any { it.path == path && part in it.message }, "no '$part' at $path in $this")

  @Test
  fun `the wear fixture is valid`() {
    assertEquals(emptyList(), errors(fixture))
  }

  @Test
  fun `an edge button takes an icon or a label, never both or neither`() {
    val label = """{ "id": "start", "component": "EdgeButton", "label": "Start","""
    val path = "/screens/home/components/4"
    errors(fixture.replace(label, "$label \"icon\": \"play_arrow\","))
      .assertHas(path, "Either an Icon or Text")
    val icon = """{ "id": "start", "component": "EdgeButton", "icon": "play_arrow","""
    assertEquals(emptyList(), errors(fixture.replace(label, icon)))
    val neither = """{ "id": "start", "component": "EdgeButton","""
    errors(fixture.replace(label, neither)).assertHas(path, "Either an Icon or Text")
  }

  @Test
  fun `a phone screen cannot use a wear component`() {
    errors(fixture.replace(wearId, phoneId))
      .assertHas("/screens/home/components/0/component", "unknown component 'Screen'")
  }

  @Test
  fun `a proposal draws from one catalog`() {
    val mixed =
      fixture.replace(
        "\"screens\": {",
        "\"screens\": { \"other\": { \"catalogId\": \"$phoneId\", \"components\": [" +
          "{ \"id\": \"root\", \"component\": \"Column\", \"children\": [] }] },",
      )
    errors(mixed).assertHas("/screens/other/catalogId", "one catalog")
  }

  @Test
  fun `wear proposals have no panes`() {
    errors(
        fixture.replace(
          "\"start\": \"home\"",
          "\"start\": \"home\", \"panes\": {\"home\": \"list\"}",
        ),
      )
      .assertHas("/navigation/panes", "no panes")
  }

  @Test
  fun `wear color and type roles are validated against wear`() {
    errors(fixture.replace("primaryDim", "surface"))
      .assertHas("/screens/home/components/2/color", "not a color role")
    errors(fixture.replace("numeralLarge", "headlineLarge"))
      .assertHas("/screens/home/components/2/type", "not a type role")
  }

  @Test
  fun `wear theme colors use wear roles`() {
    fun themed(color: String) =
      fixture.replace(
        "\"navigation\"",
        "\"theme\": {\"colors\": {\"$color\": \"#112233\"}}, \"navigation\"",
      )
    assertEquals(emptyList(), errors(themed("primary-dim")))
    errors(themed("surface")).assertHas("/theme/colors/surface", "not a color role")
  }

  @Test
  fun `the sizeClass override accepts only large`() {
    errors(fixture.replace("\"large\":", "\"medium\":"))
      .assertHas("/screens/home/components/2/sizeClass/medium", "not an overridable")
  }

  @Test
  fun `showIn does not exist on wear`() {
    errors(fixture.replace("\"text\": \"Timer\"", "\"text\": \"Timer\", \"showIn\": [\"compact\"]"))
      .assertHas("/screens/home/components/1/showIn", "unknown prop")
  }

  private val phoneDesign =
    assertIs<DesignMdParse.Parsed>(DesignMdParser.parse(Fixtures.text("DESIGN.md"))).theme

  private fun errorsNextTo(design: ThemeOverride, text: String = fixture): List<ValidationError> =
    ProposalValidator()
      .validate(assertIs<ProposalParse.Parsed>(ProposalCodec.parse(text)).proposal, design)

  @Test
  fun `a phone DESIGN_md with brand fonts and phone-only style roles is valid next to wear`() {
    assertEquals(emptyList(), errorsNextTo(phoneDesign))
  }

  @Test
  fun `DESIGN_md typography does not reach wear but its seed does`() {
    val design = DesignSystemResolver.resolve(phoneDesign, null, WearCatalog)
    assertEquals(emptyMap(), design.typography)
    assertTrue(design.seeded)
    assertEquals("#B5523B", design.seed)
  }

  @Test
  fun `a role unknown to every catalog still errors in DESIGN_md`() {
    val design =
      phoneDesign.copy(
        material =
          phoneDesign.material?.copy(
            styles = mapOf("bad" to StyleDef(type = "nopeRole", color = "primaryDim")),
          ),
      )
    val errors = errorsNextTo(design)
    errors.assertHas("/designMd/material/styles/bad/type", "not a type role")
    assertTrue(errors.none { it.path.endsWith("/color") }, "primaryDim is a wear role: $errors")
  }

  @Test
  fun `a wear screen cannot use a style with a role wear lacks`() {
    errorsNextTo(
        phoneDesign,
        fixture.replace("\"text\": \"Timer\"", "\"text\": \"Timer\", \"style\": \"hero_title\""),
      )
      .assertHas("/screens/home/components/1/style", "displayLargeEmphasized")
  }

  @Test
  fun `a wear theme may set Roboto Flex but no brand face`() {
    fun themed(family: String) =
      fixture.replace(
        "\"navigation\"",
        "\"theme\": {\"typography\": {\"display-large\": {\"fontFamily\": \"$family\"}}}, \"navigation\"",
      )
    assertEquals(emptyList(), errors(themed("Roboto Flex")))
    errors(themed("Fraunces"))
      .assertHas("/theme/typography/display-large/fontFamily", "Roboto Flex")
  }
}
