package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.PaneRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ProposalCodecTest {
  private val fixture = Fixtures.text(Fixtures.PLATES)

  private fun parsed(text: String) =
    assertIs<ProposalParse.Parsed>(ProposalCodec.parse(text)).proposal

  private fun failed(text: String) =
    assertIs<ProposalParse.Failed>(ProposalCodec.parse(text)).errors

  @Test
  fun `the fixture reads into the proposal model`() {
    val proposal = parsed(fixture)
    assertEquals(1, proposal.format)
    assertEquals("Tonight's plate", proposal.name)
    assertEquals("plates", proposal.navigation.start)
    assertEquals(PaneRole.Detail, proposal.navigation.panes["plate_detail"])
    assertEquals(listOf("plates", "plate_detail"), proposal.screens.keys.toList())
    val root = proposal.screens.getValue("plates").components.first()
    assertEquals(ComponentNode("root", "Scaffold", root.props), root)
    assertEquals(JsonPrimitive("bar"), root.props["topBar"])
  }

  @Test
  fun `writing the fixture reproduces it byte for byte`() {
    assertEquals(fixture, ProposalCodec.write(parsed(fixture)))
  }

  @Test
  fun `output is two space pretty printed with a trailing newline`() {
    val text = ProposalCodec.write(parsed(fixture))
    assertTrue(text.endsWith("}\n") && !text.endsWith("\n\n"))
    assertTrue(text.lines().any { it.startsWith("  \"format\": 1") })
    assertTrue(text.lines().none { it.startsWith("\t") })
  }

  @Test
  fun `top level keys are written in a stable order whatever the input order`() {
    val shuffled =
      """{"screens":{"a":{"components":[{"component":"Text","text":"x","id":"root"}],"catalogId":"c"}},"navigation":{"start":"a"},"name":"N","format":1}"""
    val out = ProposalCodec.write(parsed(shuffled))
    val keys =
      Regex("^  \"(\\w+)\"", RegexOption.MULTILINE).findAll(out).map { it.groupValues[1] }.toList()
    assertEquals(listOf("format", "name", "navigation", "screens"), keys)
    // id and component lead every component; props keep their order.
    assertTrue(
      Regex("\"id\": \"root\",\\s+\"component\": \"Text\",\\s+\"text\": \"x\"")
        .containsMatchIn(out),
      out,
    )
  }

  @Test
  fun `a second round trip changes nothing`() {
    val once = ProposalCodec.write(parsed(fixture))
    assertEquals(once, ProposalCodec.write(parsed(once)))
  }

  @Test
  fun `component props keep their json values`() {
    val proposal = parsed(fixture)
    val row = proposal.screens.getValue("plates").components.first { it.id == "plate_row" }
    val action = row.props.getValue("action") as JsonObject
    assertTrue("functionCall" in action)
    assertEquals(JsonObject(mapOf("@path" to JsonPrimitive("name"))), row.props["headline"])
  }

  @Test
  fun `a missing id or component type is kept empty for the validator`() {
    val proposal =
      parsed(
        """{"format":1,"navigation":{"start":"a"},"screens":{"a":{"catalogId":"c","components":[{"component":"Text"},{"id":"x"}]}}}""",
      )
    val components = proposal.screens.getValue("a").components
    assertEquals("", components[0].id)
    assertEquals("", components[1].component)
  }

  @Test
  fun `bad json is reported as an error`() {
    val errors = failed("{ not json")
    assertEquals(1, errors.size)
    assertEquals("/", errors.single().path)
  }

  @Test
  fun `a missing required field is reported`() {
    val errors = failed("""{"format":1,"screens":{}}""")
    assertTrue(errors.single().message.contains("navigation"), errors.toString())
  }

  @Test
  fun `a bad value reports the pointer to it`() {
    val errors =
      failed("""{"format":1,"navigation":{"start":"a","panes":{"a":"sideways"}},"screens":{}}""")
    assertEquals("/navigation/panes/a", errors.single().path, errors.toString())
  }

  @Test
  fun `unknown top level keys are reported`() {
    val errors = failed("""{"format":1,"navigation":{"start":"a"},"screens":{},"screeens":{}}""")
    assertTrue(errors.single().message.contains("screeens"), errors.toString())
  }
}
