package bot.charlies.androiddesign.preview.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesignMdParserTest {
  private fun parsed(text: String) =
    assertIs<DesignMdParse.Parsed>(DesignMdParser.parse(text)).theme

  @Test
  fun `the fixture front matter is parsed`() {
    val theme = parsed(Fixtures.text(Fixtures.DESIGN_MD))
    assertEquals("alpha", theme.version)
    assertEquals("Tonight's plate", theme.name)
    assertEquals("#B5523B", theme.colors["primary"])
    assertEquals("Fraunces", theme.typography.getValue("display-large").fontFamily)
    assertEquals("'opsz' 72, 'SOFT' 50", theme.typography.getValue("display-large").fontVariation)
    assertEquals("28px", theme.rounded["extra-large"])
    assertEquals("12px", theme.spacing["md"])
    val scheme = assertNotNull(theme.material?.scheme)
    assertEquals("{colors.primary}", scheme.seed)
    assertEquals("tonal-spot", scheme.variant)
    assertEquals(0.0, scheme.contrast)
    val hero = theme.material?.styles?.get("hero_title")
    assertEquals("displayLargeEmphasized", hero?.type)
    assertEquals("onPrimaryContainer", hero?.color)
  }

  @Test
  fun `a file with no front matter gives an empty theme`() {
    val theme = parsed("# Just prose\n\nNo tokens here.\n")
    assertEquals(emptyMap(), theme.colors)
    assertNull(theme.material)
  }

  @Test
  fun `an empty front matter gives an empty theme`() {
    assertEquals(emptyMap(), parsed("---\n---\nprose").colors)
  }

  @Test
  fun `keys the app does not know are ignored`() {
    val theme =
      parsed(
        """
        ---
        version: alpha
        components:
          button-primary: { backgroundColor: "{colors.primary}" }
        colors:
          primary: "#112233"
        typography:
          display-large: { fontFamily: Fraunces, fontSize: 64px, lineHeight: 72px }
        ---
        """
          .trimIndent(),
      )
    assertEquals("#112233", theme.colors["primary"])
    assertEquals("Fraunces", theme.typography.getValue("display-large").fontFamily)
  }

  @Test
  fun `numbers are read as text where a dimension is expected`() {
    val theme = parsed("---\nspacing:\n  md: 16\n  lg: 24px\nrounded:\n  small: 8\n---\n")
    assertEquals("16", theme.spacing["md"])
    assertEquals("24px", theme.spacing["lg"])
    assertEquals("8", theme.rounded["small"])
  }

  @Test
  fun `font weight and letter spacing are read`() {
    val theme =
      parsed("---\ntypography:\n  body-large: { fontWeight: 450, letterSpacing: 0.2px }\n---\n")
    val spec = theme.typography.getValue("body-large")
    assertEquals(450, spec.fontWeight)
    assertEquals("0.2px", spec.letterSpacing)
  }

  @Test
  fun `a front matter that never closes is an error`() {
    val failure =
      assertIs<DesignMdParse.Failed>(DesignMdParser.parse("---\ncolors:\n  primary: \"#fff\"\n"))
    assertTrue("front matter" in failure.message)
  }

  @Test
  fun `broken yaml is an error with a location`() {
    val failure = assertIs<DesignMdParse.Failed>(DesignMdParser.parse("---\ncolors: [oops\n---\n"))
    assertTrue(failure.message.isNotBlank())
  }

  @Test
  fun `windows line endings are accepted`() {
    val theme = parsed("---\r\ncolors:\r\n  primary: \"#B5523B\"\r\n---\r\nprose")
    assertEquals("#B5523B", theme.colors["primary"])
  }
}
