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
  fun `a prop may state its catalog default`() {
    val stated =
      base
        .with(
          "open_plate",
          """{"id":"open_plate","component":"Button","label":"Cook","variant":"filled","size":"small","shape":"round","enabled":true,"primary":true}""",
        )
        .with(
          "body",
          """{"id":"body","component":"Column","children":["open_plate","more","icon","field"],"spacing":"md","horizontalAlignment":"start","arrangement":"start","scroll":false}""",
        )
        .with("icon", """{"id":"icon","component":"Icon","name":"home","size":24,"filled":false}""")
        .with("field", """{"id":"field","component":"TextField","variant":"filled","lines":1}""")
    assertEquals(emptyList(), errors(stated))
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

  private val withPick = base.withPickScreen()

  private fun placeholders(json: String) =
    withPick.withNavigation(
      """{"start":"plates","panes":{"plates":"list","plate_detail":"detail"},"placeholders":$json}""",
    )

  @Test
  fun `a list screen may declare a placeholder screen`() {
    assertEquals(emptyList(), errors(placeholders("""{"plates":"pick"}""")))
  }

  @Test
  fun `a placeholder key must be a list screen`() {
    errors(placeholders("""{"ghost":"pick"}"""))
      .assertHas("/navigation/placeholders/ghost", "not a screen")
    errors(placeholders("""{"plate_detail":"pick"}"""))
      .assertHas("/navigation/placeholders/plate_detail", "list")
    errors(placeholders("""{"pick":"pick"}""")).assertHas("/navigation/placeholders/pick", "list")
  }

  @Test
  fun `a placeholder must name a screen`() {
    errors(placeholders("""{"plates":"ghost"}"""))
      .assertHas("/navigation/placeholders/plates", "'ghost' is not a screen")
  }

  @Test
  fun `a placeholder is in no pane`() {
    errors(placeholders("""{"plates":"plate_detail"}"""))
      .assertHas("/navigation/placeholders/plates", "pane")
  }

  @Test
  fun `a placeholder is not a navigation destination`() {
    val p =
      withPick
        .with(
          "more",
          """{"id":"more","component":"Button","label":"x","action":{"functionCall":{"@call":"navigate","args":{"screen":"pick"}}}}""",
        )
        .withNavigation(
          """{"start":"plates","panes":{"plates":"list","plate_detail":"detail"},"placeholders":{"plates":"pick"}}""",
        )
    errors(p).assertHas("/navigation/placeholders/plates", "navigates to")
  }

  @Test
  fun `the start screen cannot be a placeholder`() {
    val p =
      withPick.withNavigation(
        """{"start":"pick","panes":{"plates":"list","plate_detail":"detail"},"placeholders":{"plates":"pick"}}""",
      )
    errors(p).assertHas("/navigation/placeholders/plates", "start")
  }

  @Test
  fun `a supporting pane layout has no placeholders`() {
    val p =
      withPick.withNavigation(
        """{"start":"plates","panes":{"plates":"list","plate_detail":"supporting"},"placeholders":{"plates":"pick"}}""",
      )
    errors(p).assertHas("/navigation/placeholders", "supporting")
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

  @Test
  fun `a snackbar needs a scaffold to draw in`() {
    val snackbar = """{"id":"saved","component":"Snackbar","message":"Saved"}"""
    assertEquals(emptyList(), errors(base.with("saved", snackbar)))
    val loose =
      base
        .withDetail("saved", snackbar)
        .withDetail("root", """{"id":"root","component":"Column","children":[]}""")
    errors(loose)
      .assertHas(
        "/screens/plate_detail/components/2/component",
        "no Scaffold",
      )
  }

  @Test
  fun `a badge is a flag, a number or a text of at most four characters`() {
    fun badge(value: String) =
      base.with(
        "more",
        """{"id":"more","component":"IconButton","icon":"mail","badge":$value}""",
      )
    val path = plates("${base.indexOf("more")}") + "/badge"
    for (ok in listOf("true", "false", "7", "\"99+\"", "\"new\"", """{"@path":"/count"}""")) {
      assertEquals(emptyList(), errors(badge(ok)), ok)
    }
    errors(badge("\"12345\"")).assertHas(path, "at most 4 characters")
    errors(badge("12345")).assertHas(path, "at most 4 characters")
    errors(badge("1000")).let { assertEquals(emptyList(), it) }
    errors(badge("[1]")).assertHas(path, "must be true, false")
  }

  private fun icon(id: String) =
    """{"id":"$id","component":"IconButton","icon":"mic","contentDescription":"$id"}"""

  private fun view(id: String) = """{"id":"$id","component":"SearchView","content":[]}"""

  @Test
  fun `a search bar has at most two trailing elements, an avatar counting as one`() {
    fun bar(extra: String) =
      base
        .with("a", icon("a"))
        .with("b", icon("b"))
        .with("c", icon("c"))
        .with("results", view("results"))
        .with(
          "field",
          """{"id":"field","component":"SearchBar","placeholder":"Search","expanded":"results"$extra}""",
        )
    assertEquals(emptyList(), errors(bar(""","trailing":["a","b"]""")))
    assertEquals(emptyList(), errors(bar(""","trailing":["a"],"avatar":"me.png"""")))
    val three = bar(""","trailing":["a","b","c"]""")
    errors(three).assertHas(plates("${three.indexOf("field")}") + "/trailing", "at most 2")
    val avatar = bar(""","trailing":["a","b"],"avatar":"me.png"""")
    errors(avatar).assertHas(plates("${avatar.indexOf("field")}") + "/trailing", "at most 2")
  }

  @Test
  fun `a search app bar has two actions, and up to four only on larger screens`() {
    fun bar(extra: String) =
      base
        .with("a", icon("a"))
        .with("b", icon("b"))
        .with("c", icon("c"))
        .with("d", icon("d"))
        .with("results", view("results"))
        .with(
          "bar",
          """{"id":"bar","component":"TopAppBar","size":"search","title":"Search","expanded":"results"$extra}""",
        )
    assertEquals(emptyList(), errors(bar(""","actions":["a","b"],"avatar":"me.png"""")))
    assertEquals(
      emptyList(),
      errors(bar(""","actions":["a","b"],"sizeClass":{"medium":{"actions":["a","b","c","d"]}}""")),
    )
    val four = bar(""","actions":["a","b","c","d"]""")
    errors(four).assertHas(plates("${four.indexOf("bar")}") + "/actions", "at most 2")
    val three = bar(""","actions":["a","b","c"],"avatar":"me.png"""")
    errors(three).assertHas(plates("${three.indexOf("bar")}") + "/actions", "at most 2")
    val five =
      bar(""","actions":["a","b"],"sizeClass":{"medium":{"actions":["a","b","c","d","a"]}}""")
    errors(five)
      .assertHas(plates("${five.indexOf("bar")}") + "/sizeClass/medium/actions", "at most 4")
  }

  @Test
  fun `expanded is for a search app bar, which a size class override can make`() {
    val small =
      base
        .with("results", view("results"))
        .with(
          "bar",
          """{"id":"bar","component":"TopAppBar","title":"Plates","sizeClass":{"medium":{"size":"search"}},"expanded":"results"}""",
        )
    assertEquals(emptyList(), errors(small))
  }

  @Test
  fun `avatar and expanded belong to the search size of the top app bar`() {
    val small =
      base
        .with("results", view("results"))
        .with(
          "bar",
          """{"id":"bar","component":"TopAppBar","title":"Plates","avatar":"me.png","expanded":"results"}""",
        )
    val path = plates("${small.indexOf("bar")}")
    errors(small).assertHas("$path/avatar", "size search")
    errors(small).assertHas("$path/expanded", "size search")
  }

  @Test
  fun `a search view needs exactly one bar that names it`() {
    val loose = base.with("results", view("results"))
    errors(loose)
      .assertHas(plates("${loose.indexOf("results")}") + "/component", "nothing names 'results'")
    val twice =
      loose
        .with(
          "one",
          """{"id":"one","component":"SearchBar","placeholder":"Search","expanded":"results"}""",
        )
        .with(
          "two",
          """{"id":"two","component":"SearchBar","placeholder":"Search","expanded":"results"}""",
        )
    errors(twice).assertHas(plates("${twice.indexOf("results")}") + "/component", "one and two")
  }

  @Test
  fun `a tooltip needs an anchor, and only a rich one has a subhead and up to two buttons`() {
    fun tip(extra: String) =
      base
        .with("a", """{"id":"a","component":"Button","variant":"text","label":"A"}""")
        .with("b", """{"id":"b","component":"Button","variant":"text","label":"B"}""")
        .with("c", """{"id":"c","component":"Button","variant":"text","label":"C"}""")
        .with(
          "tip",
          """{"id":"tip","component":"Tooltip","anchor":"more","text":"Hi"$extra}""",
        )
    assertEquals(emptyList(), errors(tip("")))
    assertEquals(
      emptyList(),
      errors(tip(""","variant":"rich","title":"T","actions":["a","b"]""")),
    )
    val plain = tip(""","title":"T","actions":["a"]""")
    val path = plates("${plain.indexOf("tip")}")
    errors(plain).assertHas("$path/title", "rich tooltip")
    errors(plain).assertHas("$path/actions", "rich tooltip")
    val three = tip(""","variant":"rich","actions":["a","b","c"]""")
    errors(three).assertHas(plates("${three.indexOf("tip")}") + "/actions", "at most 2")
    val noAnchor = base.with("tip", """{"id":"tip","component":"Tooltip","text":"Hi"}""")
    errors(noAnchor).assertHas(plates("${noAnchor.indexOf("tip")}"), "requires prop 'anchor'")
  }

  private fun buttons(vararg ids: String) =
    ids.fold(base) { proposal, id ->
      proposal.with(id, """{"id":"$id","component":"Button","variant":"text","label":"$id"}""")
    }

  @Test
  fun `a malformed size class entry on a search app bar is an error, not a crash`() {
    val bar =
      base
        .with("results", view("results"))
        .with(
          "bar",
          """{"id":"bar","component":"TopAppBar","size":"search","title":"Search","expanded":"results","actions":["a","a","a","a","a"],"sizeClass":{"medium":"wide"}}""",
        )
        .with("a", icon("a"))
    errors(bar)
      .assertHas(plates("${bar.indexOf("bar")}") + "/sizeClass/medium", "must be an object")
  }

  @Test
  fun `a size class override cannot get around a tooltip's limits`() {
    fun tip(extra: String) =
      buttons("a", "b", "c")
        .with("tip", """{"id":"tip","component":"Tooltip","anchor":"more","text":"Hi"$extra}""")
    val actions = tip(""","sizeClass":{"expanded":{"actions":["a","b","c"]}}""")
    errors(actions)
      .assertHas(plates("${actions.indexOf("tip")}") + "/sizeClass/expanded/actions", "at most 2")
    val plain =
      tip(
        ""","variant":"rich","title":"T","actions":["a"],"sizeClass":{"medium":{"variant":"plain"}}""",
      )
    val path = plates("${plain.indexOf("tip")}") + "/sizeClass/medium"
    errors(plain).assertHas("$path/variant", "rich tooltip")
  }

  @Test
  fun `a size class override cannot get around the search bar's trailing limit`() {
    val bar =
      base
        .with("a", icon("a"))
        .with("b", icon("b"))
        .with("c", icon("c"))
        .with("results", view("results"))
        .with(
          "field",
          """{"id":"field","component":"SearchBar","placeholder":"Search","expanded":"results","trailing":["a"],"sizeClass":{"medium":{"trailing":["a","b","c"]}}}""",
        )
    errors(bar)
      .assertHas(plates("${bar.indexOf("field")}") + "/sizeClass/medium/trailing", "at most 2")
  }

  // region selection groups

  private fun radios(vararg selected: Boolean, extra: String = "") =
    selected.indices.fold(
      base.with(
        "group",
        """{"id":"group","component":"RadioGroup","children":[${selected.indices.joinToString { "\"r$it\"" }}]$extra}""",
      ),
    ) { acc, i ->
      acc.with(
        "r$i",
        """{"id":"r$i","component":"RadioButton","label":"Option $i"${if (selected[i]) ""","selected":true""" else ""}}""",
      )
    }

  @Test
  fun `a radio group has exactly one selected button`() {
    assertEquals(emptyList(), errors(radios(false, true, false)))
    val none = radios(false, false)
    errors(none).assertHas(plates("${none.indexOf("group")}") + "/children", "exactly one selected")
    val two = radios(true, true)
    errors(two).assertHas(plates("${two.indexOf("group")}") + "/children", "got 2")
  }

  @Test
  fun `a radio button hidden at a size class does not count there`() {
    val hidden =
      radios(true, true)
        .with(
          "r1",
          """{"id":"r1","component":"RadioButton","label":"Two","selected":true,"showIn":["expanded"]}""",
        )
    val found = errors(hidden).filter { "exactly one selected" in it.message }
    assertEquals(1, found.size, "$found")
    assertTrue(found.single().message.endsWith("got 2 at expanded"), found.single().message)
  }

  @Test
  fun `a radio group whose selection comes from data is not judged`() {
    val bound =
      radios(true, false)
        .with(
          "r1",
          """{"id":"r1","component":"RadioButton","label":"Two","selected":{"@path":"/two"}}""",
        )
    assertEquals(emptyList(), errors(bound).filter { "exactly one selected" in it.message })
  }

  @Test
  fun `a malformed selected value is left to the prop checks and does not crash`() {
    val odd =
      radios(true, false)
        .with("r1", """{"id":"r1","component":"RadioButton","label":"Two","selected":"yes"}""")
    errors(odd)
  }

  private fun menuOf(vararg items: String) =
    items
      .foldIndexed(base) { i, acc, item -> acc.with("i$i", item.replace("ID", "i$i")) }
      .with(
        "menu",
        """{"id":"menu","component":"Menu","anchor":"more","items":[${items.indices.joinToString { "\"i$it\"" }}]}""",
      )

  private fun single(selected: Boolean) =
    """{"id":"ID","component":"MenuItem","label":"A","role":"single"${if (selected) ""","selected":true""" else ""}}"""

  @Test
  fun `a single-select menu group has at most one selected item`() {
    assertEquals(emptyList(), errors(menuOf(single(true), single(false))))
    assertEquals(emptyList(), errors(menuOf(single(false), single(false))))
    val two = menuOf(single(true), single(true))
    errors(two).assertHas(plates("${two.indexOf("menu")}") + "/items", "one at a time")
  }

  @Test
  fun `a multi-select menu may select any number`() {
    val multi = """{"id":"ID","component":"MenuItem","label":"A","role":"multi","selected":true}"""
    assertEquals(emptyList(), errors(menuOf(multi, multi)))
  }

  @Test
  fun `single-select items are counted per menu group`() {
    val grouped =
      menuOf(single(true), single(true))
        .with("g1", """{"id":"g1","component":"MenuGroup","items":["i0"]}""")
        .with("g2", """{"id":"g2","component":"MenuGroup","items":["i1"]}""")
        .with("menu", """{"id":"menu","component":"Menu","anchor":"more","items":["g1","g2"]}""")
    assertEquals(emptyList(), errors(grouped))
  }

  @Test
  fun `a menu holds items or groups, not both`() {
    val mixed =
      menuOf(single(false), single(false))
        .with("g1", """{"id":"g1","component":"MenuGroup","items":["i0"]}""")
        .with("menu", """{"id":"menu","component":"Menu","anchor":"more","items":["g1","i1"]}""")
    errors(mixed).assertHas(plates("${mixed.indexOf("menu")}") + "/items", "not both")
  }

  @Test
  fun `a menu's groups and items may not mix across size classes either`() {
    val shown =
      menuOf(single(false), single(false))
        .with(
          "g1",
          """{"id":"g1","component":"MenuGroup","items":["i0"],"showIn":["medium","expanded"]}""",
        )
        .with(
          "i1",
          """{"id":"i1","component":"MenuItem","label":"A","showIn":["compact"]}""",
        )
        .with("menu", """{"id":"menu","component":"Menu","anchor":"more","items":["g1","i1"]}""")
    errors(shown).assertHas(plates("${shown.indexOf("menu")}") + "/items", "not both")
    val overridden =
      menuOf(single(false), single(false))
        .with("g1", """{"id":"g1","component":"MenuGroup","items":["i0"]}""")
        .with(
          "menu",
          """{"id":"menu","component":"Menu","anchor":"more","items":["i1"],"sizeClass":{"medium":{"items":["g1"]}}}""",
        )
    errors(overridden).assertHas(plates("${overridden.indexOf("menu")}") + "/items", "not both")
  }

  @Test
  fun `a group hidden at every size does not make a menu of items mixed`() {
    val hidden =
      menuOf(single(false), single(false))
        .with("g1", """{"id":"g1","component":"MenuGroup","items":["i0"],"showIn":[]}""")
        .with("menu", """{"id":"menu","component":"Menu","anchor":"more","items":["g1","i1"]}""")
    assertEquals(emptyList(), errors(hidden).filter { "not both" in it.message })
  }

  // endregion
}
