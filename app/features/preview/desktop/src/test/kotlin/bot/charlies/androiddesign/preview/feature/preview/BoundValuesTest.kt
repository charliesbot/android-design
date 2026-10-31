package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Values that come from the data model bypass validation, and literal ones can be wrong when a test
 * skips the validator. Neither may throw in the renderer: a throw in the live window stops it
 * recomposing, so the renderer degrades such a value to the nearest one the component accepts.
 */
class BoundValuesTest {
  private val data =
    """{"neg":-3,"zero":0,"huge":1000000000,"nan":"NaN","inf":"Infinity","text":"x"}"""

  private fun neg(key: String) = """"$key":{"@path":"/neg"}"""

  private fun zero(key: String) = """"$key":{"@path":"/zero"}"""

  private fun huge(key: String) = """"$key":{"@path":"/huge"}"""

  private fun nan(key: String) = """"$key":{"@path":"/nan"}"""

  /** Draws [component] as the body of a screen and reports it drew. */
  private fun draws(component: String, view: ViewSpec = Views.Light, extra: String = "") {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"body"},$extra""" +
          """{"id":"body","component":"Column","children":["it"]},""" +
          """{"id":"it",$component}""",
        data,
      )
    val tree = render(proposal, view = view).tree
    assertTrue(tree.has("it"), "drew $component")
  }

  @Test
  fun `maxLines below one degrades`() {
    draws(""""component":"Text","text":"hello",${neg("maxLines")}""")
    draws(""""component":"Text","text":"hello",${zero("maxLines")}""")
  }

  @Test
  fun `lines below one degrade`() {
    draws(""""component":"TextField","label":"Name",${neg("lines")}""")
    draws(""""component":"TextField","variant":"outlined","label":"Name",${zero("lines")}""")
    draws(""""component":"TextField","label":"Name",${huge("lines")}""")
  }

  @Test
  fun `steps below zero degrade`() {
    draws(""""component":"Slider",${neg("steps")}""")
    draws(""""component":"RangeSlider",${neg("steps")}""")
    draws(""""component":"Slider",${huge("steps")}""")
  }

  @Test
  fun `a reversed or empty range degrades`() {
    draws(""""component":"Slider","range":[10,0]""")
    draws(""""component":"Slider","range":[5,5],"value":5""")
    draws(""""component":"RangeSlider","range":[10,0]""")
    draws(""""component":"RangeSlider","range":[0,10],"value":[9,1]""")
    draws(""""component":"RangeSlider","range":[0,10],"value":[-5,50]""")
  }

  @Test
  fun `a value that is not a finite number degrades`() {
    draws(""""component":"Slider",${nan("value")}""")
    draws(""""component":"ProgressIndicator",${nan("progress")}""")
    draws(""""component":"ProgressIndicator","shape":"circular",${neg("progress")}""")
  }

  @Test
  fun `column counts outside what a grid can hold degrade`() {
    val cell = """{"id":"cell","component":"Text","text":"x"},"""
    for (columns in listOf(neg("columns"), zero("columns"), huge("columns"))) {
      draws(""""component":"Grid",$columns,"children":["cell"]""", extra = cell)
    }
    for (width in listOf(neg("minColumnWidth"), zero("minColumnWidth"), huge("minColumnWidth"))) {
      draws(""""component":"Grid",$width,"children":["cell"]""", extra = cell)
    }
  }

  @Test
  fun `sizes and ratios outside what the component can draw degrade`() {
    draws(""""component":"Icon","name":"home",${neg("size")}""")
    draws(""""component":"Icon","name":"home",${huge("size")}""")
    draws(""""component":"Image","src":"missing.png",${zero("aspectRatio")}""")
    draws(""""component":"Image","src":"missing.png",${neg("aspectRatio")}""")
    draws(""""component":"Image","src":"missing.png",${huge("aspectRatio")}""")
  }

  @Test
  fun `a weight that is not positive degrades`() {
    val proposal =
      singleScreen(
        """{"id":"root","component":"Scaffold","content":"col"},""" +
          """{"id":"col","component":"Column","children":["a","b","c"]},""" +
          """{"id":"a","component":"Text","text":"a",${zero("weight")}},""" +
          """{"id":"b","component":"Text","text":"b",${neg("weight")}},""" +
          """{"id":"c","component":"Text","text":"c",${nan("weight")}}""",
        data,
      )
    val tree = render(proposal).tree
    assertTrue(tree.has("a") && tree.has("b") && tree.has("c"))
  }

  @Test
  fun `a negative spacing or corner in the theme degrades`() {
    val proposal =
      proposalOf(
        "home" to
          screenJson(
            """{"id":"root","component":"Scaffold","content":"col"},""" +
              """{"id":"col","component":"Column","spacing":"md","padding":"md","children":["card"]},""" +
              """{"id":"card","component":"Card","shape":"large","padding":"md","content":["t"]},""" +
              """{"id":"t","component":"Text","text":"x"}""",
          ),
        theme = """{"spacing":{"md":"-16px"},"rounded":{"large":"-12px"}}""",
      )
    assertTrue(render(proposal).tree.has("card"))
  }
}
