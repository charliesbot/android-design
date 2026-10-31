package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.TestProposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class ProposalValidatorTest {
  private val validator = ProposalValidator()

  private fun errors(p: TestProposal, designMd: ThemeOverride? = null): List<ValidationError> =
    validator.validate(p.build(), designMd)

  private fun List<ValidationError>.assertHas(path: String, messagePart: String) {
    val atPath = filter { it.path == path }
    assertTrue(atPath.isNotEmpty(), "no error at $path; got $this")
    assertTrue(
      atPath.any { messagePart in it.message },
      "no error at $path containing '$messagePart'; got $atPath",
    )
  }

  private val base = TestProposal.valid()

  private fun plates(index: String) = "/screens/plates/components/$index"

  @Test
  fun `a valid proposal has no errors`() {
    assertEquals(emptyList(), errors(base))
  }

  @Test
  fun `format must be 1`() {
    errors(base.withFormat(2)).assertHas("/format", "1")
  }

  @Test
  fun `catalogId must name the catalog`() {
    errors(base.withCatalogId("https://example.com/other"))
      .assertHas("/screens/plates/catalogId", "catalog")
  }

  @Test
  fun `root must be present`() {
    errors(base.with("root", """{"id":"not_root","component":"Text","text":"x"}"""))
      .assertHas("/screens/plates/components", "root")
  }

  @Test
  fun `ids must be unique`() {
    val p = base.with("bar2", """{"id":"bar","component":"TopAppBar","title":"Again"}""")
    errors(p).assertHas(plates("${p.indexOf("bar2")}") + "/id", "duplicate")
  }

  @Test
  fun `ids must match the identifier pattern`() {
    val p = base.with("bad", """{"id":"1-bad","component":"Spacer"}""")
    errors(p).assertHas(plates("${p.indexOf("bad")}") + "/id", "identifier")
  }

  @Test
  fun `a component name must be in the catalog`() {
    val p = base.with("bar", """{"id":"bar","component":"AppBar","title":"x"}""")
    errors(p).assertHas(plates("${p.indexOf("bar")}") + "/component", "AppBar")
  }

  @Test
  fun `a prop must be known to its component`() {
    val p = base.with("bar", """{"id":"bar","component":"TopAppBar","title":"x","colour":"red"}""")
    errors(p).assertHas(plates("${p.indexOf("bar")}") + "/colour", "unknown prop")
  }

  @Test
  fun `a required prop must be present`() {
    val p = base.with("bar", """{"id":"bar","component":"TopAppBar"}""")
    errors(p).assertHas(plates("${p.indexOf("bar")}"), "title")
  }

  @Test
  fun `an enum prop must hold one of its values`() {
    val p =
      base.with("more", """{"id":"more","component":"Button","label":"x","variant":"shiny"}""")
    errors(p).assertHas(plates("${p.indexOf("more")}") + "/variant", "shiny")
  }

  @Test
  fun `a child reference must resolve`() {
    val p = base.with("root", """{"id":"root","component":"Scaffold","content":"nowhere"}""")
    errors(p).assertHas(plates("${p.indexOf("root")}") + "/content", "nowhere")
  }

  @Test
  fun `a child list reference must resolve`() {
    val p = base.with("body", """{"id":"body","component":"Column","children":["more","ghost"]}""")
    errors(p).assertHas(plates("${p.indexOf("body")}") + "/children/1", "ghost")
  }

  @Test
  fun `a template child must reference a component`() {
    val p =
      base.with(
        "body",
        """{"id":"body","component":"Column","children":{"componentId":"ghost","path":"/items"}}""",
      )
    errors(p).assertHas(plates("${p.indexOf("body")}") + "/children/componentId", "ghost")
  }

  @Test
  fun `a child must be an allowed component`() {
    val p =
      base.with("root", """{"id":"root","component":"Scaffold","topBar":"more","content":"body"}""")
    errors(p).assertHas(plates("${p.indexOf("root")}") + "/topBar", "TopAppBar")
  }

  @Test
  fun `components must not contain themselves`() {
    val p = base.with("body", """{"id":"body","component":"Column","children":["body"]}""")
    errors(p).assertHas(plates("${p.indexOf("body")}"), "cycle")
  }

  @Test
  fun `a menu anchor refers to its split button without forming a cycle`() {
    val p =
      base
        .with(
          "more",
          """{"id":"more","component":"SplitButton","label":"Cook","menu":"more_menu"}""",
        )
        .with(
          "more_menu",
          """{"id":"more_menu","component":"Menu","anchor":"more","items":["pick"]}""",
        )
        .with("pick", """{"id":"pick","component":"MenuItem","label":"Cook later"}""")
    assertEquals(emptyList(), errors(p))
  }

  @Test
  fun `a menu anchor must still name a component of the same screen`() {
    val p =
      base
        .with(
          "more_menu",
          """{"id":"more_menu","component":"Menu","anchor":"ghost","items":["pick"]}""",
        )
        .with("pick", """{"id":"pick","component":"MenuItem","label":"Cook later"}""")
    errors(p).assertHas(plates("${p.indexOf("more_menu")}") + "/anchor", "ghost")
  }

  @Test
  fun `a color prop must be a color role`() {
    val p =
      base.with(
        "sheet_text",
        """{"id":"sheet_text","component":"Text","text":"x","color":"blurple"}""",
      )
    errors(p).assertHas(plates("${p.indexOf("sheet_text")}") + "/color", "color role")
  }

  @Test
  fun `a type prop must be a type role`() {
    val p =
      base.with(
        "sheet_text",
        """{"id":"sheet_text","component":"Text","text":"x","type":"giant"}""",
      )
    errors(p).assertHas(plates("${p.indexOf("sheet_text")}") + "/type", "type role")
  }

  @Test
  fun `a shape prop accepts shape tokens and MaterialShapes`() {
    val ok =
      base.with("card", """{"id":"card","component":"Card","shape":"extraLarge","content":[]}""")
    assertEquals(emptyList(), errors(ok))
    val shapeName =
      base.with("card", """{"id":"card","component":"Card","shape":"cookie9Sided","content":[]}""")
    assertEquals(emptyList(), errors(shapeName))
    val bad =
      base.with("card", """{"id":"card","component":"Card","shape":"squircle","content":[]}""")
    errors(bad).assertHas(plates("${bad.indexOf("card")}") + "/shape", "shape")
  }

  @Test
  fun `a spacing prop must name a spacing token`() {
    val p =
      base.with("body", """{"id":"body","component":"Column","children":[],"spacing":"huge"}""")
    errors(p).assertHas(plates("${p.indexOf("body")}") + "/spacing", "huge")
  }

  @Test
  fun `a spacing token can come from the proposal theme`() {
    val p =
      base
        .with("body", """{"id":"body","component":"Column","children":[],"spacing":"huge"}""")
        .withTheme("""{"spacing":{"huge":"96px"}}""")
    assertEquals(emptyList(), errors(p))
  }

  @Test
  fun `a style must name a style of the design system`() {
    val p =
      base.with(
        "sheet_text",
        """{"id":"sheet_text","component":"Text","text":"x","style":"hero"}""",
      )
    errors(p).assertHas(plates("${p.indexOf("sheet_text")}") + "/style", "hero")
    val defined =
      p.withTheme("""{"material":{"styles":{"hero":{"type":"displayLarge","color":"primary"}}}}""")
    assertEquals(emptyList(), errors(defined))
  }

  @Test
  fun `a style may only hold token names`() {
    val p =
      base.withTheme(
        """{"material":{"styles":{"hero":{"type":"displayHuge","color":"#ff0000"}}}}""",
      )
    val result = errors(p)
    result.assertHas("/theme/material/styles/hero/type", "type role")
    result.assertHas("/theme/material/styles/hero/color", "color role")
  }

  @Test
  fun `navigate must target a screen`() {
    val p =
      base.with(
        "open_plate",
        """{"id":"open_plate","component":"Button","label":"x","action":{"functionCall":{"@call":"navigate","args":{"screen":"nope"}}}}""",
      )
    errors(p)
      .assertHas(plates("${p.indexOf("open_plate")}") + "/action/functionCall/args/screen", "nope")
  }

  @Test
  fun `open must target an overlay of the same screen`() {
    fun open(target: String) =
      base.with(
        "more",
        """{"id":"more","component":"Button","label":"x","action":{"functionCall":{"@call":"open","args":{"overlay":"$target"}}}}""",
      )
    val path = plates("${base.indexOf("more")}") + "/action/functionCall/args/overlay"
    errors(open("body")).assertHas(path, "overlay")
    errors(open("ghost")).assertHas(path, "ghost")
    assertEquals(emptyList(), errors(open("sheet")))
  }

  @Test
  fun `an overlay of another screen is not a target`() {
    val p =
      base
        .withDetail(
          "other_sheet",
          """{"id":"other_sheet","component":"BottomSheet","content":[]}""",
        )
        .with(
          "more",
          """{"id":"more","component":"Button","label":"x","action":{"functionCall":{"@call":"open","args":{"overlay":"other_sheet"}}}}""",
        )
    errors(p)
      .assertHas(
        plates("${p.indexOf("more")}") + "/action/functionCall/args/overlay",
        "other_sheet",
      )
  }

  @Test
  fun `event actions are rejected`() {
    val p =
      base.with(
        "more",
        """{"id":"more","component":"Button","label":"x","action":{"event":{"name":"go"}}}""",
      )
    errors(p).assertHas(plates("${p.indexOf("more")}") + "/action/event", "event")
  }

  @Test
  fun `an unknown function is rejected`() {
    val p =
      base.with(
        "more",
        """{"id":"more","component":"Button","label":"x","action":{"functionCall":{"@call":"explode"}}}""",
      )
    errors(p).assertHas(plates("${p.indexOf("more")}") + "/action/functionCall/@call", "explode")
  }

  @Test
  fun `navigation start must name a screen`() {
    errors(base.withNavigation("""{"start":"missing"}""")).assertHas("/navigation/start", "missing")
  }

  @Test
  fun `panes must name screens`() {
    errors(base.withNavigation("""{"start":"plates","panes":{"ghost":"detail"}}"""))
      .assertHas("/navigation/panes/ghost", "screen")
  }

  @Test
  fun `showIn must list size classes`() {
    val p =
      base.with(
        "more",
        """{"id":"more","component":"Button","label":"x","showIn":["compact","huge"]}""",
      )
    errors(p).assertHas(plates("${p.indexOf("more")}") + "/showIn/1", "huge")
  }

  @Test
  fun `sizeClass overrides are validated like props`() {
    val p =
      base.with(
        "body",
        """{"id":"body","component":"Column","children":[],"sizeClass":{"medium":{"spacing":"huge"},"giant":{},"expanded":{"colour":"x"}}}""",
      )
    val at = plates("${p.indexOf("body")}") + "/sizeClass"
    val result = errors(p)
    result.assertHas("$at/medium/spacing", "huge")
    result.assertHas("$at/giant", "size class")
    result.assertHas("$at/expanded/colour", "unknown prop")
  }

  @Test
  fun `typography families are restricted per role`() {
    fun typography(entry: String) = base.withTheme("""{"typography":{$entry}}""")
    val ok =
      typography(
        """"display-large":{"fontFamily":"Fraunces"},"headline-small":{"fontFamily":"Bricolage Grotesque"},"display-small":{"fontFamily":"Doto"},"body-large":{"fontFamily":"Google Sans Code"},"label-large":{"fontFamily":"Google Sans Flex"}""",
      )
    assertEquals(emptyList(), errors(ok))
    errors(typography(""""body-large":{"fontFamily":"Fraunces"}"""))
      .assertHas("/theme/typography/body-large/fontFamily", "Fraunces")
    errors(typography(""""headline-large":{"fontFamily":"Doto"}"""))
      .assertHas("/theme/typography/headline-large/fontFamily", "Doto")
    errors(typography(""""title-large-emphasized":{"fontFamily":"Comic Sans"}"""))
      .assertHas("/theme/typography/title-large-emphasized/fontFamily", "Comic Sans")
  }

  @Test
  fun `typography families in DESIGN dot md are checked too`() {
    val designMd =
      Json.decodeFromString(
        ThemeOverride.serializer(),
        """{"typography":{"body-large":{"fontFamily":"Fraunces"}}}""",
      )
    errors(base, designMd).assertHas("/designMd/typography/body-large/fontFamily", "Fraunces")
  }

  @Test
  fun `DESIGN dot md may hold names the proposal does not understand`() {
    val designMd =
      Json.decodeFromString(
        ThemeOverride.serializer(),
        """{"colors":{"brand-accent":"#ff00ff"},"typography":{"headline-xl":{"fontFamily":"Anything"}}}""",
      )
    assertEquals(emptyList(), errors(base, designMd))
  }

  @Test
  fun `the proposal theme is strict about names`() {
    errors(base.withTheme("""{"colors":{"brand-accent":"#ff00ff"}}"""))
      .assertHas("/theme/colors/brand-accent", "color role")
  }

  @Test
  fun `theme colors must be hex`() {
    errors(base.withTheme("""{"colors":{"primary":"red","surface":"#12"}}""")).let {
      it.assertHas("/theme/colors/primary", "#RRGGBB")
      it.assertHas("/theme/colors/surface", "#RRGGBB")
    }
  }

  @Test
  fun `scheme variant and contrast are checked`() {
    val result =
      errors(base.withTheme("""{"material":{"scheme":{"variant":"glitter","contrast":2.0}}}"""))
    result.assertHas("/theme/material/scheme/variant", "glitter")
    result.assertHas("/theme/material/scheme/contrast", "-1")
  }

  @Test
  fun `rounded and spacing values must be dimensions`() {
    val result =
      errors(
        base.withTheme(
          """{"rounded":{"extra-large":"big","wobbly":"4px"},"spacing":{"md":"wide"}}""",
        ),
      )
    result.assertHas("/theme/rounded/extra-large", "dimension")
    result.assertHas("/theme/rounded/wobbly", "corner")
    result.assertHas("/theme/spacing/md", "dimension")
  }

  // region numbers that would throw in the renderer

  /** The errors of one extra component added to the valid proposal as `probe`. */
  private fun probe(json: String): List<ValidationError> =
    errors(base.with("probe", json.replace("""{"id":"probe",""", """{"id":"probe",""")))

  private fun probePath(suffix: String) =
    plates("${base.with("probe", "{}").indexOf("probe")}") + suffix

  private fun List<ValidationError>.assertHasProbe(suffix: String, messagePart: String) =
    assertHas(probePath(suffix), messagePart)

  @Test
  fun `a number prop outside its bounds is an error that states them`() {
    val cases =
      listOf(
        """{"id":"probe","component":"Text","text":"x","maxLines":0}""" to "/maxLines",
        """{"id":"probe","component":"Text","text":"x","maxLines":-2}""" to "/maxLines",
        """{"id":"probe","component":"Text","text":"x","maxLines":1.5}""" to "/maxLines",
        """{"id":"probe","component":"TextField","lines":0}""" to "/lines",
        """{"id":"probe","component":"Slider","steps":-1}""" to "/steps",
        """{"id":"probe","component":"Slider","steps":2.5}""" to "/steps",
        """{"id":"probe","component":"RangeSlider","steps":-1}""" to "/steps",
        """{"id":"probe","component":"Grid","children":["more"],"columns":0}""" to "/columns",
        """{"id":"probe","component":"Grid","children":["more"],"columns":1000000}""" to "/columns",
        """{"id":"probe","component":"Grid","children":["more"],"minColumnWidth":0}""" to
          "/minColumnWidth",
        """{"id":"probe","component":"Image","src":"a.png","aspectRatio":0}""" to "/aspectRatio",
        """{"id":"probe","component":"Image","src":"a.png","aspectRatio":-1}""" to "/aspectRatio",
        """{"id":"probe","component":"Icon","name":"home","size":-4}""" to "/size",
        """{"id":"probe","component":"Icon","name":"home","size":1000000}""" to "/size",
        """{"id":"probe","component":"ProgressIndicator","progress":1.5}""" to "/progress",
        """{"id":"probe","component":"ProgressIndicator","progress":-0.1}""" to "/progress",
        """{"id":"probe","component":"Text","text":"x","weight":0}""" to "/weight",
        """{"id":"probe","component":"Text","text":"x","weight":-1}""" to "/weight",
      )
    for ((json, suffix) in cases) {
      val result = probe(json)
      assertTrue(
        result.any {
          it.path == probePath(suffix) && "must be a" in it.message && "from" in it.message
        },
        "$json should be rejected at $suffix; got $result",
      )
    }
  }

  @Test
  fun `numbers inside their bounds and bound numbers are accepted`() {
    val ok =
      listOf(
        """{"id":"probe","component":"Text","text":"x","maxLines":3,"weight":1.5}""",
        """{"id":"probe","component":"TextField","lines":4}""",
        """{"id":"probe","component":"Slider","steps":5,"range":[0,100],"value":50}""",
        """{"id":"probe","component":"Grid","children":["more"],"columns":3}""",
        """{"id":"probe","component":"Grid","children":["more"],"minColumnWidth":160.5}""",
        """{"id":"probe","component":"Image","src":"a.png","aspectRatio":1.7778}""",
        """{"id":"probe","component":"ProgressIndicator","progress":0}""",
        """{"id":"probe","component":"Text","text":"x","maxLines":{"@path":"/n"}}""",
        """{"id":"probe","component":"Slider","steps":{"@path":"/n"},"value":{"@path":"/v"}}""",
      )
    for (json in ok) assertEquals(emptyList(), probe(json), json)
  }

  @Test
  fun `a number override outside its bounds is an error at the override`() {
    val result =
      probe(
        """{"id":"probe","component":"Grid","children":["more"],"columns":2,"sizeClass":{"medium":{"columns":0}}}""",
      )
    result.assertHasProbe("/sizeClass/medium/columns", "from 1 to 24")
  }

  @Test
  fun `a slider range must run from a smaller to a larger number`() {
    probe("""{"id":"probe","component":"Slider","range":[10,0]}""")
      .assertHasProbe("/range", "less than")
    probe("""{"id":"probe","component":"Slider","range":[5,5]}""")
      .assertHasProbe("/range", "less than")
    probe("""{"id":"probe","component":"RangeSlider","range":[10,0]}""")
      .assertHasProbe("/range", "less than")
  }

  @Test
  fun `a slider value must lie inside its range`() {
    probe("""{"id":"probe","component":"Slider","range":[0,10],"value":11}""")
      .assertHasProbe("/value", "range")
    probe("""{"id":"probe","component":"Slider","value":50}""").assertHasProbe("/value", "range")
    probe("""{"id":"probe","component":"RangeSlider","range":[0,10],"value":[-1,5]}""")
      .assertHasProbe("/value", "range")
    probe("""{"id":"probe","component":"RangeSlider","range":[0,10],"value":[9,1]}""")
      .assertHasProbe("/value", "start")
    assertEquals(
      emptyList(),
      probe("""{"id":"probe","component":"RangeSlider","range":[0,10],"value":[2,8]}"""),
    )
  }

  @Test
  fun `a size class override is checked against the props it applies to`() {
    // At medium and up the range shrinks below the base value.
    probe(
        """{"id":"probe","component":"Slider","range":[0,100],"value":50,"sizeClass":{"medium":{"range":[0,10]}}}""",
      )
      .assertHasProbe("/sizeClass/medium/range", "range")
    probe(
        """{"id":"probe","component":"Slider","range":[0,10],"sizeClass":{"expanded":{"range":[10,0]}}}""",
      )
      .assertHasProbe("/sizeClass/expanded/range", "less than")
  }

  @Test
  fun `spacing and corner sizes cannot be negative`() {
    val result = errors(base.withTheme("""{"rounded":{"large":"-4px"},"spacing":{"md":"-16px"}}"""))
    result.assertHas("/theme/rounded/large", "negative")
    result.assertHas("/theme/spacing/md", "negative")
  }

  @Test
  fun `a font weight must be between 1 and 1000`() {
    val result =
      errors(
        base.withTheme(
          """{"typography":{"body-large":{"fontWeight":0},"body-small":{"fontWeight":2000}}}""",
        ),
      )
    result.assertHas("/theme/typography/body-large/fontWeight", "1 to 1000")
    result.assertHas("/theme/typography/body-small/fontWeight", "1 to 1000")
  }

  @Test
  fun `a wght axis in a font variation must be between 1 and 1000`() {
    val result =
      errors(
        base.withTheme(
          """{"typography":{"body-large":{"fontVariation":"'wght' 0"},"body-small":{"fontVariation":"'opsz' 14, 'wght' 2000"}}}""",
        ),
      )
    result.assertHas("/theme/typography/body-large/fontVariation", "wght")
    result.assertHas("/theme/typography/body-small/fontVariation", "1 to 1000")
    val edges =
      base.withTheme(
        """{"typography":{"body-large":{"fontVariation":"'wght' 1000, 'opsz' 14"},"body-small":{"fontVariation":"'wght' 1"}}}""",
      )
    assertEquals(emptyList(), errors(edges))
  }

  @Test
  fun `every axis value in a font variation must be finite`() {
    val huge = "9".repeat(60)
    val result =
      errors(
        base.withTheme(
          """{"typography":{"display-large":{"fontVariation":"'opsz' $huge"},"display-small":{"fontVariation":"'wght' $huge"}}}""",
        ),
      )
    result.assertHas("/theme/typography/display-large/fontVariation", "finite")
    result.assertHas("/theme/typography/display-small/fontVariation", "finite")
  }

  // endregion
}
