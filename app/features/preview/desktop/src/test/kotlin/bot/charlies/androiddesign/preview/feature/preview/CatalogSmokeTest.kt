package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.domain.validation.ProposalValidator
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every component of the catalog renders headlessly without crashing and shows up in the layout
 * tree. A case is a small valid screen around one component with the id `subject`.
 */
class CatalogSmokeTest {
  private class Case(val component: String, val components: String, val open: String? = null)

  private val text = """{"id":"t1","component":"Text","text":"Hello"}"""
  private val text2 = """{"id":"t2","component":"Text","text":"World"}"""

  /** Scaffold around a stage Column that holds `subject`. */
  private fun inStage(subject: String, extra: String = "") =
    """{"id":"root","component":"Scaffold","content":"stage"},""" +
      """{"id":"stage","component":"Column","padding":"lg","spacing":"md","children":["subject"]},""" +
      "$subject,$text,$text2$extra"

  private fun inScaffold(slot: String, subject: String, extra: String = "") =
    """{"id":"root","component":"Scaffold","$slot":"subject","content":"t1"},$subject,$text$extra"""

  private val carouselItems =
    """,{"id":"c1","component":"Card","content":["t1"]},{"id":"c2","component":"Card","content":["t2"]},""" +
      """{"id":"c3","component":"Card","content":["t1"]}"""

  private val action = """"action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}"""

  private val menu =
    """{"id":"menu","component":"Menu","anchor":"subject","items":["mi1","mi2"]},""" +
      """{"id":"mi1","component":"MenuItem","label":"Edit","icon":"edit"},""" +
      """{"id":"mi2","component":"MenuItem","label":"Delete"}"""

  private val cases =
    listOf(
      Case(
        "Column",
        inStage("""{"id":"subject","component":"Column","spacing":"sm","children":["t1","t2"]}"""),
      ),
      Case(
        "Row",
        inStage("""{"id":"subject","component":"Row","spacing":"sm","children":["t1","t2"]}"""),
      ),
      Case(
        "Box",
        inStage(
          """{"id":"subject","component":"Box","contentAlignment":"center","children":["t1"]}""",
        ),
      ),
      Case(
        "Grid",
        inStage(
          """{"id":"subject","component":"Grid","columns":2,"spacing":"sm","children":["t1","t2"]}""",
        ),
      ),
      Case(
        "Grid",
        inStage(
          """{"id":"subject","component":"Grid","minColumnWidth":120,"children":["t1","t2"]}""",
        ),
      ),
      Case(
        "FlexBox",
        inStage(
          """{"id":"subject","component":"FlexBox","wrap":true,"gap":"sm","children":["t1","t2"]}""",
        ),
      ),
      Case("Spacer", inStage("""{"id":"subject","component":"Spacer","height":"xl"}""")),
      Case("Divider", inStage("""{"id":"subject","component":"Divider"}""")),
      Case(
        "Shape",
        inStage(
          """{"id":"subject","component":"Shape","shape":"sunny","color":"primaryContainer","content":"t1"}""",
        ),
      ),
      Case(
        "Text",
        inStage(
          """{"id":"subject","component":"Text","text":"Subject","type":"headlineMedium","color":"primary"}""",
        ),
      ),
      Case(
        "Icon",
        inStage(
          """{"id":"subject","component":"Icon","name":"home","size":32,"color":"primary"}""",
        ),
      ),
      Case(
        "Image",
        inStage(
          """{"id":"subject","component":"Image","aspectRatio":1.5,"shape":"large","contentDescription":"Pixel",""" +
            """"src":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="}""",
        ),
      ),
      Case("Image", inStage("""{"id":"subject","component":"Image","src":"missing.png"}""")),
      Case(
        "Carousel",
        inStage(
          """{"id":"subject","component":"Carousel","children":["c1","c2","c3"]}""",
          carouselItems,
        ),
      ),
      Case(
        "Carousel",
        inStage(
          """{"id":"subject","component":"Carousel","variant":"hero","children":["c1","c2","c3"]}""",
          carouselItems,
        ),
      ),
      Case(
        "Carousel",
        inStage(
          """{"id":"subject","component":"Carousel","variant":"centered-hero","children":["c1","c2","c3"]}""",
          carouselItems,
        ),
      ),
      Case(
        "Carousel",
        inStage(
          """{"id":"subject","component":"Carousel","variant":"uncontained","itemWidth":120,"children":["c1","c2","c3"]}""",
          carouselItems,
        ),
      ),
      Case(
        "Carousel",
        inStage(
          """{"id":"subject","component":"Carousel","variant":"full-screen","children":["c1","c2","c3"]}""",
          carouselItems,
        ),
      ),
      Case(
        "Scaffold",
        """{"id":"subject","component":"Scaffold","content":"t1"},$text"""
          .replace("subject", "root"),
      ),
      Case(
        "TopAppBar",
        inScaffold(
          "topBar",
          """{"id":"subject","component":"TopAppBar","title":"Small bar","subtitle":"Sub","navigationIcon":"nav","actions":["act"]}""",
          """,{"id":"nav","component":"IconButton","icon":"arrow_back","contentDescription":"Back"},{"id":"act","component":"IconButton","icon":"search","contentDescription":"Search"}""",
        ),
      ),
      Case(
        "TopAppBar",
        inScaffold(
          "topBar",
          """{"id":"subject","component":"TopAppBar","size":"center","title":"Centered"}""",
        ),
      ),
      Case(
        "TopAppBar",
        inScaffold(
          "topBar",
          """{"id":"subject","component":"TopAppBar","size":"medium","title":"Medium bar"}""",
        ),
      ),
      Case(
        "TopAppBar",
        inScaffold(
          "topBar",
          """{"id":"subject","component":"TopAppBar","size":"large","title":"Large bar","subtitle":"Sub"}""",
        ),
      ),
      Case(
        "NavigationBar",
        inScaffold(
          "bottomBar",
          """{"id":"subject","component":"NavigationBar","children":["n1","n2","n3"]}""",
          """,{"id":"n1","component":"NavigationBarItem","label":"Home","icon":"home","selected":true},{"id":"n2","component":"NavigationBarItem","label":"Search","icon":"search"},{"id":"n3","component":"NavigationBarItem","label":"Saved","icon":"bookmark","selectedIcon":"bookmark"}""",
        ),
      ),
      Case(
        "NavigationBarItem",
        inScaffold(
            "bottomBar",
            """{"id":"bar","component":"NavigationBar","children":["subject","n2"]},{"id":"subject","component":"NavigationBarItem","label":"Home","icon":"home","selected":true}""",
            """,{"id":"n2","component":"NavigationBarItem","label":"Search","icon":"search"}""",
          )
          .replace(""""bottomBar":"subject"""", """"bottomBar":"bar""""),
      ),
      Case(
        "Tabs",
        inStage(
          """{"id":"subject","component":"Tabs","children":["tab1","tab2"]}""",
          """,{"id":"tab1","component":"Tab","label":"Plates","icon":"restaurant","selected":true},{"id":"tab2","component":"Tab","label":"Menus","icon":"menu_book"}""",
        ),
      ),
      Case(
        "Tabs",
        inStage(
          """{"id":"subject","component":"Tabs","variant":"secondary","scrollable":true,"children":["tab1","tab2"]}""",
          """,{"id":"tab1","component":"Tab","label":"Plates"},{"id":"tab2","component":"Tab","label":"Menus","selected":true}""",
        ),
      ),
      Case(
        "Tabs",
        inStage(
          """{"id":"subject","component":"Tabs","scrollable":true,"children":["tab1"]}""",
          """,{"id":"tab1","component":"Tab","label":"Plates"}""",
        ),
      ),
      Case(
        "Tabs",
        inStage(
          """{"id":"subject","component":"Tabs","variant":"secondary","children":["tab1"]}""",
          """,{"id":"tab1","component":"Tab","label":"Plates"}""",
        ),
      ),
      Case(
        "Tab",
        """{"id":"root","component":"Scaffold","content":"tabs"},{"id":"tabs","component":"Tabs","children":["subject"]},{"id":"subject","component":"Tab","label":"Plates","icon":"restaurant","selected":true}""",
      ),
      Case(
        "NavigationRail",
        """{"id":"root","component":"Scaffold","navigationRail":"subject","content":"t1"},{"id":"subject","component":"NavigationRail","expanded":true,"children":["n1","n2"]},{"id":"n1","component":"NavigationRailItem","label":"Home","icon":"home","selected":true},{"id":"n2","component":"NavigationRailItem","label":"Search","icon":"search"},$text""",
      ),
      Case(
        "NavigationRailItem",
        """{"id":"root","component":"Scaffold","navigationRail":"rail","content":"t1"},{"id":"rail","component":"NavigationRail","children":["subject"]},{"id":"subject","component":"NavigationRailItem","label":"Home","icon":"home","selected":true},$text""",
      ),
      Case(
        "FloatingToolbar",
        inScaffold(
          "bottomBar",
          """{"id":"subject","component":"FloatingToolbar","colors":"vibrant","children":["b1","b2"]}""",
          """,{"id":"b1","component":"IconButton","icon":"edit","contentDescription":"Edit"},{"id":"b2","component":"IconButton","icon":"share","contentDescription":"Share"}""",
        ),
      ),
      Case(
        "Card",
        inStage(
          """{"id":"subject","component":"Card","variant":"elevated","content":["t1","t2"]}""",
        ),
      ),
      Case(
        "Card",
        inStage(
          """{"id":"subject","component":"Card","variant":"outlined","shape":"extraLarge",""" +
            action +
            ""","content":["t1"]}""",
          ",$menu".replace(""""anchor":"subject",""", ""),
        ),
      ),
      Case(
        "List",
        inStage(
          """{"id":"subject","component":"List","variant":"segmented","children":["li1","li2"]}""",
          """,{"id":"li1","component":"ListItem","headline":"One","supporting":"First"},{"id":"li2","component":"ListItem","headline":"Two","supporting":"Second","selected":true}""",
        ),
      ),
      Case(
        "List",
        inStage(
          """{"id":"subject","component":"List","children":["li1"]}""",
          """,{"id":"li1","component":"ListItem","headline":"One"}""",
        ),
      ),
      Case(
        "ListItem",
        inStage(
          """{"id":"subject","component":"ListItem","overline":"Over","headline":"Head","supporting":"Support","leading":"t1","trailing":"t2"}""",
        ),
      ),
      Case(
        "Button",
        inStage(
          """{"id":"subject","component":"Button","label":"Cook","icon":"skillet","size":"large","primary":true}""",
        ),
      ),
      Case(
        "Button",
        inStage(
          """{"id":"subject","component":"Button","variant":"tonal","label":"Tonal","size":"extra-small"}""",
        ),
      ),
      Case(
        "Button",
        inStage(
          """{"id":"subject","component":"Button","variant":"elevated","label":"Elevated","size":"medium"}""",
        ),
      ),
      Case(
        "Button",
        inStage(
          """{"id":"subject","component":"Button","variant":"outlined","label":"Outlined","shape":"square"}""",
        ),
      ),
      Case(
        "Button",
        inStage(
          """{"id":"subject","component":"Button","variant":"text","label":"Text","size":"extra-large"}""",
        ),
      ),
      Case(
        "ToggleButton",
        inStage(
          """{"id":"subject","component":"ToggleButton","label":"Toggle","icon":"favorite","checked":true}""",
        ),
      ),
      Case(
        "ToggleButton",
        inStage(
          """{"id":"subject","component":"ToggleButton","variant":"tonal","label":"Toggle"}""",
        ),
      ),
      Case(
        "ButtonGroup",
        inStage(
          """{"id":"subject","component":"ButtonGroup","variant":"connected","children":["g1","g2","g3"]}""",
          """,{"id":"g1","component":"ToggleButton","variant":"tonal","label":"Day","checked":true},{"id":"g2","component":"ToggleButton","variant":"tonal","label":"Week"},{"id":"g3","component":"ToggleButton","variant":"tonal","label":"Month"}""",
        ),
      ),
      Case(
        "ButtonGroup",
        inStage(
          """{"id":"subject","component":"ButtonGroup","children":["g1","g2"]}""",
          """,{"id":"g1","component":"Button","label":"One"},{"id":"g2","component":"Button","variant":"tonal","label":"Two"}""",
        ),
      ),
      Case(
        "IconButton",
        inStage(
          """{"id":"subject","component":"IconButton","variant":"filled","icon":"favorite","toggle":true,"contentDescription":"Like"}""",
        ),
      ),
      Case(
        "IconButton",
        inStage(
          """{"id":"subject","component":"IconButton","variant":"outlined","size":"large","width":"wide","icon":"share","contentDescription":"Share"}""",
        ),
      ),
      Case(
        "IconButton",
        inStage(
          """{"id":"subject","component":"IconButton","variant":"tonal","size":"extra-small","icon":"share"}""",
        ),
      ),
      Case(
        "SplitButton",
        inStage(
          """{"id":"subject","component":"SplitButton","variant":"tonal","label":"Save","icon":"save","menu":"menu"}""",
          """,{"id":"menu","component":"Menu","items":["mi1"]},{"id":"mi1","component":"MenuItem","label":"Save as"}""",
        ),
        open = "menu",
      ),
      Case(
        "FloatingActionButton",
        inScaffold(
          "floatingActionButton",
          """{"id":"subject","component":"FloatingActionButton","size":"large","color":"tertiary-container","icon":"add"}""",
        ),
      ),
      Case(
        "FloatingActionButton",
        inScaffold(
          "floatingActionButton",
          """{"id":"subject","component":"FloatingActionButton","extended":true,"label":"Create","icon":"add","primary":true}""",
        ),
      ),
      Case(
        "FabMenu",
        inScaffold(
          "floatingActionButton",
          """{"id":"subject","component":"FabMenu","icon":"add","expanded":true,"children":["f1","f2"]}""",
          """,{"id":"f1","component":"FabMenuItem","label":"Photo","icon":"image"},{"id":"f2","component":"FabMenuItem","label":"Note","icon":"edit"}""",
        ),
      ),
      Case(
        "FabMenuItem",
        inScaffold(
            "floatingActionButton",
            """{"id":"menu","component":"FabMenu","icon":"add","expanded":true,"children":["subject"]},{"id":"subject","component":"FabMenuItem","label":"Photo","icon":"image"}""",
          )
          .replace(""""floatingActionButton":"subject"""", """"floatingActionButton":"menu""""),
      ),
      Case(
        "AssistChip",
        inStage(
          """{"id":"subject","component":"AssistChip","label":"Directions","icon":"directions"}""",
        ),
      ),
      Case(
        "AssistChip",
        inStage(
          """{"id":"subject","component":"AssistChip","variant":"elevated","label":"Elevated"}""",
        ),
      ),
      Case(
        "FilterChip",
        inStage("""{"id":"subject","component":"FilterChip","label":"Vegan","selected":true}"""),
      ),
      Case(
        "FilterChip",
        inStage(
          """{"id":"subject","component":"FilterChip","variant":"elevated","label":"Quick","icon":"timer"}""",
        ),
      ),
      Case(
        "InputChip",
        inStage("""{"id":"subject","component":"InputChip","label":"Basil","selected":true}"""),
      ),
      Case(
        "SuggestionChip",
        inStage("""{"id":"subject","component":"SuggestionChip","label":"Dessert"}"""),
      ),
      Case(
        "SuggestionChip",
        inStage(
          """{"id":"subject","component":"SuggestionChip","variant":"elevated","label":"Dessert"}""",
        ),
      ),
      Case(
        "ChipGroup",
        inStage(
          """{"id":"subject","component":"ChipGroup","children":["c1","c2","c3"]}""",
          """,{"id":"c1","component":"FilterChip","label":"One","selected":true},{"id":"c2","component":"FilterChip","label":"Two"},{"id":"c3","component":"FilterChip","label":"Three"}""",
        ),
      ),
      Case(
        "ChipGroup",
        inStage(
          """{"id":"subject","component":"ChipGroup","multi":true,"children":["c1","c2"]}""",
          """,{"id":"c1","component":"FilterChip","label":"One","selected":true},{"id":"c2","component":"FilterChip","label":"Two"}""",
        ),
      ),
      Case(
        "Switch",
        inStage(
          """{"id":"subject","component":"Switch","checked":true,"contentDescription":"Notifications"}""",
        ),
      ),
      Case(
        "Checkbox",
        inStage("""{"id":"subject","component":"Checkbox","label":"Remember me","checked":true}"""),
      ),
      Case(
        "Checkbox",
        inStage("""{"id":"subject","component":"Checkbox","contentDescription":"Agree"}"""),
      ),
      Case("Slider", inStage("""{"id":"subject","component":"Slider","value":0.4,"steps":4}""")),
      Case(
        "RangeSlider",
        inStage("""{"id":"subject","component":"RangeSlider","value":[20,80],"range":[0,100]}"""),
      ),
      Case(
        "TextField",
        inStage(
          """{"id":"subject","component":"TextField","label":"Name","value":"Ada","supporting":"Your name","leadingIcon":"person"}""",
        ),
      ),
      Case(
        "TextField",
        inStage(
          """{"id":"subject","component":"TextField","variant":"outlined","label":"Notes","lines":3,"error":true,"prefix":"$","trailingIcon":"close"}""",
        ),
      ),
      Case(
        "LoadingIndicator",
        inStage("""{"id":"subject","component":"LoadingIndicator","contained":true}"""),
      ),
      Case(
        "ProgressIndicator",
        inStage("""{"id":"subject","component":"ProgressIndicator","progress":0.6,"wavy":true}"""),
      ),
      Case(
        "ProgressIndicator",
        inStage("""{"id":"subject","component":"ProgressIndicator","shape":"circular"}"""),
      ),
      Case(
        "BottomSheet",
        inStage(
          """{"id":"opener","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"subject"}}}},{"id":"subject","component":"BottomSheet","content":["t1","t2"]}""",
        ),
        open = "subject",
      ),
      Case(
        "Dialog",
        inStage(
          """{"id":"opener","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"subject"}}}},{"id":"subject","component":"Dialog","title":"Discard?","text":"This cannot be undone.","confirm":"yes","dismiss":"no"},{"id":"yes","component":"Button","variant":"text","label":"Discard"},{"id":"no","component":"Button","variant":"text","label":"Keep"}""",
        ),
        open = "subject",
      ),
      Case(
        "Menu",
        inStage(
          """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"subject"}}}},{"id":"subject","component":"Menu","anchor":"anchor","items":["mi1","mi2"]}""",
          """,{"id":"mi1","component":"MenuItem","label":"Edit","icon":"edit"},{"id":"mi2","component":"MenuItem","label":"Delete"}""",
        ),
        open = "subject",
      ),
      Case(
        "MenuItem",
        inStage(
          """{"id":"anchor","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"menu"}}}},{"id":"menu","component":"Menu","anchor":"anchor","items":["subject"]},{"id":"subject","component":"MenuItem","label":"Edit","icon":"edit"}""",
        ),
        open = "menu",
      ),
      Case(
        "SideSheet",
        inStage(
          """{"id":"opener","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"subject"}}}},{"id":"subject","component":"SideSheet","title":"Filters","back":{"functionCall":{"@call":"dismiss"}},"content":["t1","t2"]}""",
        ),
        open = "subject",
      ),
      Case(
        "SearchBar",
        inStage(
          """{"id":"subject","component":"SearchBar","placeholder":"Search mail","trailing":["mic"],"expanded":"results"},{"id":"mic","component":"IconButton","icon":"mic","contentDescription":"Voice search"},{"id":"results","component":"SearchView","content":["t1"]}""",
        ),
      ),
      Case(
        "SearchView",
        """{"id":"root","component":"Scaffold","content":"bar"},""" +
          """{"id":"bar","component":"SearchBar","placeholder":"Search mail","expanded":"subject"},""" +
          """{"id":"subject","component":"SearchView","layout":"docked","content":["t2"]},$text2""",
        open = "subject",
      ),
      Case(
        "Tooltip",
        """{"id":"root","component":"Scaffold","content":"anchor"},""" +
          """{"id":"anchor","component":"IconButton","icon":"info","contentDescription":"Info"},""" +
          """{"id":"subject","component":"Tooltip","anchor":"anchor","variant":"rich","title":"Why","text":"Because.","actions":["ok"]},""" +
          """{"id":"ok","component":"Button","variant":"text","label":"Got it"}""",
        open = "subject",
      ),
      Case(
        "Snackbar",
        inStage(
          """{"id":"opener","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"subject"}}}},{"id":"subject","component":"Snackbar","message":"Saved","actionLabel":"Undo","dismiss":true}""",
        ),
        open = "subject",
      ),
    )

  @Test
  fun everyCatalogComponentHasACase() {
    val covered = cases.map { it.component }.toSet()
    assertEquals(
      M3ExpressiveCatalog.components.map { it.name }.toSet(),
      covered,
      "catalog components without a smoke case",
    )
  }

  @Test
  fun everyCaseIsAValidProposal() {
    cases.forEachIndexed { index, case ->
      val proposal = singleScreen(case.components)
      val errors = ProposalValidator().validate(proposal)
      assertTrue(errors.isEmpty(), "case $index (${case.component}): $errors")
    }
  }

  @Test
  fun everyCaseRendersAndAppearsInTheTree() {
    cases.forEachIndexed { index, case ->
      val proposal = singleScreen(case.components)
      val result =
        render(proposal, view = Views.Light) { host ->
          case.open?.let { host.interaction.open("home", it) }
        }
      val label = "case $index (${case.component})"
      assertTrue(result.png.size > 1000, "$label: empty png")
      assertTrue(
        result.tree.has("subject") || case.component == "Scaffold" && result.tree.has("root"),
        "$label: subject missing from ${result.tree.nodes.map { it.id }}",
      )
    }
  }
}
